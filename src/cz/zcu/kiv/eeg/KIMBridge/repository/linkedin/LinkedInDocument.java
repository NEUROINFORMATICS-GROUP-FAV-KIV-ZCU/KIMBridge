package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;
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
		return String.format("%s%n%s", postData.getTitle(), postData.getSummary());
	}

	@Override
	public String getTitle() {
		return null;
	}
}
