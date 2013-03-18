package cz.zcu.kiv.eeg.KIMBridge;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class SyncState {
	private static final String SYNC_COMMENT = "Saved repository synchronization state.";

	private File storageFile;

	private Properties state;

	public SyncState(File syncFile) {
		state = new Properties();
		storageFile = syncFile;
	}

	public void load() throws IOException {
		if (storageFile.exists()) {
			InputStream is = openInputStream();
			state.load(is);
			is.close();
		}
	}

	public void storeState(String repositoryId, Map<String, String> repoState) {
		for (Map.Entry<String, String> entry : repoState.entrySet()) {
			state.setProperty(prefixKey(entry.getKey(), repositoryId), entry.getValue());
		}
	}


	public Map<String, String> restoreState(String repositoryId) {
		Map<String, String> repoState = new HashMap<>();
		for (String key : state.stringPropertyNames()) {
			if (hasRepositoryPrefix(key, repositoryId)) {
				repoState.put(removePrefix(key, repositoryId), state.getProperty(key));
			}
		}
		return repoState;
	}


	public void save() throws IOException {
		OutputStream os = openOutputStream();
		state.store(os, SYNC_COMMENT);
		os.close();
	}


	private InputStream openInputStream() throws FileNotFoundException {
		FileInputStream fs = new FileInputStream(storageFile);
		return new BufferedInputStream(fs);
	}

	private OutputStream openOutputStream() throws FileNotFoundException {
		FileOutputStream fs = new FileOutputStream(storageFile);
		return new BufferedOutputStream(fs);
	}

	private boolean hasRepositoryPrefix(String key, String repositoryId) {
		return key.startsWith(repositoryId) && key.charAt(repositoryId.length()) == '.';
	}

	private String prefixKey(String key, String repositoryId) {
		return String.format("%s.%s", repositoryId, key);
	}

	private String removePrefix(String key, String repositoryId) {
		return key.substring(repositoryId.length() + 1);
	}
}
