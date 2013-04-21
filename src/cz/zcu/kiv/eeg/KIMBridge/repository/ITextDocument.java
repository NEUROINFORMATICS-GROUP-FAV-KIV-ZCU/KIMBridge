package cz.zcu.kiv.eeg.KIMBridge.repository;

/**
 * Text document.
 */
public interface ITextDocument extends IDocument {
	/**
	 * Gets the document content.
	 * @return Plaintext.
	 */
	public String getContents();
}
