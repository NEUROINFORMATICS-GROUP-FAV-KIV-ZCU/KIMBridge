package cz.zcu.kiv.eeg.KIMBridge;

/**
 * Generic KIMBridge exception. Used as a wrapper for all KIMBridge operation exceptions.
 */
public class KIMBridgeException extends Exception {
	public KIMBridgeException() {
	}

	public KIMBridgeException(String message) {
		super(message);
	}

	public KIMBridgeException(String message, Throwable cause) {
		super(message, cause);
	}

	public KIMBridgeException(Throwable cause) {
		super(cause);
	}
}
