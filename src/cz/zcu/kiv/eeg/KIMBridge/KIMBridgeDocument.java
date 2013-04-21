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

	/**
	 * Gets the document ID.
	 * @return ID of the document stored in KIM.
	 */
	public long getId() {
		return document.getDocumentId();
	}

	/**
	 * Gets the underlying document.
	 *
	 * Before the document is returned, all metadata are updated.
	 *
	 * @return Underlying document.
	 * @throws KIMCorporaException when the metadata could not be written.
	 */
	public KIMDocument getDocument() throws KIMCorporaException {
		writeFeatures();
		return document;
	}

	/**
	 * Gets the document title.
	 * @return Document title.
	 */
	public String getTitle() {
		return (String) metadata.get(FEATURE_TITLE);
	}


	/**
	 * Sets the document title.
	 * @param title New document title.
	 */
	public void setTitle(String title) {
		metadata.put(FEATURE_TITLE, title);
	}

	/**
	 * Sets the document URL.
	 * @return URL where the document can be found.
	 */
	public String getUrl() {
		return (String) metadata.get(FEATURE_URL);
	}

	/**
	 * Sets the URL of the document.
	 * @param url URL where the document can be found.
	 */
	public void setUrl(String url) {
		metadata.put(FEATURE_URL, url);
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


	/**
	 * Copy content and title from another document.
	 * @param otherDoc Document from which the content will by copied.
	 * @throws KIMCorporaException when the metadata could not be written.
	 */
	public void copyContentFrom(KIMBridgeDocument otherDoc) throws KIMCorporaException {
		KIMDocument rawDoc = otherDoc.getDocument();
		setTitle(otherDoc.getTitle());
		document.setContent(rawDoc.getContent());
	}

}
