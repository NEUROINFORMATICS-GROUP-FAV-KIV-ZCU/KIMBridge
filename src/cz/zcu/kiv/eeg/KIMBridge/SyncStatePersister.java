package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class SyncStatePersister {
	public static final String LOG_COMPONENT = "SyncState";

	private File storageFile;

	private Map<String, IRepositoryState> states;

	private ILogger logger;

	public SyncStatePersister(ILogger logger, File syncFile) {
		states = new TreeMap<>();
		storageFile = syncFile;
		this.logger = logger;
	}

	public void load() throws IOException, ClassNotFoundException {
		if (storageFile.exists()) {
			logger.logMessage("Loading %s", storageFile.getPath());
			ObjectInputStream is = openInputStream();
			states = (Map<String, IRepositoryState>) is.readObject();
			is.close();
		}
	}

	public IRepositoryState restoreState(String repositoryId) {
		return states.get(repositoryId);
	}


	public void storeState(String repositoryId, IRepositoryState repoState) {
		states.put(repositoryId, repoState);
	}

	public void save() throws IOException {
		logger.logMessage("Writing %s", storageFile.getPath());
		ObjectOutputStream os = openOutputStream();
		os.writeObject(states);
		os.close();
	}


	private ObjectInputStream openInputStream() throws IOException {
		FileInputStream fs = new FileInputStream(storageFile);
		BufferedInputStream bfs = new BufferedInputStream(fs);
		return new ObjectInputStream(bfs);
	}

	private ObjectOutputStream openOutputStream() throws IOException {
		FileOutputStream fs = new FileOutputStream(storageFile);
		BufferedOutputStream bfs = new BufferedOutputStream(fs);
		return new ObjectOutputStream(bfs);
	}
}
