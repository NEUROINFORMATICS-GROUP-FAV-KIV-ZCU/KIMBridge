package cz.zcu.kiv.eeg.KIMBridge;

/**
 * KIMBridge configuration exception.
 * @author Jan Smitka <jan@smitka.org>
 */
public class ConfigurationException extends Exception {
	public ConfigurationException() {
	}

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String messageFormat, Object... arguments) {
		super(String.format(messageFormat, arguments));
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

	public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
