package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.logging.ConsoleLoggerFactory;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILoggerFactory;
import cz.zcu.kiv.eeg.KIMBridge.repository.StateRestoreException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Timer;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMBridgeDaemon implements Daemon {
	/** New document indexing period. Default: 5min. */
	private static final String KEY_INDEX_PERIOD = "indexPeriod";

	private static final String KEY_SYNC_FILE = "syncFile";

	private static final String CONFIG_FILE = "./config.xml";

	private Timer scheduler;

	private ILoggerFactory loggerFactory;

	private KIMIndexTask task;

	private Configurator config;

	private SyncStatePersister syncState;

	private KIMConnector connector;

	private KIMBridge kimBridge;


	public static void main(String [] args) {
		KIMBridgeDaemon daemon = new KIMBridgeDaemon();
		registerShutdownHandler(daemon);
		try {
			daemon.init(null);
			daemon.start();
		} catch (Exception e) {
			daemon.handleException(e);
		}

	}

	public static void registerShutdownHandler(KIMBridgeDaemon daemon) {
		Runtime.getRuntime().addShutdownHook(new ShutdownHandler(daemon));
	}

	public void handleException(Exception e) {
		e.printStackTrace();
	}


	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		scheduler = new Timer();
		loggerFactory = new ConsoleLoggerFactory();
		createConfigurator();
		createSyncState();
		createKIMBridge();
		createRepositories();

		task = new KIMIndexTask(this, kimBridge);
	}

	private void createConfigurator() throws KIMBridgeException {
		try {
			ILogger configLogger = loggerFactory.createLogger(Configurator.LOG_COMPONENT);
			config = new Configurator(configLogger);
			config.loadDefaults();

			File configFile = new File(CONFIG_FILE);
			if (configFile.exists()) {
				config.loadFile(configFile);
			}
		} catch (ConfigurationException|IOException e) {
			throw KIMBridgeException.loadingConfiguration(e);
		}
	}

	private void createSyncState() throws IOException, ConfigurationException, ClassNotFoundException {
		ILogger syncStateLogger = loggerFactory.createLogger(SyncStatePersister.LOG_COMPONENT);
		syncState = new SyncStatePersister(syncStateLogger, new File(config.get(KEY_SYNC_FILE)));
		syncState.load();
	}


	private void createKIMBridge() throws KIMBridgeException {
		try {
			ILogger connectorLogger = loggerFactory.createLogger(KIMConnector.LOG_COMPONENT);
			connector = new KIMConnector(connectorLogger);
			kimBridge = new KIMBridge(loggerFactory, connector, syncState);
		} catch (RemoteException e) {
			throw KIMBridgeException.connectingToKim(e);
		}
	}

	private void createRepositories() throws StateRestoreException, ConfigurationException {
		RepositoryConfigurator repoConfigurator = new RepositoryConfigurator(config);
		repoConfigurator.initializeRepositories(kimBridge);
	}


	@Override
	public void start() throws Exception {
		connector.connect();
		scheduler.schedule(task, 0, Long.parseLong(config.get(KEY_INDEX_PERIOD)));
	}

	@Override
	public void stop() throws Exception {
		scheduler.cancel();
		kimBridge.saveRepositoryStates();
		syncState.save();
	}

	@Override
	public void destroy() {

	}


	/**
	 * Handler to correctly shutdown KIMBridge daemon running in non-daemon mode.
	 */
	private static class ShutdownHandler extends Thread {
		private KIMBridgeDaemon daemon;

		/**
		 * Initializes shutdown handler for the specified daemon.
		 * @param kimDaemon Daemon to shutdown.
		 */
		public ShutdownHandler(KIMBridgeDaemon kimDaemon) {
			daemon = kimDaemon;
		}

		/**
		 * Shutdowns the daemon.
		 */
		@Override
		public void run() {
			try {
				daemon.stop();
				daemon.destroy();
			} catch (Exception e) {
				daemon.handleException(e);
			}
		}
	}
}
