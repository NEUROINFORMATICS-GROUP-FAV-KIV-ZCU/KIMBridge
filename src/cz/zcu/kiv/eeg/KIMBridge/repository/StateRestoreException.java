package cz.zcu.kiv.eeg.KIMBridge.repository;

/**
 * Repository state restore exception.
 * @author Jan Smitka <jan@smitka.org>
 */
public class StateRestoreException extends Exception {
	public StateRestoreException() {
	}

	public StateRestoreException(String message) {
		super(message);
	}

	public StateRestoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public StateRestoreException(Throwable cause) {
		super(cause);
	}

	public StateRestoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
