package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryState;

import java.util.ArrayDeque;
import java.util.Date;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepositoryState implements IRepositoryState {
	private Date lastCheck;

	private Date lastRecheck;

	private ArrayDeque<PostInfo> posts;

	public LinkedInRepositoryState(Date lastCheck, Date lastRecheck, ArrayDeque<PostInfo> posts) {
		this.lastCheck = lastCheck;
		this.lastRecheck = lastRecheck;
		this.posts = posts;
	}

	public Date getLastCheck() {
		return lastCheck;
	}

	public Date getLastRecheck() {
		return lastRecheck;
	}

	public ArrayDeque<PostInfo> getPosts() {
		return posts;
	}
}
