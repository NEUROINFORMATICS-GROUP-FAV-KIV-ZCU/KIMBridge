package cz.zcu.kiv.eeg.KIMBridge.repository;

/**
 * Interface for all repository documents. Must not be implemented!
 *
 * Implement descendant interfaces instead.
 *
 * @see IBinaryDocument
 * @see ITextDocument
 */
public interface IDocument {
	public long getId();

	public boolean isNew();

	public String getTitle();
}
