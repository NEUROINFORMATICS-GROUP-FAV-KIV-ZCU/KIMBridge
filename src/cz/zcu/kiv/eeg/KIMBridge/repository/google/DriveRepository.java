package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocument;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.RepositoryException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 *
 */
public class DriveRepository implements IDocumentRepository {
	private static final String LAST_CHANGE_ID_KEY = "lastChangeId";

	private DriveConnector drive;
	private String folderId;

	private BigInteger lastChangeId = null;

	private Set<String> allowedMimeTypes;

	public DriveRepository(DriveConnector driveConnector, String folder) {
		folderId = folder;
		drive = driveConnector;

		allowedMimeTypes = createDefaultAllowedMimeTypes();
	}

	@Override
	public void documentIndexed(IDocument document, long kimId) {
		// no action is required
	}

	private Set<String> createDefaultAllowedMimeTypes() {
		Set<String> set = new HashSet<>();
		set.add("application/pdf");
		return set;
	}


	@Override
	public String getId() {
		return "GoogleDrive";
	}

	@Override
	public Map<String, String> getState() {
		Map<String, String> state = new HashMap<>();
		if (lastChangeId != null) {
			state.put(LAST_CHANGE_ID_KEY, lastChangeId.toString());
		}
		return state;
	}

	@Override
	public void setState(Map<String, String> state) {
		if (state.containsKey(LAST_CHANGE_ID_KEY)) {
			lastChangeId = new BigInteger(state.get(LAST_CHANGE_ID_KEY));
		}
	}

	@Override
	public List<IDocument> getAllDocuments() throws RepositoryException {
		try {
			List<IDocument> documents = new LinkedList<IDocument>();
			FileList files = drive.listFilesInFolder(folderId);
			for (File file : files.getItems()) {
				DriveDocument document = createDocument(file);
				if (document != null) {
					documents.add(document);
				}
			}
			return documents;
		} catch (IOException e) {
			throw new RepositoryException();
		}
	}

	@Override
	public List<IDocument> getNewDocuments() throws RepositoryException {
		try {
			List<IDocument> documents = new LinkedList<>();
			ChangeList changes = drive.listChanges(lastChangeId);
			for (Change change : changes.getItems()) {
				if (!change.getDeleted()) {
					DriveDocument document = createDocument(change.getFile());
					if (document != null) {
						documents.add(document);
					}
				}
			}
			if (changes.getLargestChangeId() != null) {
				lastChangeId = changes.getLargestChangeId();
			}
			return documents;
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}


	private DriveDocument createDocument(File file) {
		if (!fileIsTrashed(file) && mimeTypeIsAllowed(file.getMimeType()) && fileIsDownloadable(file)) {
			return new DriveDocument(drive, file);
		} else {
			return null;
		}
	}

	private boolean fileIsTrashed(File file) {
		return file.getLabels().getTrashed();
	}

	private boolean mimeTypeIsAllowed(String mimeType) {
		return allowedMimeTypes.contains(mimeType);
	}

	private boolean fileIsDownloadable(File file) {
		return (file.getDownloadUrl() != null);
	}
}
