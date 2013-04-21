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


	public static KIMBridgeException loadingConfiguration(Throwable cause) {
		return new KIMBridgeException("Error while loading configuration.", cause);
	}

	public static KIMBridgeException settingMetadata(Throwable cause) {
		return new KIMBridgeException("Error while setting document metadata.", cause);
	}

	public static KIMBridgeException annotatingDocument(Throwable cause) {
		return new KIMBridgeException("Error while annotating the document.", cause);
	}

	public static KIMBridgeException fetchingDocuments(Throwable cause) {
		return new KIMBridgeException("Error while fetching documents from remote repositories.", cause);
	}

	public static KIMBridgeException synchronizingIndex(Throwable cause) {
		return new KIMBridgeException("Error while synchronizing document index.", cause);
	}


	public static KIMBridgeException fetchingDocument(Throwable cause) {
		return new KIMBridgeException("Error while fetching document from repository.", cause);
	}

	public static KIMBridgeException synchronizingDocument(Throwable cause) {
		return new KIMBridgeException("Error while synchronizing document with document repository.", cause);
	}

	public static KIMBridgeException connectingToKim(Throwable cause) {
		return new KIMBridgeException("Error while connecting to KIM Platform.", cause);
	}
}
