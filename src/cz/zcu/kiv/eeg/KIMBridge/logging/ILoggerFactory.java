package cz.zcu.kiv.eeg.KIMBridge.logging;

/**
 * Interface for log factories. Log factory should be able to create a logger for specific component.
 * @author Jan Smitka <jan@smitka.org>
 */
public interface ILoggerFactory {
	/**
	 * Creates a logger for specific component.
	 * @param component Textual component identifier.
	 * @return Logger for specific component.
	 */
	public ILogger createLogger(String component);
}
