package cz.zcu.kiv.eeg.KIMBridge.logging;

/**
 * Factory for console loggers.
 *
 * @author Jan Smitka <jan@smitka.org>
 */
public class ConsoleLoggerFactory implements ILoggerFactory {
	/**
	 * Creates a console logger for specified component.
	 * @param component Textual component identifier.
	 * @return Console logger for specific component.
	 */
	@Override
	public ILogger createLogger(String component) {
		return new ConsoleLogger(component);
	}
}
