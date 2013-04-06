package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryFactory implements IRepositoryFactory {
	private static final String KEY_FOLDER = "folder";

	FactoryConfiguration configuration;

	public DriveRepositoryFactory(FactoryConfiguration config) {
		configuration = config;
	}

	public DriveRepository createRepository(String id, RepositoryConfiguration config) {
		try {
			DriveConnector connector = new DriveConnector(configuration);

			return new DriveRepository(connector, config.getProperty(KEY_FOLDER));
		} catch (Exception e) {
			//TODO: catch exception
			return null;
		}
	}
}
