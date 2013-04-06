package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class SyncState {
	private static final String SYNC_COMMENT = "Saved repository synchronization state.";

	private File storageFile;

	private Map<String, IRepositoryState> states;

	public SyncState(File syncFile) {
		states = new TreeMap<>();
		storageFile = syncFile;
	}

	public void load() throws IOException, ClassNotFoundException {
		ObjectInputStream is = openInputStream();
		states = (Map<String, IRepositoryState>) is.readObject();
		is.close();
	}

	public void storeState(String repositoryId, IRepositoryState repoState) {
		states.put(repositoryId, repoState);
	}


	public IRepositoryState restoreState(String repositoryId) {
		return states.get(repositoryId);
	}


	public void save() throws IOException {
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
