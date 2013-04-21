package cz.zcu.kiv.eeg.KIMBridge.config;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;

import java.util.Map;

/**
 * Base class of element configuration.
 * @author Jan Smitka <jan@smitka.org>
 */
public abstract class AbstractConfiguration {
	private Map<String, String> properties;

	/**
	 * Initializes configuration with specified properties.
	 * @param props Properties.
	 */
	public AbstractConfiguration(Map<String, String> props) {
		properties = props;
	}

	/**
	 * Adds list of properties to the configuration.
	 * @param props Map of properties.
	 */
	public void addProperties(Map<String, String> props) {
		properties.putAll(props);
	}

	/**
	 * Gets property with specified name.
	 * @param key Name of the property.
	 * @return Value.
	 * @throws ConfigurationException when the property does not exist.
	 */
	public String getProperty(String key) throws ConfigurationException {
		if (properties.containsKey(key)) {
			return properties.get(key);
		} else {
			throw new ConfigurationException("Missing repository configuration key: %s", key);
		}
	}
}
