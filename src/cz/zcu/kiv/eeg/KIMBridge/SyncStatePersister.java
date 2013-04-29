package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Persists synchronization state of repositories.
 * @author Jan Smitka <jan@smitka.org>
 */
public class SyncStatePersister {
	/** Name of the component in the log. */
	public static final String LOG_COMPONENT = "SyncState";

	private File storageFile;

	private Map<String, IRepositoryState> states;

	private ILogger logger;

	/**
	 * Initializes the persister.
	 * @param logger Logger.
	 * @param syncFile Synchronization file with binary data.
	 */
	public SyncStatePersister(ILogger logger, File syncFile) {
		states = new TreeMap<>();
		storageFile = syncFile;
		this.logger = logger;
	}

	/**
	 * Loads synchronization data.
	 * @throws IOException when the file cannot be read.
	 * @throws ClassNotFoundException when one of the classes in the binary file does not exist.
	 */
	public void load() throws IOException, ClassNotFoundException {
		if (storageFile.exists()) {
			logger.logMessage("Loading %s", storageFile.getPath());
			ObjectInputStream is = openInputStream();
			states = (Map<String, IRepositoryState>) is.readObject();
			is.close();
		} else {
			logger.logMessage("File %s does not exist, repository states are null.", storageFile.getPath());
		}
	}

	/**
	 * Retrieves synchronization state for repository.
	 * @param repositoryId Internal ID of repository.
	 * @return Synchronization state or {@code null}.
	 */
	public IRepositoryState getState(String repositoryId) {
		return states.get(repositoryId);
	}

	/**
	 * Puts synchronization state into storage. No data is written, states are saved by calling {@code save}.
	 * @param repositoryId Internal ID of repository.
	 * @param repoState Repository state.
	 */
	public void putState(String repositoryId, IRepositoryState repoState) {
		states.put(repositoryId, repoState);
	}

	/**
	 * Writes the synchronization states to output file.
	 * @throws IOException when the file cannot be written.
	 */
	public void save() throws IOException {
		logger.logMessage("Writing %s", storageFile.getPath());
		ObjectOutputStream os = openOutputStream();
		os.writeObject(states);
		os.close();
	}

	/**
	 * Opens file for reading.
	 * @return Input stream.
	 * @throws IOException when the file cannot be opened for reading.
	 */
	private ObjectInputStream openInputStream() throws IOException {
		FileInputStream fs = new FileInputStream(storageFile);
		BufferedInputStream bfs = new BufferedInputStream(fs);
		return new ObjectInputStream(bfs);
	}

	/**
	 * Opens file for writing.
	 * @return Output stream.
	 * @throws IOException when the file cannot be opened for writing.
	 */
	private ObjectOutputStream openOutputStream() throws IOException {
		FileOutputStream fs = new FileOutputStream(storageFile);
		BufferedOutputStream bfs = new BufferedOutputStream(fs);
		return new ObjectOutputStream(bfs);
	}
}
