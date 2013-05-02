package cz.zcu.kiv.eeg.KIMBridge;

/**
 * KIM Conector exception.
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMConnectionException extends Exception {
	public KIMConnectionException() {
	}

	public KIMConnectionException(String message) {
		super(message);
	}

	public KIMConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public KIMConnectionException(Throwable cause) {
		super(cause);
	}

	public KIMConnectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
