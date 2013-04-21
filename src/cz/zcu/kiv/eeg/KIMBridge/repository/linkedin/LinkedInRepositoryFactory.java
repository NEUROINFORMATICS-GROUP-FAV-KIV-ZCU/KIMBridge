package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;

/**
 * Repository for LinkedIn repositories. Each repository checks new discussions in specified group.
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepositoryFactory implements IRepositoryFactory {
	private static final String KEY_GROUP_ID = "gid";

	private FactoryConfiguration configuration;

	private LinkedInConnector connector;

	/**
	 * Sets the factory configuration.
	 * @param config Factory configuration.
	 * @throws ConfigurationException when the configuration does not contain required values.
	 */
	@Override
	public void setConfiguration(FactoryConfiguration config) throws ConfigurationException {
		configuration = config;
		connector = new LinkedInConnector(configuration);
	}

	/**
	 * Creates a repository for group with specified ID.
	 * @param id Repository ID.
	 * @param config Repository configuration.
	 * @return Repository for the specified group.
	 * @throws ConfigurationException when the configuration does not contain group ID.
	 */
	@Override
	public IDocumentRepository createRepository(String id, RepositoryConfiguration config) throws ConfigurationException {
		return new LinkedInRepository(id, connector, config.getProperty(KEY_GROUP_ID));
	}
}
