package cz.zcu.kiv.eeg.KIMBridge;

import com.ontotext.kim.client.corpora.KIMCorporaException;
import com.ontotext.kim.client.corpora.KIMDocument;
import com.ontotext.kim.client.corpora.KIMFeatureMap;

/**
 * Wrapper for KIMDocument.
 *
 * Provides API for document metadata manipulation.
 */
public class KIMBridgeDocument {
	/** Document title key. */
	private static final String FEATURE_TITLE = "TITLE";

	/** Document URL key. */
	private static final String FEATURE_URL = "URL";

	/** Underlying KIMDocument. */
	private KIMDocument document;

	/** Collection of document metadata and features. */
	private KIMFeatureMap metadata;

	/**
	 * Creates the wrapper.
	 * @param kimDocument KIMDocument to be wrapped and manipulated with.
	 */
	public KIMBridgeDocument(KIMDocument kimDocument) {
		document = kimDocument;
		metadata = kimDocument.getFeatures();
	}

	public long getId() {
		return document.getDocumentId();
	}

	public KIMDocument getDocument() throws KIMCorporaException {
		writeFeatures();
		return document;
	}

	public String getTitle() {
		return (String) metadata.get(FEATURE_TITLE);
	}


	public void setTitle(String title) {
		metadata.put(FEATURE_TITLE, title);
	}


	/**
	 * Writes the features back to the document object.
	 *
	 * Should be called before the document is stored in the persistence.
	 * KIMConnector handles this automatically.
	 */
	public void writeFeatures() throws KIMCorporaException {
		document.setFeatures(metadata);
	}


	public void copyContentFrom(KIMBridgeDocument otherDoc) throws KIMCorporaException {
		KIMDocument rawDoc = otherDoc.getDocument();
		setTitle(otherDoc.getTitle());
		document.setContent(rawDoc.getContent());
	}

}
