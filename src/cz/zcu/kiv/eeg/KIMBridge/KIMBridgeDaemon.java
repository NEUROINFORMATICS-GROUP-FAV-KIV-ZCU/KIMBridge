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

	private ILogger logger;

	private Timer scheduler;

	private ILoggerFactory loggerFactory;

	private KIMIndexTask task;

	private Configurator config;

	private SyncStatePersister syncState;

	private KIMConnector connector;

	private KIMBridge kimBridge;

	/**
	 * KIMBridge CLI entry point.
	 * @param args Command line arguments.
	 */
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

	/**
	 * Registers shutdown handler for the CLI runtime.
	 * @param daemon Daemon which should be shut down.
	 */
	public static void registerShutdownHandler(KIMBridgeDaemon daemon) {
		Runtime.getRuntime().addShutdownHook(new ShutdownHandler(daemon));
	}

	/**
	 * Handles the exception.
	 * @param e Exception.
	 */
	public void handleException(Exception e) {
		e.printStackTrace();
	}

	/**
	 * Initializes the KIM service.
	 * @param daemonContext Context of the daemon. Required by {@code Daemon} interface.
	 * @throws DaemonInitException never thrown.
	 * @throws Exception when any exception occurs. :-) Required by {@code Daemon} interface.
	 */
	@Override
	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		scheduler = new Timer();
		loggerFactory = new ConsoleLoggerFactory();
		logger = loggerFactory.createLogger("KIMBridgeDaemon");
		String configPath = CONFIG_FILE;
		if (daemonContext == null) {
			logger.logMessage("Running without daemon context with default configuration in %s", CONFIG_FILE);
		} else {
			String[] arguments = daemonContext.getArguments();
			logger.logMessage("Running in daemon context with %d params: %s", arguments.length, buildArgs(arguments));
			if (arguments.length > 0) {
				configPath = arguments[0];
			}
		}

		createConfigurator(configPath);
		createSyncState();
		createKIMBridge();
		createRepositories();

		task = new KIMIndexTask(kimBridge, logger);
	}

	private String buildArgs(String[] args) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String s : args) {
			stringBuilder.append(s);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	/**
	 * Creates configurator, loads default configuration and a instance-specific configuration file (config.xml by default).
	 * @throws KIMBridgeException when the configuration could not be loaded.
	 */
	private void createConfigurator(String configPath) throws KIMBridgeException {
		try {
			ILogger configLogger = loggerFactory.createLogger(Configurator.LOG_COMPONENT);
			config = new Configurator(configLogger);

			File configFile = new File(configPath);
			if (configFile.exists()) {
				config.loadFile(configFile);
			} else {
				throw new ConfigurationException(String.format("Configuration file %s is not present.", configFile.getPath()));
			}
		} catch (ConfigurationException|IOException e) {
			throw KIMBridgeException.loadingConfiguration(e);
		}
	}

	/**
	 * Creates synchronization state persister and loads repository states.
	 * @throws IOException when the synchronization file could not be read.
	 * @throws ConfigurationException when the configuration is malformed.
	 * @throws ClassNotFoundException when one of the classes in the binary store could not be loaded.
	 */
	private void createSyncState() throws IOException, ConfigurationException, ClassNotFoundException {
		ILogger syncStateLogger = loggerFactory.createLogger(SyncStatePersister.LOG_COMPONENT);
		syncState = new SyncStatePersister(syncStateLogger, new File(config.get(KEY_SYNC_FILE)));
		syncState.load();
	}


	/**
	 * Creates a connection to KIM and a KIMBridge instance.
	 * @throws KIMBridgeException when the instance could not be initialized.
	 */
	private void createKIMBridge() throws KIMBridgeException {
		ILogger connectorLogger = loggerFactory.createLogger(KIMConnector.LOG_COMPONENT);
		connector = new KIMConnector(connectorLogger);
		kimBridge = new KIMBridge(loggerFactory, connector, syncState);
	}

	/**
	 * Creates remote document repositories.
	 * @throws StateRestoreException when state of the repositories could not be fully restored.
	 * @throws ConfigurationException when the configuration does not contain all required values.
	 */
	private void createRepositories() throws StateRestoreException, ConfigurationException {
		RepositoryConfigurator repoConfigurator = new RepositoryConfigurator(config);
		repoConfigurator.initializeRepositories(kimBridge);
	}


	/**
	 * Connects to KIM Platform and schedules indexing tasks.
	 * @throws Exception when any exception occurs. Required by {@code Daemon} interface.
	 */
	@Override
	public void start() throws Exception {
		try {
			connector.connect();
			scheduler.schedule(task, 0, Long.parseLong(config.get(KEY_INDEX_PERIOD)));
		} catch (RemoteException e) {
			throw KIMBridgeException.connectingToKim(e);
		}
	}

	/**
	 * Stops the service.
	 * @throws Exception when any exception occurs. Required by {@code Daemon} interface.
	 */
	@Override
	public void stop() throws Exception {
		scheduler.cancel();
		kimBridge.saveRepositoryStates();
		kimBridge.persistStates();

		logger.logMessage("Shut down.");
	}

	/**
	 * Removes all service data.
	 */
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
