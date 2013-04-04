package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Comment;
import com.google.code.linkedinapi.schema.Post;
import cz.zcu.kiv.eeg.KIMBridge.repository.ITextDocument;

import java.util.Date;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInDocument implements ITextDocument {
	private Post postData;

	private PostInfo postInfo;

	private boolean newDoc = true;

	public LinkedInDocument(Post post) {
		postData = post;
	}

	@Override
	public long getId() {
		return postInfo.getKimDocumentId();
	}

	@Override
	public boolean isNew() {
		return newDoc;
	}


	public void setNew(boolean isNew) {
		newDoc = isNew;
	}

	public void setPostInfo(PostInfo info) {
		postInfo = info;
	}


	public PostInfo getPostInfo() {
		return postInfo;
	}


	@Override
	public String getContents() {
		return String.format("%s%n%s%n%n%s", postData.getTitle(), postData.getSummary(), getAllComments());
	}

	@Override
	public String getTitle() {
		return null;
	}

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
