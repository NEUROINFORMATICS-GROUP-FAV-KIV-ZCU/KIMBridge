package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.math.BigInteger;

/**
 * Google Drive repository synchronization state. Contains last change ID.
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryState implements IRepositoryState {
	private BigInteger lastChangeId;

	/**
	 * Creates a new synchronization state object.
	 * @param lastChangeId Last change ID.
	 */
	public DriveRepositoryState(BigInteger lastChangeId) {
		this.lastChangeId = lastChangeId;
	}

	/**
	 * Gets the last change ID.
	 * @return ID of the last change.
	 */
	public BigInteger getLastChangeId() {
		return lastChangeId;
	}
}
