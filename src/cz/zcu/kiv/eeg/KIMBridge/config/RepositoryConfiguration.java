package cz.zcu.kiv.eeg.KIMBridge.config;

import java.util.Map;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class RepositoryConfiguration extends AbstractConfiguration {
	private String factoryName;

	public RepositoryConfiguration(String factory, Map<String, String> repositoryProperties) {
		super(repositoryProperties);
		factoryName = factory;
	}


	public String getFactoryName() {
		return factoryName;
	}
}
