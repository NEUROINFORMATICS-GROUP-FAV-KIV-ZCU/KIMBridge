package cz.zcu.kiv.eeg.KIMBridge.config;

import java.util.Map;

/**
 * Document repository configuration.
 * @author Jan Smitka <jan@smitka.org>
 */
public class RepositoryConfiguration extends AbstractConfiguration {
	private String factoryName;

	/**
	 * Initializes repository configuration.
	 * @param factory Factory name.
	 * @param repositoryProperties Repository properties.
	 */
	public RepositoryConfiguration(String factory, Map<String, String> repositoryProperties) {
		super(repositoryProperties);
		factoryName = factory;
	}


	/**
	 * Gets the factory name for the repository.
	 * @return Name of the factory.
	 */
	public String getFactoryName() {
		return factoryName;
	}
}
