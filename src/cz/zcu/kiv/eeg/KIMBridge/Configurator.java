package cz.zcu.kiv.eeg.KIMBridge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Configurator loads the repository configuration from specified stream.
 */
public class Configurator {
	private static final String PREFIX_FORMAT = "%s.%s";

	private static final String DEFAULT_CONFIG = "config/default.properties";

	private Properties properties;

	public Configurator() {
		properties = new Properties();
	}

	/**
	 * Loads default configuration.
	 * @throws KIMBridgeException if the default configuration could not be loaded.
	 */
	public void loadDefaults() throws KIMBridgeException {
		InputStream stream = getClass().getResourceAsStream(DEFAULT_CONFIG);
		try {
			load(stream);
		} catch (IOException e) {
			throw new KIMBridgeException("Error while loading configuration.", e);
		}
	}

	public void load(InputStream configStream) throws IOException {
		Properties newProperties = new Properties();
		newProperties.load(configStream);
		properties.putAll(newProperties);
	}


	public String get(String key) {
		return get(key, null);
	}


	public String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public String getPrefixed(String prefix, String key) {
		return getPrefixed(prefix, key, null);
	}

	public String getPrefixed(String prefix, String key, String defaultValue) {
		return get(joinPrefix(prefix, key), defaultValue);
	}

	private String joinPrefix(String prefix, String key) {
		return String.format(PREFIX_FORMAT, prefix, key);
	}

	public File getFile(String key) {
		return new File(get(key));
	}

	public File getFilePrefixed(String prefix, String key) {
		return getFile(joinPrefix(prefix, key));
	}


}
