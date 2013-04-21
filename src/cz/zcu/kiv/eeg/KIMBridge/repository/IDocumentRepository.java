package cz.zcu.kiv.eeg.KIMBridge.repository;

import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;

import java.util.List;

/**
 * Interface for generic repository of documents.
 *
 * Must be able to fetch the list of new documents and to download a specified document from the repository.
 */
public interface IDocumentRepository {
	/**
	 * Sets the repository logger.
	 * @param logger Logger.
	 */
	public void setLogger(ILogger logger);

	/**
	 * Gets the repository ID.
	 * @return Repository ID:
	 */
	public String getId();

	/**
	 * Fetches the list of new documents. Contents of documents can be lazily loaded in their implementing classes.
	 * @return List of new documents.
	 * @throws RepositoryException when the list of new documents cannot be retrieved.
	 */
	public List<IDocument> getNewDocuments() throws RepositoryException;

	/**
	 * Called after the document has been successfully indexed in KIM.
	 * @param document
	 * @param kimId
	 */
	public void documentIndexed(IDocument document, long kimId);

	/**
	 * Gets the repository synchronization state.
	 * @return Stored synchronization state of the repository.
	 */
	public IRepositoryState getState();

	/**
	 * Restores the repository synchronization state.
	 * @param state Synchronization state of the repository.
	 * @throws StateRestoreException when the state cannot be restored.
	 */
	public void setState(IRepositoryState state) throws StateRestoreException;
}
