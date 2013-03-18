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

	private Configurator config;

	private SyncState state;

	private KIMConnector kim;

	private List<IDocumentRepository> repositories;


	public KIMBridge(Configurator configurator, SyncState syncState) {
		config = configurator;
		state = syncState;
		repositories = new LinkedList<IDocumentRepository>();
	}


	public Configurator getConfigurator() {
		return config;
	}


	/**
	 * Connects to the KIM Platform.
	 * @throws KIMBridgeException if the connection could not be established.
	 */
	public void connect() throws KIMBridgeException {
		try {
			kim = new KIMConnector();
		} catch (RemoteException e) {
			throw new KIMBridgeException("Error while connecting.", e);
		}
	}

	/**
	 *
	 * @param title Title of created document.
	 * @param string Textual content of the document.
	 * @return Annotated an stored document.
	 * @throws KIMBridgeException if the document could not be annotated or stored.
	 */
	public KIMBridgeDocument annotateString(String title, String string) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = kim.createDocumentFromString(string);
			if (title != null) {
				doc.setTitle(title);
			}
			return annotateAndStoreDocument(doc);
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while creating the document.", e);
		}
	}


	public KIMBridgeDocument annotateLocalFile(File file) throws IOException, KIMBridgeException {
		Path path = Paths.get(file.getPath());
		byte[] data = Files.readAllBytes(path);
		return annotateBytes(data, extractFileExtension(path));
	}



	private String extractFileExtension(Path path) throws KIMBridgeException{
		String fileName = path.getFileName().toString();
		int extPos = fileName.lastIndexOf(EXT_SEPARATOR);
		if (extPos == -1) {
			throw new KIMBridgeException("Files without extension cannot be annotated.");
		}
		return fileName.substring(extPos + 1);
	}


	public KIMBridgeDocument annotateBytes(byte[] data, String extension) throws KIMBridgeException {
		try {
			KIMBridgeDocument doc = kim.createDocumentFromBinaryData(data, extension);
			return annotateAndStoreDocument(doc);
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while creating the document.", e);
		}
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
			throw new KIMBridgeException("Error while syncing the document.", e);
		} catch (RemoteException e) {
			throw new KIMBridgeException("Error while annotating the document.", e);
		} catch (KIMCorporaException e) {
			throw new KIMBridgeException("Error while setting document metadata.", e);
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


	public void annotateAllDocuments() throws KIMBridgeException {
		for (IDocumentRepository repository : repositories) {
			try {
				List<IDocument> documents = repository.getAllDocuments();
				annotateDocumentList(documents);
			} catch (RepositoryException e) {
				throw new KIMBridgeException("Error while fetching documents.", e);
			}
		}
	}


	public void annotateNewDocuments() throws KIMBridgeException {
		for (IDocumentRepository repository : repositories) {
			try {
				List<IDocument> newDocuments = repository.getNewDocuments();
				annotateDocumentList(newDocuments);
			} catch (RepositoryException e) {
				throw new KIMBridgeException("Error while fetching new documents.", e);
			}
		}
	}


	public void annotateDocumentList(List<IDocument> documents) throws KIMBridgeException{
		for (IDocument document : documents) {
			try {
				annotateDocument(document);
			} catch (KIMBridgeException e) {
				System.err.println(e.toString());
			}
		}
	}


	public void annotateDocument(IDocument document) throws KIMBridgeException {
		if (document instanceof ITextDocument) {
			annotateTextDocument((ITextDocument) document);
		} else if (document instanceof IBinaryDocument) {
			annotateBinaryDocument((IBinaryDocument) document);
		} else {
			throw new KIMBridgeException("IDocument is generic interface and should not be implemented!");
		}
	}

	private void annotateTextDocument(ITextDocument document) throws KIMBridgeException {
		annotateString(document.getTitle(), document.getContents());
	}

	private void annotateBinaryDocument(IBinaryDocument document) throws KIMBridgeException {
		try {
			annotateBytes(document.getData(), document.getExtension());
		} catch (IOException e) {
			throw new KIMBridgeException("", e);
		}
	}
}
