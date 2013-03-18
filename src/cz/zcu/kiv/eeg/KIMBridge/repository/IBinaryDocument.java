package cz.zcu.kiv.eeg.KIMBridge.repository;

import java.io.IOException;

/**
 * Binary document.
 */
public interface IBinaryDocument extends IDocument {
	public String getExtension();

	public byte[] getData() throws IOException;
}
