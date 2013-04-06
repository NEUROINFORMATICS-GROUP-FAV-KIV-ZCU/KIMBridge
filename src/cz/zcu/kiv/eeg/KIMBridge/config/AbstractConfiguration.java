package cz.zcu.kiv.eeg.KIMBridge.config;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;

import java.util.Map;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public abstract class AbstractConfiguration {
	private Map<String, String> properties;


	public AbstractConfiguration(Map<String, String> props) {
		properties = props;
	}


	public void addProperties(Map<String, String> props) {
		properties.putAll(props);
	}


	public String getProperty(String key) throws ConfigurationException {
		if (properties.containsKey(key)) {
			return properties.get(key);
		} else {
			throw new ConfigurationException("Missing repository configuration key: %s", key);
		}
	}
}
