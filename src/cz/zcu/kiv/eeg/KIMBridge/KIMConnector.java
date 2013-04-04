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

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for KIM API.
 */
public class KIMConnector {
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 1099;

	private KIMService kimService;

	private CorporaAPI corpora;

	private DocumentRepositoryAPI docRepository;

	private SemanticAnnotationAPI semanticAnnotation;

	public KIMConnector() throws RemoteException {
		this(DEFAULT_HOST);
	}

	public KIMConnector(String host) throws RemoteException {
		this(host, DEFAULT_PORT);
	}

	public KIMConnector(String host, int port) throws RemoteException {
		kimService = GetService.from(host, port);
		corpora = kimService.getCorporaAPI();
		docRepository = kimService.getDocumentRepositoryAPI();
		semanticAnnotation = kimService.getSemanticAnnotationAPI();
	}

	public List<Long> listDocumentIds() throws KIMQueryException {
		DocumentQuery query = new DocumentQuery();
		List<Long> result = new LinkedList<>();
		DocumentQueryResult res = docRepository.getDocumentIds(query);
		for (DocumentQueryResultRow row : res) {
			result.add(row.getDocumentId());
		}
		return result;
	}

	public KIMBridgeDocument getDocument(long id) throws KIMQueryException {
		return new KIMBridgeDocument(docRepository.loadDocument(id));
	}

	public Map<String, Object> getDocumentMetadata(long id) {
		return docRepository.getDocumentFeatures(id);
	}

	public KIMBridgeDocument createDocumentFromString(String text) throws KIMCorporaException {
		return createDocumentFromString(text, true);
	}

	public KIMBridgeDocument createDocumentFromString(String text, boolean noMarkup) throws KIMCorporaException {
		return new KIMBridgeDocument(corpora.createDocument(text, noMarkup));
	}

	public KIMBridgeDocument createDocumentFromBinaryData(byte[] bytes, String extension) throws KIMCorporaException {
		return new KIMBridgeDocument(corpora.createDocument(bytes, extension));
	}

	public void annotateDocument(KIMBridgeDocument document) throws RemoteException, KIMCorporaException {
		semanticAnnotation.execute(document.getDocument());
	}

	public void storeDocument(KIMBridgeDocument document) throws DocumentRepositoryException, KIMCorporaException {
		docRepository.addDocument(document.getDocument());
	}

	public void synchronizeDocument(KIMBridgeDocument document) throws DocumentRepositoryException, KIMCorporaException {
		docRepository.syncDocument(document.getDocument());
	}

	public void removeDocument(KIMBridgeDocument document) throws DocumentRepositoryException {
		removeDocument(document.getId());
	}

	public void removeDocument(long id) throws DocumentRepositoryException {
		docRepository.deleteDocument(id);
	}


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
