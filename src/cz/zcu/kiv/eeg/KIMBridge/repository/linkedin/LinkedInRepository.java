package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocument;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.RepositoryException;
import cz.zcu.kiv.eeg.KIMBridge.repository.StateRestoreException;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepository implements IDocumentRepository {
	private static final String LAST_CHECK_KEY = "lastCheck";

	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);

	private LinkedInConnector linkedIn;

	private String group;

	private Date lastCheck = null;

	public LinkedInRepository(LinkedInConnector connector, String groupId) {
		linkedIn = connector;
		group = groupId;
	}

	@Override
	public String getId() {
		return "LinkedIn";
	}

	@Override
	public Map<String, String> getState() {
		Map<String, String> state = new HashMap<>();
		if (lastCheck != null) {
			state.put(LAST_CHECK_KEY, dateFormat.format(lastCheck));
		}
		return state;
	}

	@Override
	public void setState(Map<String, String> state) throws StateRestoreException {
		try {
			if (state.containsKey(LAST_CHECK_KEY)) {
				lastCheck = dateFormat.parse(state.get(LAST_CHECK_KEY));
			}
		} catch (ParseException e) {
			throw new StateRestoreException("Error while parsing last checked date.", e);
		}
	}

	@Override
	public List<IDocument> getAllDocuments() throws RepositoryException {
		List<Post> posts = linkedIn.getGroupPosts(group);
		List<IDocument> documents = new LinkedList<IDocument>();
		for (Post post : posts) {
			documents.add(createDocument(post));
		}
		return documents;
	}

	private LinkedInDocument createDocument(Post post) {
		return new LinkedInDocument(post);
	}

	@Override
	public List<IDocument> getNewDocuments() throws RepositoryException {
		List<Post> posts;
		if (lastCheck == null) {
			posts = linkedIn.getGroupPosts(group);
		} else {
			posts = linkedIn.getGroupPosts(group, lastCheck);
		}

		List<IDocument> documents = new LinkedList<IDocument>();

		for (Post post : posts) {
			documents.add(createDocument(post));
		}
		lastCheck = new Date();
		return documents;
	}
}
