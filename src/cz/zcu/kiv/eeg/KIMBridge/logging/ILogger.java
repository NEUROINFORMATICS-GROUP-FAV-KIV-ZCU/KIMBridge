package cz.zcu.kiv.eeg.KIMBridge.logging;

/**
 * Interface for loggers. Logger should be able to log component which emitted the log entry.
 * @author Jan Smitka <jan@smitka.org>
 */
public interface ILogger {
	public static final int INFO = 0;
	public static final int ERROR = 1;

	/**
	 * Logs a message.
	 * @param message Message as format string.
	 * @param params Formatting params.
	 */
	public void logMessage(String message, Object... params);

	/**
	 * Logs an exception.
	 * @param exception Exception to be logged.
	 */
	public void logException(Exception exception);
}
