package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;

/**
 * Factory for Google Drive repository.
 *
 * Only one repository should be created since they share the same access tokens (and thus the same client ID) and
 * synchronization is account-based. Creating multiple repositories will result in data duplication, so only one can
 * be created.
 *
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryFactory implements IRepositoryFactory {
	private FactoryConfiguration configuration;

	private DriveConnector connector;

	private boolean created = false;

	/**
	 * Sets the factory configuration and access tokens.
	 * @param config Factory configuration.
	 * @throws ConfigurationException when the configuration does not contain all required values.
	 * @see DriveConnector
	 */
	@Override
	public void setConfiguration(FactoryConfiguration config) throws ConfigurationException {
		configuration = config;
		try {
			connector = new DriveConnector(configuration);
		} catch (Exception e) {
			throw new ConfigurationException("Repository could not be created.", e);
		}
	}

	/**
	 * Creates an Google Drive repository. Only one repository can be created to prevent data duplication.
	 * @param id Repository ID.
	 * @param config Repository configuration.
	 * @return New Google Drive repository.
	 * @throws ConfigurationException when multiple repositories are being created.
	 */
	public DriveRepository createRepository(String id, RepositoryConfiguration config) throws ConfigurationException {
		if (!created) {
			created = true;
			return new DriveRepository(id, connector);
		} else {
			throw new ConfigurationException("Only one Google Drive repository can be created.");
		}
	}
}
