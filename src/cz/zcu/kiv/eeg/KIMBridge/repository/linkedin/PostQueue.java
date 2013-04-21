package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;

import java.util.ArrayDeque;

/**
 * Tracked discussions synchronization queue.
 *
 * Contains predefined number of discussions. New discussions are added to the top, updated discussions are moved to the top,
 * discussions from the bottom of the queue are removed by the {@code cropQueue()} method when they overflow the queue capacity.
 *
 * @author Jan Smitka <jan@smitka.org>
 */
public class PostQueue {
	private int maxItems;

	private ArrayDeque<PostInfo> posts;


	/**
	 * Create a new queue.
	 * @param maxPosts Maximum number of tracked discussions.
	 */
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

	/**
	 * Gets the internal mutable collection of tracked discussions.
	 *
	 * This method is intended to be used only when storing the repository state.
	 * @return Collection of discussions.
	 */
	public ArrayDeque<PostInfo> getInternalCollection() {
		return posts;
	}

	/**
	 * Sets the internal collection of tracked discussions.
	 *
	 * This method is intended to by used only when restoring the repository state.
	 * @param postsColl New collection of discussions.
	 */
	public void setInternalCollection(ArrayDeque<PostInfo> postsColl) {
		posts = postsColl;
	}


	/**
	 * Adds a new post to the top of the queue.
	 * @param post New post.
	 * @return Post synchronization information.
	 */
	public PostInfo addNewPost(Post post) {
		PostInfo postInfo = new PostInfo(post);
		return addNewPost(postInfo);
	}

	/**
	 * Adds a new post synchronization information to the queue.
	 * @param postInfo New post synchronization information.
	 * @return {@code PostInfo} instance that was passed in arguments.
	 */
	public PostInfo addNewPost(PostInfo postInfo) {
		posts.addLast(postInfo);
		return postInfo;
	}

	/**
	 * Moves the specified post to the top of the queue. Number of comments must be altered manually.
	 * @param post Post synchronization information.
	 */
	public void postUpdated(PostInfo post) {
		posts.remove(post);
		posts.addLast(post);
	}

	/**
	 * Removes discussions that overflow the queue capacity.
	 */
	public void cropQueue() {
		int overlap = posts.size() - maxItems;
		if (overlap > 0) {
			for (int i = 0; i < overlap; i++) {
				posts.removeFirst();
			}
		}
	}


}
