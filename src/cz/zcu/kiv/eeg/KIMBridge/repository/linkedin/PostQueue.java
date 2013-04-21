package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;

import java.util.ArrayDeque;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class PostQueue {
	private int maxItems;

	private ArrayDeque<PostInfo> posts;



	public PostQueue(int maxPosts) {
		maxItems = maxPosts;
		posts = new ArrayDeque<>();
	}


	/**
	 * Return immutable collection of items currently in queue.
	 * @return Collection of items in queue.
	 */
	public PostInfo[] getItems() {
		return posts.toArray(new PostInfo[posts.size()]);
	}


	public ArrayDeque<PostInfo> getInternalCollection() {
		return posts;
	}

	public void setInternalCollection(ArrayDeque<PostInfo> postsColl) {
		posts = postsColl;
	}


	public PostInfo addNewPost(Post post) {
		PostInfo postInfo = new PostInfo(post);
		return addNewPost(postInfo);
	}

	public PostInfo addNewPost(PostInfo postInfo) {
		posts.addLast(postInfo);
		return postInfo;
	}


	public void postUpdated(PostInfo post) {
		posts.remove(post);
		posts.addLast(post);
	}


	public void cropQueue() {
		int overlap = posts.size() - maxItems;
		if (overlap > 0) {
			for (int i = 0; i < overlap; i++) {
				posts.removeFirst();
			}
		}
	}


}
