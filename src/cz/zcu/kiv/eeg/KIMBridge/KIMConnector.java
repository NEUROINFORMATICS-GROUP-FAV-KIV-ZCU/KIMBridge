package cz.zcu.kiv.eeg.KIMBridge;

import com.ontotext.kim.client.GetService;
import com.ontotext.kim.client.KIMService;
import com.ontotext.kim.client.corpora.CorporaAPI;
import com.ontotext.kim.client.corpora.KIMCorporaException;
import com.ontotext.kim.client.documentrepository.DocumentQuery;
import com.ontotext.kim.client.documentrepository.DocumentRepositoryAPI;
import com.ontotext.kim.client.documentrepository.DocumentRepositoryException;
import com.ontotext.kim.client.query.DocumentQueryResult;
import com.ontotext.kim.client.query.DocumentQueryResultRow;
import com.ontotext.kim.client.query.KIMQueryException;
import com.ontotext.kim.client.semanticannotation.SemanticAnnotationAPI;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for KIM API.
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMConnector {
	/** Name of the component in the log. */
	public static final String LOG_COMPONENT = "KIMConnector";

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 1099;

	private ILogger logger;

	private String kimHost;

	private int kimPort;

	private KIMService kimService;

	private CorporaAPI corpora;

	private DocumentRepositoryAPI docRepository;

	private SemanticAnnotationAPI semanticAnnotation;

	/**
	 * Initializes the connector to default host (localhost) and port (1099).
	 * @param logger Logger.
	 */
	public KIMConnector(ILogger logger) {
		this(logger, DEFAULT_HOST);
	}

	/**
	 * Initializes the connector to specified host and default port (1099).
	 * @param logger Logger.
	 * @param host Remote host.
	 */
	public KIMConnector(ILogger logger, String host) {
		this(logger, host, DEFAULT_PORT);
	}

	/**
	 * Initializes the connector to specified host and port.
	 * @param logger Logger.
	 * @param host Remote host.
	 * @param port Remote port.
	 */
	public KIMConnector(ILogger logger, String host, int port) {
		kimHost = host;
		kimPort = port;
		this.logger = logger;
	}

	/**
	 * Connects to the KIMPlatform and initializes services.
	 * @throws RemoteException when the connection fails.
	 */
	public void connect() throws RemoteException {
		logger.logMessage("Connecting to the KIM Platform.");
		kimService = GetService.from(kimHost, kimPort);
		corpora = kimService.getCorporaAPI();
		docRepository = kimService.getDocumentRepositoryAPI();
		semanticAnnotation = kimService.getSemanticAnnotationAPI();
	}

	/**
	 * Gets ID of all documents stored in the document repository.
	 * @return List of document IDs.
	 * @throws KIMQueryException when the document query fails.
	 */
	public List<Long> listDocumentIds() throws KIMQueryException {
		DocumentQuery query = new DocumentQuery();
		List<Long> result = new LinkedList<>();
		DocumentQueryResult res = docRepository.getDocumentIds(query);
		for (DocumentQueryResultRow row : res) {
			result.add(row.getDocumentId());
		}
		return result;
	}

	/**
	 * Fetches the specified document from remote repository.
	 * @param id Document ID.
	 * @return Document data.
	 * @throws KIMQueryException when the document could not be fetched.
	 */
	public KIMBridgeDocument getDocument(long id) throws KIMQueryException {
		return new KIMBridgeDocument(docRepository.loadDocument(id));
	}

	/**
	 * Fetches features of the document with given ID.
	 * @param id Document ID.
	 * @return Map with document features.
	 */
	public Map<String, Object> getDocumentMetadata(long id) {
		return docRepository.getDocumentFeatures(id);
	}

	/**
	 * Creates plaintext document from string.
	 * @param text Textual content of the document.
	 * @return Created KIM document.
	 * @throws KIMCorporaException when the document could not be created.
	 */
	public KIMBridgeDocument createDocumentFromString(String text) throws KIMCorporaException {
		return createDocumentFromString(text, true);
	}

	/**
	 * Creates plaintext or HTML document from string.
	 * @param text Textual or HTML content of the document.
	 * @param noMarkup {@code true} for plaintext document, or {@code false} for (X)HTML document.
	 * @return Created KIM document.
	 * @throws KIMCorporaException when the document could not be created.
	 */
	public KIMBridgeDocument createDocumentFromString(String text, boolean noMarkup) throws KIMCorporaException {
		return new KIMBridgeDocument(corpora.createDocument(text, noMarkup));
	}

	/**
	 * Creates a document from binary data. Extension of the file is required.
	 * @param bytes Binary data of the document.
	 * @param extension Extension of the original file, e.g. pdf, doc.
	 * @return Created KIM document.
	 * @throws KIMCorporaException when the document could not be created.
	 */
	public KIMBridgeDocument createDocumentFromBinaryData(byte[] bytes, String extension) throws KIMCorporaException {
		return new KIMBridgeDocument(corpora.createDocument(bytes, extension));
	}

	/**
	 * Annotates the document through the KIM pipeline.
	 * @param document Document.
	 * @throws RemoteException when the annotation service fails.
	 * @throws KIMCorporaException when the document features could not be annotated.
	 */
	public void annotateDocument(KIMBridgeDocument document) throws RemoteException, KIMCorporaException {
		semanticAnnotation.execute(document.getDocument());
	}

	/**
	 * Stores the document in the repository. Document has to be annotated before it is stored.
	 * @param document Document.
	 * @throws DocumentRepositoryException when the document could not be stored.
	 * @throws KIMCorporaException when the document features could not be stored.
	 */
	public void storeDocument(KIMBridgeDocument document) throws DocumentRepositoryException, KIMCorporaException {
		docRepository.addDocument(document.getDocument());
	}

	/**
	 * Synchronizes document content and features with document repository.
	 * @param document Document to be synchronized.
	 * @throws DocumentRepositoryException when the document could not be synchronized.
	 * @throws KIMCorporaException when the document features could not be synchronized.
	 */
	public void synchronizeDocument(KIMBridgeDocument document) throws DocumentRepositoryException, KIMCorporaException {
		docRepository.syncDocument(document.getDocument());
	}

	/**
	 * Removes the specified document from the document repository.
	 * @param document Document to be removed.
	 * @throws DocumentRepositoryException when the specified document could not be removed.
	 */
	public void removeDocument(KIMBridgeDocument document) throws DocumentRepositoryException {
		removeDocument(document.getId());
	}

	/**
	 * Removes document with specified ID from the repository.
	 * @param id Document ID.
	 * @throws DocumentRepositoryException when the document could not be deleted.
	 */
	public void removeDocument(long id) throws DocumentRepositoryException {
		docRepository.deleteDocument(id);
	}


	/**
	 * Updates document in the repository.
	 * @param id ID of the document.
	 * @param updatedDocument Document with new content.
	 * @return Updated document.
	 * @throws KIMQueryException when the document could not be fetched.
	 * @throws KIMCorporaException when the new document data could not be prepared.
	 */
	public KIMBridgeDocument updateDocument(long id, KIMBridgeDocument updatedDocument) throws KIMQueryException, KIMCorporaException {
		KIMBridgeDocument repositoryDocument = getDocument(id);
		repositoryDocument.copyContentFrom(updatedDocument);
		return repositoryDocument;
	}


	/**
	 * Synchronizes the search index.
	 * @param force Forces immediate synchronization.
	 * @throws DocumentRepositoryException when the index cannot be synchronized.
	 */
	public void synchronizeIndex(boolean force) throws DocumentRepositoryException {
		docRepository.synchronizeIndex(force);
	}

	/**
	 * Immediately synchronizes the search index.
	 * @throws DocumentRepositoryException when the index cannot be synchronized.
	 */
	public void synchonizeIndex() throws DocumentRepositoryException {
		synchronizeIndex(true);
	}
}
