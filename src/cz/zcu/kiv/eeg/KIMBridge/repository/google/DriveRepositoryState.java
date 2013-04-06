package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.math.BigInteger;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveRepositoryState implements IRepositoryState {
	private BigInteger lastChangeId;

	public DriveRepositoryState(BigInteger lastChangeId) {
		this.lastChangeId = lastChangeId;
	}

	public BigInteger getLastChangeId() {
		return lastChangeId;
	}
}
