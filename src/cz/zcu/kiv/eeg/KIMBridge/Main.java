package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.google.DriveRepositoryFactory;
import cz.zcu.kiv.eeg.KIMBridge.repository.linkedin.LinkedInRepositoryFactory;

import java.io.File;
import java.rmi.RMISecurityManager;

/**
 * Main class of the KIMBridge importer.
 */
public class Main {
	/**
	 * Application entry point.
	 *
	 * Initializes the KIM Bridge and starts fetching the documents.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		createSecurityManager();
		try {
			Configurator config = new Configurator();
			config.loadDefaults();

			SyncState state = new SyncState(new File("./state.properties"));
			state.load();

			KIMBridge bridge = new KIMBridge(config, state);
			bridge.connect();

			DriveRepositoryFactory factory = new DriveRepositoryFactory(config);
			IDocumentRepository googleDriveRepository = factory.createRepository(config.get("googledrive.folder"));
			bridge.registerRepository(googleDriveRepository);

			LinkedInRepositoryFactory linkedInRepositoryFactory = new LinkedInRepositoryFactory(config);
			IDocumentRepository linkedInRepository = linkedInRepositoryFactory.createRepository(config.get("linkedin.group"));
			bridge.registerRepository(linkedInRepository);

			bridge.annotateNewDocuments();

			state.save();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Initializes a new security manager in the global scope.
	 *
	 * Required for the RMI used by KIM client.
	 */
	private static void createSecurityManager() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}
	}
}
