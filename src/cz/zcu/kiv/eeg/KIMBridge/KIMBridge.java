package cz.zcu.kiv.eeg.KIMBridge;

import com.ontotext.kim.client.corpora.KIMCorporaException;
import com.ontotext.kim.client.documentrepository.DocumentRepositoryException;
import com.ontotext.kim.client.query.KIMQueryException;
import cz.zcu.kiv.eeg.KIMBridge.repository.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

/**
 * Main KIMBridge class.
 */
public class KIMBridge {
	private static final char EXT_SEPARATOR = '.';

	private SyncStatePersister state;

	private KIMConnector kim;

	private List<IDocumentRepository> repositories;


	public KIMBridge(KIMConnector connector, SyncStatePersister syncState) {
		kim = connector;
		state = syncState;
		repositories = new LinkedList<IDocumentRepository>();
	}


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
	 *
	 * @param title Title of created document.
	 * @param text Textual content of the document.
	 * @return Annotated an stored document.
	 * @throws KIMBridgeException if the document could not be annotated or stored.
	 */
	public KIMBridgeDocument annotateString(String title, String text) throws KIMBridgeException {
		KIMBridgeDocument doc = createDocumentFromString(title, text);
		return annotateAndStoreDocument(doc);
	}


	/**
	 * Annotates and stores a locally stored file.
	 * @param file File to allocate.
	 * @return
	 * @throws IOException
	 * @throws KIMBridgeException
	 */
	public KIMBridgeDocument annotateLocalFile(File file) throws IOException, KIMBridgeException {
		Path path = Paths.get(file.getPath());
		byte[] data = Files.readAllBytes(path);
		return annotateBytes(data, extractFileExtension(path));
	}


	/**
	 *
	 * @param path
	 * @return
	 * @throws KIMBridgeException
	 */
	private String extractFileExtension(Path path) throws KIMBridgeException{
		String fileName = path.getFileName().toString();
		int extPos = fileName.lastIndexOf(EXT_SEPARATOR);
		if (extPos == -1) {
			throw new KIMBridgeException("Files without extension cannot be annotated.");
		}
		return fileName.substring(extPos + 1);
	}


	public KIMBridgeDocument annotateBytes(byte[] data, String extension) throws KIMBridgeException {
		KIMBridgeDocument doc = createDocumentFromBytes(data, extension);
		return annotateAndStoreDocument(doc);
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

		repository.setState(state.restoreState(repository.getId()));
	}


	public void saveRepositoryStates() {
		for (IDocumentRepository repository : repositories) {
			state.storeState(repository.getId(), repository.getState());
		}
	}


	public void annotateNewDocuments() throws KIMBridgeException {
		for (IDocumentRepository repository : repositories) {
			try {
				List<IDocument> newDocuments = repository.getNewDocuments();
				annotateDocumentList(newDocuments, repository);
				kim.synchronizeIndex(true);
			} catch (RepositoryException e) {
				throw KIMBridgeException.fetchingDocuments(e);
			} catch (DocumentRepositoryException e) {
				throw KIMBridgeException.synchronizingIndex(e);
			}
		}
	}


	public void annotateDocumentList(List<IDocument> documents, IDocumentRepository repository) throws KIMBridgeException{
		for (IDocument document : documents) {
			try {
				annotateDocument(document, repository);
			} catch (KIMBridgeException e) {
				System.err.println(e.toString());
			}
		}
	}


	public void annotateDocument(IDocument document, IDocumentRepository repository) throws KIMBridgeException {
		KIMBridgeDocument kimDoc;
		if (document instanceof ITextDocument) {
			kimDoc = annotateTextDocument((ITextDocument) document);
		} else if (document instanceof IBinaryDocument) {
			kimDoc = annotateBinaryDocument((IBinaryDocument) document);
		} else {
			throw new KIMBridgeException("IDocument is generic interface and should not be implemented!");
		}
		repository.documentIndexed(document, kimDoc.getId());
	}

	private KIMBridgeDocument annotateTextDocument(ITextDocument document) throws KIMBridgeException {
		if (document.isNew()) {
			return annotateString(document.getTitle(), document.getContents());
		} else {
			KIMBridgeDocument newDoc = createDocumentFromString(document.getTitle(), document.getContents());
			return updateDocument(document.getId(), newDoc);
		}
	}

	private KIMBridgeDocument annotateBinaryDocument(IBinaryDocument document) throws KIMBridgeException {
		try {
			if (document.isNew()) {
				return annotateBytes(document.getData(), document.getExtension());
			} else {
				KIMBridgeDocument newDoc = createDocumentFromBytes(document.getData(), document.getExtension());
				return updateDocument(document.getId(), newDoc);
			}
		} catch (IOException e) {
			throw new KIMBridgeException("Error while reading source data.", e);
		}
	}
}
