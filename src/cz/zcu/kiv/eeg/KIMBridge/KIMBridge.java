package cz.zcu.kiv.eeg.KIMBridge;

import com.ontotext.kim.client.corpora.KIMCorporaException;
import com.ontotext.kim.client.documentrepository.DocumentRepositoryException;
import com.ontotext.kim.client.query.KIMQueryException;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILoggerFactory;
import cz.zcu.kiv.eeg.KIMBridge.repository.*;

import java.io.IOException;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

/**
 * Main KIMBridge class.
 */
public class KIMBridge {
	private static final char EXT_SEPARATOR = '.';
	private static final String LOG_COMPONENT = "KIMBridge";

	private SyncStatePersister state;

	private KIMConnector kim;

	private List<IDocumentRepository> repositories;

	private ILoggerFactory loggerFactory;

	private ILogger logger;


	public KIMBridge(ILoggerFactory loggerFactory, KIMConnector connector, SyncStatePersister syncState) {
		kim = connector;
		state = syncState;
		repositories = new LinkedList<IDocumentRepository>();
		this.loggerFactory = loggerFactory;
		logger = loggerFactory.createLogger(LOG_COMPONENT);
	}


	/**
	 *
	 * @param title Title of created document.
	 * @param text Textual content of the document.
	 * @return Created document.
	 * @throws KIMBridgeException
	 */
	public KIMBridgeDocument createDocumentFromString(String title, String text) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = kim.createDocumentFromString(text);
			if (title != null) {
				doc.setTitle(title);
			}
			return doc;
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while creating the document.", e);
		}
	}

	public KIMBridgeDocument createDocumentFromBytes(byte[] data, String extension) throws KIMBridgeException {
		try {
			return kim.createDocumentFromBinaryData(data, extension);
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while creating the document.", e);
		}
	}


	/**
	 * Extracts extension from path info.
	 * @param path Path.
	 * @return Extension of the file.
	 * @throws KIMBridgeException when the file has no extension.
	 */
	private String extractFileExtension(Path path) throws KIMBridgeException{
		String fileName = path.getFileName().toString();
		int extPos = fileName.lastIndexOf(EXT_SEPARATOR);
		if (extPos == -1) {
			throw new KIMBridgeException("Files without extension cannot be annotated.");
		}
		return fileName.substring(extPos + 1);
	}


	public KIMBridgeDocument annotateAndStoreDocument(KIMBridgeDocument document) throws KIMBridgeException {
		try {
			kim.annotateDocument(document);
			kim.storeDocument(document);
			return document;
		} catch (RemoteException e) {
			throw new KIMBridgeException("Error while annotating the document.", e);
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while setting document metadata.", e);
		} catch (DocumentRepositoryException e) {
			throw new KIMBridgeException("Error while storing document.", e);
		}
	}


	/**
	 * Reannotates the document given by its ID.
	 * @param id Document ID.
	 * @return Object of the reannotated document.
	 * @throws KIMBridgeException
	 */
	public KIMBridgeDocument reannotateDocument(long id) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = kim.getDocument(id);
			reannotateDocument(doc);
			return doc;
		} catch (KIMQueryException e) {
			throw new KIMBridgeException("Error while fetching the document.", e);
		}
	}


	/**
	 * Reannotates the given document.
	 * @param document Document to be reannotated.
	 * @throws KIMBridgeException
	 */
	public void reannotateDocument(KIMBridgeDocument document) throws KIMBridgeException {
		try {
			kim.annotateDocument(document);
			kim.synchronizeDocument(document);
		} catch (DocumentRepositoryException e) {
			throw KIMBridgeException.synchronizingDocument(e);
		} catch (RemoteException e) {
			throw KIMBridgeException.annotatingDocument(e);
		} catch (KIMCorporaException e) {
			throw KIMBridgeException.settingMetadata(e);
		}
	}


	/**
	 * Updates the document with given ID.
	 * @param id ID of document in the repository.
	 * @param updatedDocument Document containing updated contents.
	 * @throws KIMBridgeException if the document cannot be updated.
	 */
	public KIMBridgeDocument updateDocument(long id, KIMBridgeDocument updatedDocument) throws KIMBridgeException {
		try {
			KIMBridgeDocument repoDoc = kim.updateDocument(id, updatedDocument);
			reannotateDocument(repoDoc);
			return repoDoc;
		} catch (KIMCorporaException e) {
			throw KIMBridgeException.settingMetadata(e);
		} catch (KIMQueryException e) {
			throw KIMBridgeException.fetchingDocument(e);
		}
	}


	/**
	 * Registers a new document repository.
	 * @param repository Document repository instance.
	 */
	public void registerRepository(IDocumentRepository repository) throws StateRestoreException {
		repositories.add(repository);

		String repoId = repository.getId();
		repository.setLogger(loggerFactory.createLogger(formatRepositoryLogComponent(repoId)));
		logger.logMessage("Restoring %s repository state", repoId);
		repository.setState(state.restoreState(repoId));
	}

	private String formatRepositoryLogComponent(String repositoryId) {
		return String.format("Repository %s", repositoryId);
	}


	public void saveRepositoryStates() {
		logger.logMessage("Storing repository states.");
		for (IDocumentRepository repository : repositories) {
			state.storeState(repository.getId(), repository.getState());
		}
	}


	/**
	 * Checks repositories for new documents and annotates them.
	 *
	 * All synchronization errors are logged and then thrown.
	 * @throws KIMBridgeException when an synchronization error occurs.
	 */
	public void annotateNewDocuments() throws KIMBridgeException {
		logger.logMessage("Annotation of new documents has started.");
		for (IDocumentRepository repository : repositories) {
			logger.logMessage("Annotating new documents in %s repository has started.", repository.getId());
			try {
				List<IDocument> newDocuments = repository.getNewDocuments();
				annotateDocumentList(newDocuments, repository);
				logger.logMessage("Annotating new documents in %s repository has finished.", repository.getId());
				kim.synchronizeIndex(true);
				logger.logMessage("KIM index synchronized.", repository.getId());
			} catch (RepositoryException e) {
				KIMBridgeException ke = KIMBridgeException.fetchingDocuments(e);
				logger.logException(ke);
				throw ke;
			} catch (DocumentRepositoryException e) {
				KIMBridgeException ke = KIMBridgeException.synchronizingIndex(e);
				logger.logException(ke);
				throw ke;
			}
		}
		logger.logMessage("Annotation of new documents has finished.");
	}


	public void annotateDocumentList(List<IDocument> documents, IDocumentRepository repository) throws KIMBridgeException{
		for (IDocument document : documents) {
			try {
				annotateDocument(document, repository);
			} catch (KIMBridgeException e) {
				logger.logException(e);
			}
		}
	}


	public void annotateDocument(IDocument document, IDocumentRepository repository) throws KIMBridgeException {
		KIMBridgeDocument kimDoc;
		logger.logMessage("Annotating and indexing document %s", document.getTitle());
		if (document instanceof ITextDocument) {
			kimDoc = annotateTextDocument((ITextDocument) document);
		} else if (document instanceof IBinaryDocument) {
			kimDoc = annotateBinaryDocument((IBinaryDocument) document);
		} else {
			throw new KIMBridgeException("IDocument is generic interface and should not be implemented!");
		}
		repository.documentIndexed(document, kimDoc.getId());
		logger.logMessage("Document indexed with ID #%d", kimDoc.getId());
	}

	private KIMBridgeDocument annotateTextDocument(ITextDocument document) throws KIMBridgeException {
		KIMBridgeDocument doc = createDocumentFromString(document.getTitle(), document.getContents());
		doc.setUrl(document.getUrl());
		if (document.isNew()) {
			return annotateAndStoreDocument(doc);
		} else {
			return updateDocument(document.getId(), doc);
		}
	}

	private KIMBridgeDocument annotateBinaryDocument(IBinaryDocument document) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = createDocumentFromBytes(document.getData(), document.getExtension());
			doc.setTitle(document.getTitle());
			doc.setUrl(document.getUrl());
			if (document.isNew()) {
				return annotateAndStoreDocument(doc);
			} else {
				return updateDocument(document.getId(), doc);
			}
		} catch (IOException e) {
			throw new KIMBridgeException("Error while reading source data.", e);
		}
	}
}
