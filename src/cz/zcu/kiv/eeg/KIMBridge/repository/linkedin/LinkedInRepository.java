package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepository implements IDocumentRepository {
	/** Recheck interval. Default: 12h */
	private static final long RECHECK_INTERVAL = 43200000L;

	/** Maximum count of posts to be kept in queue for recheck. */
	private static final int QUEUE_LIMIT = 25;

	private ILogger logger;

	private String id;

	private LinkedInConnector linkedIn;

	private String group;

	private Date lastCheck = null;

	private Date lastRecheck = null;

	private PostQueue queue;

	public LinkedInRepository(String repoId, LinkedInConnector connector, String groupId) {
		id = repoId;
		linkedIn = connector;
		group = groupId;
		queue = new PostQueue(QUEUE_LIMIT);
	}

	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IRepositoryState getState() {
		return new LinkedInRepositoryState(lastCheck, lastRecheck, queue.getInternalCollection());
	}

	@Override
	public void setState(IRepositoryState state) throws StateRestoreException {
		if (state instanceof LinkedInRepositoryState) {
			LinkedInRepositoryState liState = (LinkedInRepositoryState) state;
			lastCheck = liState.getLastCheck();
			lastRecheck = liState.getLastRecheck();
			queue.setInternalCollection(liState.getPosts());
		}
	}


	@Override
	public void documentIndexed(IDocument document, long kimId) {
		LinkedInDocument doc = (LinkedInDocument) document;
		doc.getPostInfo().setKimDocumentId(kimId);
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
		logger.logMessage("Fetching and updating posts.");
		List<IDocument> documents = getUpdatedPosts();
		documents.addAll(getNewPosts());
		queue.cropQueue();
		logger.logMessage("Fetching and updating posts done. %d new or updated posts in total.", documents.size());
		return documents;
	}

	private List<IDocument> getNewPosts() {
		List<Post> posts;
		if (lastCheck == null) {
			logger.logMessage("Fetching last 10 posts.");
			posts = linkedIn.getGroupPosts(group);
		} else {
			logger.logMessage("Fetching new posts since %tc.", lastCheck);
			posts = linkedIn.getGroupPosts(group, lastCheck);
		}

		List<IDocument> documents = new LinkedList<>();

		for (Post post : posts) {
			PostInfo info = queue.addNewPost(post);
			LinkedInDocument document = createDocument(post, info);
			documents.add(document);
		}
		lastCheck = new Date();
		logger.logMessage("New posts fetched: %d new posts.", documents.size());
		return documents;
	}


	private List<IDocument> getUpdatedPosts() {
		List<IDocument> documents = new LinkedList<>();

		Date now = new Date();
		Date checkInterval = new Date(now.getTime() - RECHECK_INTERVAL);
		if (lastRecheck == null || lastRecheck.before(checkInterval)) {
			lastRecheck = now;

			int count = 0;
			logger.logMessage("Fetching new comments in tracked posts.");
			PostInfo[] posts = queue.getItems();
			for (PostInfo post : posts) {
				logger.logMessage("Fetching comments for post #%s", post.getId());
				IDocument doc = checkPost(post);
				if (doc != null) {
					count++;
					documents.add(doc);
				}
			}
			logger.logMessage("Found new comments in %s posts.", count);
		} else {
			logger.logMessage("Tracked posts not checked: last check was at %tc", lastRecheck);
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
