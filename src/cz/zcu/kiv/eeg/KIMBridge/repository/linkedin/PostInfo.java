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



	public PostInfo(Post post) {
		this(post.getId(), post.getComments().getTotal());
	}

	public PostInfo(String postId, long commentsCount) {
		id = postId;
		comments = commentsCount;
	}


	public String getId() {
		return id;
	}


	public long getKimDocumentId() {
		return kimId;
	}

	public void setKimDocumentId(long docId) {
		kimId = docId;
	}

	public boolean hasNewComments(long postCount) {
		return (postCount > comments);
	}

	public boolean hasNewComments(Post newPost) {
		return hasNewComments(newPost.getComments().getTotal());
	}

	public void setCommentCountFromPost(Post newPost) {
		setCommentCount(newPost.getComments().getTotal());
	}

	public void setCommentCount(long newCommentCount) {
		comments = newCommentCount;
	}
}
