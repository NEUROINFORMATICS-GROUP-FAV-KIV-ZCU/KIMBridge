package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryFactory implements IRepositoryFactory {
	private FactoryConfiguration configuration;

	private DriveConnector connector;

	@Override
	public void setConfiguration(FactoryConfiguration config) throws ConfigurationException {
		configuration = config;
		try {
			connector = new DriveConnector(configuration);
		} catch (Exception e) {
			throw new ConfigurationException("Repository could not be created.", e);
		}
	}

	public DriveRepository createRepository(String id, RepositoryConfiguration config) throws ConfigurationException {
		return new DriveRepository(connector);
	}
}
