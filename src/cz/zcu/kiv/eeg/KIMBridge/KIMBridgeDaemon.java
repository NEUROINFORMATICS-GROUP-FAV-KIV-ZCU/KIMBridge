package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.StateRestoreException;
import cz.zcu.kiv.eeg.KIMBridge.repository.google.DriveRepositoryFactory;
import cz.zcu.kiv.eeg.KIMBridge.repository.linkedin.LinkedInRepositoryFactory;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import java.io.IOException;
import java.util.Timer;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMBridgeDaemon implements Daemon {
	/** New document indexing period. Default: 5min. */
	private static final String KEY_INDEX_PERIOD = "kim.indexPeriod";

	private static final String KEY_SYNC_FILE = "kim.syncFile";

	private static final String KEY_DRIVE_FOLDER = "googledrive.folder";

	private static final String KEY_LINKEDIN_GROUP = "linkedin.group";

	private Timer scheduler;

	private KIMIndexTask task;

	private Configurator config;

	private SyncState syncState;

	private KIMBridge kimBridge;

	private DriveRepositoryFactory driveRepositoryFactory;

	private LinkedInRepositoryFactory linkedInRepositoryFactory;


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
		task = new KIMIndexTask(this, kimBridge);

		createConfigurator();
		createSyncState();
		createKIMBridge();
		createFactories();
		createRepositories();
	}

	private void createConfigurator() throws KIMBridgeException {
		config = new Configurator();
		config.loadDefaults();
	}

	private void createSyncState() throws IOException {
		syncState = new SyncState(config.getFile(KEY_SYNC_FILE));
		syncState.load();
	}


	private void createKIMBridge() {
		kimBridge = new KIMBridge(config, syncState);
	}

	private void createFactories() {
		driveRepositoryFactory = new DriveRepositoryFactory(config);
		linkedInRepositoryFactory = new LinkedInRepositoryFactory(config);
	}

	private void createRepositories() throws StateRestoreException{
		kimBridge.registerRepository(createDriveRepository());
		kimBridge.registerRepository(createLinkedInRepository());
	}

	private IDocumentRepository createDriveRepository() {
		return driveRepositoryFactory.createRepository(config.get(KEY_DRIVE_FOLDER));
	}

	private IDocumentRepository createLinkedInRepository() {
		return linkedInRepositoryFactory.createRepository(config.get(KEY_LINKEDIN_GROUP));
	}


	@Override
	public void start() throws Exception {
		kimBridge.connect();
		scheduler.schedule(task, 0, Long.parseLong(config.get(KEY_INDEX_PERIOD)));
	}

	@Override
	public void stop() throws Exception {
		scheduler.cancel();

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
