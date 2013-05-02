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
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMBridge {
	private static final char EXT_SEPARATOR = '.';
	private static final String LOG_COMPONENT = "KIMBridge";

	private SyncStatePersister state;

	private KIMConnector kim;

	private List<IDocumentRepository> repositories;

	private ILoggerFactory loggerFactory;

	private ILogger logger;


	/**
	 * Initializes KIMBridge.
	 * @param loggerFactory Factory for creating per-repository loggers.
	 * @param connector Connector to KIM.
	 * @param syncState Synchronization state persister.
	 */
	public KIMBridge(ILoggerFactory loggerFactory, KIMConnector connector, SyncStatePersister syncState) {
		kim = connector;
		state = syncState;
		repositories = new LinkedList<IDocumentRepository>();
		this.loggerFactory = loggerFactory;
		logger = loggerFactory.createLogger(LOG_COMPONENT);
	}


	/**
	 * Creates a new plaintext document from string.
	 *
	 * @param text Textual content of the document.
	 * @return Created document.
	 * @throws KIMBridgeException when the document could not be created.
	 */
	public KIMBridgeDocument createDocumentFromString(String text) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = kim.createDocumentFromString(text);
			return doc;
		} catch (KIMCorporaException|KIMConnectionException e) {
			throw new KIMBridgeException("Error while creating the document.", e);
		}
	}

	/**
	 * Creates a new document from binary data.
	 * @param data Binary data.
	 * @param extension Original file extension.
	 * @return Created document.
	 * @throws KIMBridgeException when the document could not be created.
	 */
	public KIMBridgeDocument createDocumentFromBytes(byte[] data, String extension) throws KIMBridgeException {
		try {
			return kim.createDocumentFromBinaryData(data, extension);
		} catch (KIMCorporaException|KIMConnectionException e) {
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


	/**
	 * Annotates and stores document in KIM document repository.
	 * @param document Document.
	 * @return Annotated document.
	 * @throws KIMBridgeException when the document could not be annotated or stored.
	 */
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
		} catch (KIMConnectionException e) {
			throw new KIMBridgeException("Connection to KIM lost.", e);
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
		} catch (KIMQueryException|KIMConnectionException e) {
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
		} catch (RemoteException|KIMConnectionException e) {
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
		} catch (KIMQueryException|KIMConnectionException e) {
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
		repository.setState(state.getState(repoId));
	}

	/**
	 * Formats log component string for given repository.
	 * @param repositoryId Repository ID.
	 * @return Formatted log component name.
	 */
	private String formatRepositoryLogComponent(String repositoryId) {
		return String.format("Repository %s", repositoryId);
	}


	/**
	 * Stores the repository states to the sync state persister. Does not write the data to output file.
	 */
	public void saveRepositoryStates() {
		logger.logMessage("Storing repository states.");
		for (IDocumentRepository repository : repositories) {
			state.putState(repository.getId(), repository.getState());
		}
	}

	/**
	 * Synchronizes the persisted repository states to the binary file.
	 *
	 * Should be called after {@code saveRepositoryStates()}.
	 */
	public void persistStates() throws KIMBridgeException {
		try {
			state.save();
		} catch (IOException e) {
			logger.logException(e);
			throw KIMBridgeException.persistingStates(e);
		}
	}


	/**
	 * Checks repositories for new documents and annotates them.
	 *
	 * All synchronization errors are logged and then thrown.
	 * @throws KIMBridgeException when an synchronization error occurs.
	 */
	public void annotateNewDocuments() throws KIMBridgeException {
		if (!kim.isConnected()) {
			try {
				kim.connect();
			} catch (KIMConnectionException e) {
				logger.logMessage("KIM is not connected. Will retry at next document check.");
				return;
			}
		}

		logger.logMessage("Annotation of new documents has started.");
		for (IDocumentRepository repository : repositories) {
			logger.logMessage("Annotating new documents in %s repository has started.", repository.getId());
			try {
				List<IDocument> newDocuments = repository.getNewDocuments();
				annotateDocumentList(newDocuments, repository);
				logger.logMessage("Annotating new documents in %s repository has finished.", repository.getId());
				kim.synchronizeIndex(true);
				logger.logMessage("KIM index synchronized.", repository.getId());
			} catch (KIMConnectionException e) {
				logger.logMessage("KIM disconnected.");
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


	/**
	 * Annotates and stores all documents from remote repository in specified list.
	 *
	 * All annotation errors are logged and not thrown.
	 * @param documents List of documents from remote repository.
	 * @param repository Originating repository.
	 */
	private void annotateDocumentList(List<IDocument> documents, IDocumentRepository repository) {
		for (IDocument document : documents) {
			try {
				annotateDocument(document, repository);
			} catch (KIMBridgeException e) {
				logger.logException(e);
			}
		}
	}


	/**
	 * Annotates and stores single document from remote repository.
	 * @param document Document from remote repository.
	 * @param repository Originating repository.
	 * @throws KIMBridgeException
	 */
	private void annotateDocument(IDocument document, IDocumentRepository repository) throws KIMBridgeException {
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

	/**
	 * Annotates and stores single text document. Allows document updates.
	 * @param document Text document to be stored or updated.
	 * @return Annotated document.
	 * @throws KIMBridgeException when the document cannot be annotated or stored.
	 */
	private KIMBridgeDocument annotateTextDocument(ITextDocument document) throws KIMBridgeException {
		KIMBridgeDocument kimDoc = createDocumentFromString(document.getContents());
		return annotateKIMDocument(document, kimDoc);
	}

	/**
	 * Annotates and stores single binary document. Allows document updates.
	 * @param document Binary document to be stored or updated.
	 * @return Annotated document.
	 * @throws KIMBridgeException when the document cannot be annotated or stored.
	 */
	private KIMBridgeDocument annotateBinaryDocument(IBinaryDocument document) throws KIMBridgeException {
		try {
			KIMBridgeDocument kimDoc = createDocumentFromBytes(document.getData(), document.getExtension());
			return annotateKIMDocument(document, kimDoc);
		} catch (IOException e) {
			throw new KIMBridgeException("Error while reading source data.", e);
		}
	}

	/**
	 * Annotates and stores already created KIM document with specified document features.
	 *
	 * Allows document updates.
	 * @param document Document from remote repository containing document features and optionally information about
	 *                 original document.
	 * @param doc KIM document.
	 * @return Annotated document.
	 * @throws KIMBridgeException
	 */
	private KIMBridgeDocument annotateKIMDocument(IDocument document, KIMBridgeDocument doc) throws KIMBridgeException {
		doc.setTitle(document.getTitle());
		doc.setUrl(document.getUrl());
		if (document.isNew()) {
			return annotateAndStoreDocument(doc);
		} else {
			return updateDocument(document.getId(), doc);
		}
	}
}
