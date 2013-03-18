package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.Configurator;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryFactory {
	Configurator configurator;

	public DriveRepositoryFactory(Configurator configurator) {
		this.configurator = configurator;
	}

	public DriveRepository createRepository(String folder) {
		try {
			DriveConnector connector = new DriveConnector(configurator);

			return new DriveRepository(connector, folder);
		} catch (Exception e) {
			//TODO: catch exception
			return null;
		}
	}
}
