package cz.zcu.kiv.eeg.KIMBridge.config;

import java.util.Map;

/**
 * Document repository factory configuration.
 * @author Jan Smitka <jan@smitka.org>
 */
public class FactoryConfiguration extends AbstractConfiguration {
	private Class type;

	/**
	 * Initializes configuration for factory of specified type.
	 * @param className Class of the factory.
	 * @param factoryProperties Properties.
	 * @throws ClassNotFoundException when the factory class cannot be found.
	 */
	public FactoryConfiguration(String className, Map<String, String> factoryProperties) throws ClassNotFoundException {
		super(factoryProperties);
		type = Class.forName(className);
	}

	/**
	 * Gets the document repository factory class.
	 * @return Class.
	 */
	public Class getFactoryClass() {
		return type;
	}
}
