package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.util.ArrayDeque;
import java.util.Date;

/**
 * LinkedIn repository synchronization state.
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepositoryState implements IRepositoryState {
	private Date lastCheck;

	private Date lastRecheck;

	private ArrayDeque<PostInfo> posts;

	/**
	 * Creates a new synchronization state object.
	 * @param lastCheck Date of last check for new discussions.
	 * @param lastRecheck Date of last check for new comments for tracked discussions.
	 * @param posts Queue of the tracked discussions.
	 */
	public LinkedInRepositoryState(Date lastCheck, Date lastRecheck, ArrayDeque<PostInfo> posts) {
		this.lastCheck = lastCheck;
		this.lastRecheck = lastRecheck;
		this.posts = posts;
	}

	/**
	 * Gets the date of last check for new discussions.
	 * @return Date of last check for new discussions.
	 */
	public Date getLastCheck() {
		return lastCheck;
	}

	/**
	 * Gets the date of last check for new comments for tracked discussions.
	 * @return Date of last check for new comments for tracked discussions.
	 */
	public Date getLastRecheck() {
		return lastRecheck;
	}

	/**
	 * Gets the queue of tracked discussions.
	 * @return Queue of tracked discussions.
	 */
	public ArrayDeque<PostInfo> getPosts() {
		return posts;
	}
}
