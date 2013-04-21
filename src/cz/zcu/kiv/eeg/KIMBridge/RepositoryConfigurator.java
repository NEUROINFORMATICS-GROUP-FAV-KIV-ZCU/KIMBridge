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

	public RepositoryConfigurator(Configurator config) {
		configurator = config;
		factories = new HashMap<>();
	}


	public void initializeRepositories(KIMBridge bridge) throws ConfigurationException, StateRestoreException {
		createFactories();

		for (Map.Entry<String, RepositoryConfiguration> repoEntry : configurator.getRepositories().entrySet()) {
			initializeRepository(bridge, repoEntry.getKey(), repoEntry.getValue());
		}
	}

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


	private void initializeRepository(KIMBridge bridge, String name, RepositoryConfiguration config) throws ConfigurationException, StateRestoreException {
		IRepositoryFactory factory = factories.get(config.getFactoryName());
		IDocumentRepository repository = factory.createRepository(name, config);
		bridge.registerRepository(repository);
	}

}
