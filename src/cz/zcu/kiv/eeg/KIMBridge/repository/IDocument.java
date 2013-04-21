package cz.zcu.kiv.eeg.KIMBridge.repository;

/**
 * Interface for all repository documents. Must not be implemented!
 *
 * Implement descendant interfaces instead.
 *
 * @see IBinaryDocument
 * @see ITextDocument
 */
public interface IDocument {
	/**
	 * Get the document ID in KIM Platform. Used only when this document is an update to the existing document.
	 * @return
	 */
	public long getId();

	/**
	 * Is this a new document?
	 * @return {@code true} if the document is new, {@code false} when this is an update to existing document.
	 */
	public boolean isNew();

	/**
	 * Get document title.
	 * @return Document title.
	 */
	public String getTitle();

	/**
	 * Get the original document URL.
	 * @return Document URL.
	 */
	public String getUrl();
}
