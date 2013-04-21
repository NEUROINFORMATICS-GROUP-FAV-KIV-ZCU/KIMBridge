package cz.zcu.kiv.eeg.KIMBridge.repository;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;

/**
 * Document repository factory.
 * @author Jan Smitka <jan@smitka.org>
 */
public interface IRepositoryFactory {
	/**
	 * Sets the factory configuration.
	 * @param configuration Factory configuration.
	 * @throws ConfigurationException when the configuration does not contain required values.
	 */
	public void setConfiguration(FactoryConfiguration configuration) throws ConfigurationException;

	/**
	 * Creates a new repository with given configuration.
	 * @param id Repository ID.
	 * @param configuration Repository configuration.
	 * @return Created repository.
	 * @throws ConfigurationException when the configuration does not contain required values.
	 */
	public IDocumentRepository createRepository(String id, RepositoryConfiguration configuration) throws ConfigurationException;
}
