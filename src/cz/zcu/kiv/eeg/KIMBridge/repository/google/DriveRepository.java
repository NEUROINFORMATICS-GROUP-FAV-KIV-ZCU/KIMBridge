package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocument;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;
import cz.zcu.kiv.eeg.KIMBridge.repository.RepositoryException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DriveRepository implements IDocumentRepository {
	private DriveConnector drive;

	private BigInteger lastChangeId = null;

	private Set<String> allowedMimeTypes;

	public DriveRepository(DriveConnector driveConnector) {
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
	public IRepositoryState getState() {
		return new DriveRepositoryState(lastChangeId);
	}

	@Override
	public void setState(IRepositoryState state) {
		if (state instanceof  DriveRepositoryState) {
			DriveRepositoryState drvState = (DriveRepositoryState) state;
			lastChangeId = drvState.getLastChangeId();
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
