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

	private static final String LAST_RECHECK_KEY = "lastRecheck";

	/** Recheck interval. Default: 12h */
	private static final long RECHECK_INTERVAL = 43200000L;

	/** Maximum count of posts to be kept in queue for recheck. */
	private static final int QUEUE_LIMIT = 25;

	private DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);

	private LinkedInConnector linkedIn;

	private String group;

	private Date lastCheck = null;

	private Date lastRecheck = null;

	private PostQueue queue;

	public LinkedInRepository(LinkedInConnector connector, String groupId) {
		linkedIn = connector;
		group = groupId;
		queue = new PostQueue(QUEUE_LIMIT);
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
	public void documentIndexed(IDocument document, long kimId) {
		LinkedInDocument doc = (LinkedInDocument) document;
		doc.getPostInfo().setKimDocumentId(kimId);
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

	private LinkedInDocument createDocument(Post post, PostInfo postInfo) {
		LinkedInDocument document = createDocument(post);
		document.setPostInfo(postInfo);
		return document;
	}

	@Override
	public List<IDocument> getNewDocuments() throws RepositoryException {
		List<IDocument> documents = getUpdatedPosts();
		documents.addAll(getNewPosts());
		queue.cropQueue();
		return documents;
	}

	private List<IDocument> getNewPosts() {
		List<Post> posts;
		if (lastCheck == null) {
			posts = linkedIn.getGroupPosts(group);
		} else {
			posts = linkedIn.getGroupPosts(group, lastCheck);
		}

		List<IDocument> documents = new LinkedList<>();

		for (Post post : posts) {
			PostInfo info = queue.addNewPost(post);
			LinkedInDocument document = createDocument(post, info);
			documents.add(document);
		}
		lastCheck = new Date();
		return documents;
	}


	private List<IDocument> getUpdatedPosts() {
		List<IDocument> documents = new LinkedList<>();

		Date now = new Date();
		Date checkInterval = new Date(now.getTime() - RECHECK_INTERVAL);
		if (lastRecheck == null || lastRecheck.before(checkInterval)) {
			lastRecheck = now;

			PostInfo[] posts = queue.getItems();
			for (PostInfo post : posts) {
				IDocument doc = checkPost(post);
				if (doc != null) {
					documents.add(doc);
				}
			}
		}

		return documents;
	}


	private IDocument checkPost(PostInfo postInfo) {
		Post post = linkedIn.getPost(postInfo.getId());

		if (postInfo.hasNewComments(post)) {
			postInfo.setCommentCountFromPost(post);
			queue.postUpdated(postInfo);
			LinkedInDocument doc = createDocument(post, postInfo);
			doc.setNew(false);
			return doc;
		} else {
			return null;
		}
	}

}
