package cz.zcu.kiv.eeg.KIMBridge.repository;

import java.util.List;

/**
 * Interface for generic repository of documents.
 *
 * Must be able to fetch the list of new documents and to download a specified document from the repository.
 */
public interface IDocumentRepository {
	public String getId();

	public List<IDocument> getAllDocuments() throws RepositoryException;

	public List<IDocument> getNewDocuments() throws RepositoryException;

	public void documentIndexed(IDocument document, long kimId);

	public IRepositoryState getState();

	public void setState(IRepositoryState state) throws StateRestoreException;
}
