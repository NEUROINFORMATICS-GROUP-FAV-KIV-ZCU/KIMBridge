package cz.zcu.kiv.eeg.KIMBridge.logging;

import java.io.PrintStream;
import java.util.Date;

/**
 * STDOUT and STDERR logger.
 * @author Jan Smitka <jan@smitka.org>
 */
public class ConsoleLogger implements ILogger {
	private String component;

	/**
	 * Initializes the logger.
	 * @param logComponent Component string.
	 */
	public ConsoleLogger(String logComponent) {
		component = logComponent;
	}

	/**
	 * Formats and logs a message.
	 * @param message Message as format string.
	 * @param params Formatting params.
	 */
	@Override
	public void logMessage(String message, Object... params) {
		printLinePrefix(System.out);
		System.out.format(message, params);
		System.out.println();
	}

	/**
	 * Logs an exception.
	 * @param exception Exception to be logged.
	 */
	public void logException(Exception exception) {
		printLinePrefix(System.err);
		exception.printStackTrace(System.err);
		System.err.println();
	}

	/**
	 * Prints line prefix with current date, time and component string.
	 * @param stream PrintStream.
	 */
	private void printLinePrefix(PrintStream stream) {
		stream.format("%tc [%s] ", new Date(), component);
	}
}
