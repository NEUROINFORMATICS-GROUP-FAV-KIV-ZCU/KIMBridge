package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;
import cz.zcu.kiv.eeg.KIMBridge.repository.StateRestoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates configured repository factories and repositories.
 *
 * @author Jan Smitka <jan@smitka.org>
 */
public class RepositoryConfigurator {
	private Configurator configurator;

	private Map<String, IRepositoryFactory> factories;

	/**
	 * Creates repository configurator for configuration loaded in specified configurator.
	 * @param config Configurator.
	 */
	public RepositoryConfigurator(Configurator config) {
		configurator = config;
		factories = new HashMap<>();
	}

	/**
	 * Initializes repositories and registers them into specified KIMBridge instance.
	 * @param bridge KIMBridge instance.
	 * @throws ConfigurationException when the configuration does not contain required values.
	 * @throws StateRestoreException when state of repositories cannot be fully restored.
	 */
	public void initializeRepositories(KIMBridge bridge) throws ConfigurationException, StateRestoreException {
		createFactories();

		for (Map.Entry<String, RepositoryConfiguration> repoEntry : configurator.getRepositories().entrySet()) {
			initializeRepository(bridge, repoEntry.getKey(), repoEntry.getValue());
		}
	}

	/**
	 * Creates repository factories.
	 * @throws ConfigurationException when the configuration of factories does not contain required values or one of
	 * the factories could not be instantiated.
	 */
	private void createFactories() throws ConfigurationException {
		try {
			for (Map.Entry<String, FactoryConfiguration> configEntry : configurator.getFactories().entrySet()) {
				FactoryConfiguration configuration = configEntry.getValue();
				IRepositoryFactory factory = (IRepositoryFactory) configuration.getFactoryClass().newInstance();
				factory.setConfiguration(configuration);
				factories.put(configEntry.getKey(), factory);
			}
		} catch (IllegalAccessException|InstantiationException e) {
			throw new ConfigurationException("Cannot instantiate factory.", e);
		}
	}

	/**
	 * Initializes and registers repository to the specified KIMBridge instance.
	 * @param bridge KIMBridge instance.
	 * @param name Internal repository name (ID).
	 * @param config Repository configuration.
	 * @throws ConfigurationException when the repository configuration does not contain required values or
	 * factory for the repository does not exist.
	 * @throws StateRestoreException when the repository state could not be restored.
	 */
	private void initializeRepository(KIMBridge bridge, String name, RepositoryConfiguration config) throws ConfigurationException, StateRestoreException {
		IRepositoryFactory factory = factories.get(config.getFactoryName());
		if (factory == null) {
			throw new ConfigurationException("Invalid factory name.");
		}
		IDocumentRepository repository = factory.createRepository(name, config);
		bridge.registerRepository(repository);
	}

}
