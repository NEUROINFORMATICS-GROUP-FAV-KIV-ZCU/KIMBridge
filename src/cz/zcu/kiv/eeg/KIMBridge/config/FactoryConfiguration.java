package cz.zcu.kiv.eeg.KIMBridge.config;

import java.util.Map;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class FactoryConfiguration extends AbstractConfiguration {
	private Class type;


	public FactoryConfiguration(String className, Map<String, String> factoryProperties) throws ClassNotFoundException {
		super(factoryProperties);
		type = Class.forName(className);
	}


	public Class getFactoryClass() {
		return type;
	}
}
