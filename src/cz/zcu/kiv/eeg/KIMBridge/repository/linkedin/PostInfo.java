package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;

import java.io.Serializable;

/**
 * Class representing synchronization information about LinkedIn post.
 *
 * @author Jan Smitka <jan@smitka.org>
 */
public class PostInfo implements Serializable {
	private String id;

	private long kimId;

	private long comments;


	/**
	 * Creates a new synchronization information from post.
	 * @param post Post.
	 */
	public PostInfo(Post post) {
		this(post.getId(), post.getComments().getTotal());
	}

	/**
	 * Creates a new synchronization information.
	 * @param postId ID of the post.
	 * @param commentsCount Number of comments.
	 */
	public PostInfo(String postId, long commentsCount) {
		id = postId;
		comments = commentsCount;
	}


	/**
	 * Gets the LinkedIn post ID.
	 * @return Post ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Gets the KIM document ID.
	 * @return KIM document ID.
	 */
	public long getKimDocumentId() {
		return kimId;
	}

	/**
	 * Sets the KIM document ID.
	 * @param docId KIM document ID.
	 */
	public void setKimDocumentId(long docId) {
		kimId = docId;
	}

	/**
	 * Checks if the number of comments has increased.
	 * @param commentCount Number of comments.
	 * @return {@code true} if number of comments has increased.
	 */
	public boolean hasNewComments(long commentCount) {
		return (commentCount > comments);
	}

	/**
	 * Checks whether the updated post contains new comments.
	 * @param newPost Updated post.
	 * @return {@code true} if the post has new comments.
	 */
	public boolean hasNewComments(Post newPost) {
		return hasNewComments(newPost.getComments().getTotal());
	}

	/**
	 * Sets the current number of comments from updated post information.
	 * @param newPost Updated post.
	 */
	public void setCommentCountFromPost(Post newPost) {
		setCommentCount(newPost.getComments().getTotal());
	}

	/**
	 * Sets the current number of comments.
	 * @param newCommentCount Number of comments.
	 */
	public void setCommentCount(long newCommentCount) {
		comments = newCommentCount;
	}
}
