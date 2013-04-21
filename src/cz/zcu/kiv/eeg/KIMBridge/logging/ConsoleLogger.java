package cz.zcu.kiv.eeg.KIMBridge.logging;

import java.io.PrintStream;
import java.util.Date;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class ConsoleLogger implements ILogger {
	private String component;

	public ConsoleLogger(String logComponent) {
		component = logComponent;
	}

	@Override
	public void logMessage(String message, Object... params) {
		printLinePrefix(System.out);
		System.out.format(message, params);
		System.out.println();
	}

	public void logException(Exception exception) {
		printLinePrefix(System.err);
		exception.printStackTrace(System.err);
		System.err.println();
	}

	private void printLinePrefix(PrintStream stream) {
		stream.format("%tc [%s] ", new Date(), component);
	}
}
