package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Comment;
import com.google.code.linkedinapi.schema.Post;
import cz.zcu.kiv.eeg.KIMBridge.repository.ITextDocument;

import java.util.Date;

/**
 * LinkedIn discussion thread.
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInDocument implements ITextDocument {
	private Post postData;

	private PostInfo postInfo;

	private boolean newDoc = true;

	/**
	 * Creates a new document from given discussion post.
	 * @param post
	 */
	public LinkedInDocument(Post post) {
		postData = post;
	}

	/**
	 * Gets the document ID in the KIM Platform for discussion updates.
	 * @return KIM document ID.
	 */
	@Override
	public long getId() {
		return postInfo.getKimDocumentId();
	}

	/**
	 * Is this a new discussion?
	 * @return {@code true} if this discussion is new, {@code false} if it is scheduled for update.
	 */
	@Override
	public boolean isNew() {
		return newDoc;
	}

	/**
	 * Sets the new discussion flag.
	 * @param isNew Is this a new discussion?
	 */
	public void setNew(boolean isNew) {
		newDoc = isNew;
	}

	/**
	 * Sets the synchronization information.
	 * @param info Post information.
	 */
	public void setPostInfo(PostInfo info) {
		postInfo = info;
	}

	/**
	 * Gets the discussion synchronization information.
	 * @return Post information.
	 */
	public PostInfo getPostInfo() {
		return postInfo;
	}

	/**
	 * Gets the contents of the discussion and all comments.
	 * @return Discussion contents.
	 */
	@Override
	public String getContents() {
		return String.format("%s%n%s%n%n%s", postData.getTitle(), postData.getSummary() == null ? "" : postData.getSummary(), getAllComments());
	}

	/**
	 * Gets the title of the discussion.
	 * @return Title.
	 */
	@Override
	public String getTitle() {
		return postData.getTitle();
	}

	/**
	 * Gets the URL of the discussion.
	 * @return URL.
	 */
	@Override
	public String getUrl() {
		return postData.getSiteGroupPostUrl();
	}

	/**
	 * Formats the list of all comments.
	 * @return String containing all comments.
	 */
	private String getAllComments() {
		StringBuilder str = new StringBuilder();
		for (Comment comment : postData.getComments().getCommentList()) {
			str.append("-- ");
			str.append(comment.getCreator().getFirstName());
			str.append(" ");
			str.append(comment.getCreator().getLastName());
			str.append(" (");
			str.append(String.format("%tc", new Date(comment.getCreationTimestamp())));
			str.append(")\n");
			str.append(comment.getText());
			str.append("\n--\n\n");
		}
		return str.toString();
	}
}
