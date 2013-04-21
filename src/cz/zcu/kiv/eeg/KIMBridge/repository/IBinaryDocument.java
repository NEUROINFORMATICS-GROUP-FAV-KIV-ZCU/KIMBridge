package cz.zcu.kiv.eeg.KIMBridge.repository;

import java.io.IOException;

/**
 * Binary document.
 */
public interface IBinaryDocument extends IDocument {
	/**
	 * Gets the file extension. Required for processing in KIM.
	 * @return Extension of the file.
	 */
	public String getExtension();

	/**
	 * Gets the binary data.
	 * @return Bytes with raw data.
	 * @throws IOException when the data cannot be read.
	 */
	public byte[] getData() throws IOException;
}
