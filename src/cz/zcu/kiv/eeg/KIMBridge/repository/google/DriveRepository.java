package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
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
 * Google Drive document repository.
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepository implements IDocumentRepository {
	private String id;

	private DriveConnector drive;

	/** ID of last downloaded change. */
	private BigInteger lastChangeId = null;

	private Set<String> allowedMimeTypes;

	private ILogger logger;

	/**
	 * Initializes the repository.
	 * @param driveConnector Google Drive connector.
	 */
	public DriveRepository(String repoId, DriveConnector driveConnector) {
		id = repoId;
		drive = driveConnector;
		allowedMimeTypes = createDefaultAllowedMimeTypes();
	}

	/**
	 * Sets the repository logger.
	 * @param logger Logger.
	 */
	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	/**
	 * Called when the document is indexed. No action is taken since there is no need for document updates.
	 * @param document Document.
	 * @param kimId ID of the document in KIM Platform.
	 */
	@Override
	public void documentIndexed(IDocument document, long kimId) {
		// no action is required
	}

	/**
	 * Initializes the set of allowed mime types.
	 * @return Set of allowed mime-types.
	 */
	private Set<String> createDefaultAllowedMimeTypes() {
		Set<String> set = new HashSet<>();
		set.add("application/pdf");
		return set;
	}

	/**
	 * Gets the internal repository ID.
	 * @return Repository ID.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Gets the synchronization state of the repository.
	 * @return Synchronization state containing ID of last change.
	 */
	@Override
	public IRepositoryState getState() {
		return new DriveRepositoryState(lastChangeId);
	}

	/**
	 * Restores the repository synchronization state.
	 */
	@Override
	public void setState(IRepositoryState state) {
		if (state instanceof  DriveRepositoryState) {
			DriveRepositoryState drvState = (DriveRepositoryState) state;
			lastChangeId = drvState.getLastChangeId();
		}
	}

	/**
	 * Fetches the list of new documents stored in Google Drive.
	 *
	 * Changes are fetched from all folders and documents shared with the service account.
	 * @return List of new documents.
	 * @throws RepositoryException when the documents cannot be fetched.
	 */
	@Override
	public List<IDocument> getNewDocuments() throws RepositoryException {
		try {
			List<IDocument> documents = new LinkedList<>();
			if (lastChangeId == null) {
				logger.logMessage("Downloading list of all changes.");
			} else {
				logger.logMessage("Downloading list of all changes since change #%d.", lastChangeId.longValue());
			}
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
			logger.logMessage("List of changes downloaded: %d new documents.", documents.size());
			return documents;
		} catch (IOException e) {
			throw new RepositoryException(e);
		}
	}

	/**
	 * Creates a document from Google Drive metadata. Documents are created only non-trashed from files with accepted mime-types
	 * that can be downloaded.
	 * @param file File metadata.
	 * @return New document instance, or {@code null} if the document does not have accepted mime-type.
	 */
	private DriveDocument createDocument(File file) {
		if (!fileIsTrashed(file) && mimeTypeIsAllowed(file.getMimeType()) && fileIsDownloadable(file)) {
			DriveDocument doc = new DriveDocument(drive, file);
			doc.setLogger(logger);
			return doc;
		} else {
			return null;
		}
	}

	/**
	 * Checks whether the file is trashed.
	 * @param file File metadata.
	 * @return {@code true} if the file is trashed.
	 */
	private boolean fileIsTrashed(File file) {
		return file.getLabels().getTrashed();
	}

	/**
	 * Checks whether the mime-type is allowed.
	 * @param mimeType Mime-Type.
	 * @return {@code true} if the mime-type is listed as allowed.
	 */
	private boolean mimeTypeIsAllowed(String mimeType) {
		return allowedMimeTypes.contains(mimeType);
	}

	/**
	 * Checks whether the file can be downloaded.
	 * @param file File metadata.
	 * @return {@code true} if the file can be downloaded.
	 */
	private boolean fileIsDownloadable(File file) {
		return (file.getDownloadUrl() != null);
	}
}
