package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;

import java.io.Serializable;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class PostInfo implements Serializable {

	private String id;

	private long kimId;

	private long comments;



	public PostInfo(Post post) {
		id = post.getId();
		comments = post.getComments().getTotal();
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


	public boolean hasNewComments(Post newPost) {
		return (newPost.getComments().getTotal() > comments);
	}

	public void setCommentCountFromPost(Post newPost) {
		comments = newPost.getComments().getTotal();
	}
}
