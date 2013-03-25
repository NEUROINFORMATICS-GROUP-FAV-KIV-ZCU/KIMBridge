package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;
import com.google.code.linkedinapi.schema.Comment;
import cz.zcu.kiv.eeg.KIMBridge.repository.ITextDocument;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInDocument implements ITextDocument {
	private Post postData;

	public LinkedInDocument(Post post) {
		postData = post;
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
			str.append(comment.getCreator().getFirstName());
			str.append(" ");
			str.append(comment.getCreator().getLastName());
			str.append("\n");
			str.append(comment.getText());
			str.append("\n\n");
		}
		return str.toString();
	}
}
