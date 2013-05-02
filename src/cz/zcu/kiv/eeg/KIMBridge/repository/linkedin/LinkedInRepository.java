package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import com.google.code.linkedinapi.schema.Post;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * LinkedIn discussions repository. Each repository downloads discussions from specified group.
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

	/**
	 * Creates a new repository.
	 * @param repoId Repository ID.
	 * @param connector LinkedIn connector.
	 * @param groupId Group ID.
	 */
	public LinkedInRepository(String repoId, LinkedInConnector connector, String groupId) {
		id = repoId;
		linkedIn = connector;
		group = groupId;
		queue = new PostQueue(QUEUE_LIMIT);
	}

	/**
	 * Sets the logger.
	 * @param logger Logger.
	 */
	@Override
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	/**
	 * Gets the repository ID.
	 * @return Repository ID.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Gets the repository synchronization state.
	 * @return Synchronization state.
	 */
	@Override
	public IRepositoryState getState() {
		return new LinkedInRepositoryState(lastCheck, lastRecheck, queue.getInternalCollection());
	}

	/**
	 * Restores the repository synchronization state.
	 * @param state Synchronization state of the repository.
	 * @throws StateRestoreException when the repository state cannot be restored.
	 */
	@Override
	public void setState(IRepositoryState state) throws StateRestoreException {
		if (state instanceof LinkedInRepositoryState) {
			LinkedInRepositoryState liState = (LinkedInRepositoryState) state;
			lastCheck = liState.getLastCheck();
			lastRecheck = liState.getLastRecheck();
			queue.setInternalCollection(liState.getPosts());
		}
	}

	/**
	 * Called after the document has been indexed. Stores KIM document ID for later updates.
	 * @param document Document.
	 * @param kimId KIM document ID.
	 */
	@Override
	public void documentIndexed(IDocument document, long kimId) {
		LinkedInDocument doc = (LinkedInDocument) document;
		doc.getPostInfo().setKimDocumentId(kimId);
	}

	/**
	 * Creates a new document from given post.
	 * @param post Post.
	 * @return LinkedIn document.
	 */
	private LinkedInDocument createDocument(Post post) {
		return new LinkedInDocument(post);
	}

	/**
	 * Creates a new document from given post and synchronization information.
	 * @param post Post.
	 * @param postInfo Synchronization information.
	 * @return LinkedIn document.
	 */
	private LinkedInDocument createDocument(Post post, PostInfo postInfo) {
		LinkedInDocument document = createDocument(post);
		document.setPostInfo(postInfo);
		return document;
	}

	/**
	 * Gets the list of new documents.
	 * @return List of new documents.
	 * @throws RepositoryException when the documents cannot be fetched.
	 */
	@Override
	public List<IDocument> getNewDocuments() throws RepositoryException {
		logger.logMessage("Fetching and updating posts.");
		List<IDocument> documents = getUpdatedPosts();
		documents.addAll(getNewPosts());
		queue.cropQueue();
		logger.logMessage("Fetching and updating posts done. %d new or updated posts in total.", documents.size());
		return documents;
	}

	/**
	 * Gets the list of new discussions since last synchronization.
	 * @return List of new discussions.
	 */
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


	/**
	 * Checks for new comments in tracked discussions.
	 * @return List of documents containing discussions with new comments.
	 */
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


	/**
	 * Checks specified post for new comments.
	 * @param postInfo Synchronization information.
	 * @return New document when there are new comments available or {@code null}.
	 */
	private IDocument checkPost(PostInfo postInfo) {
		try {
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
		} catch (Exception e) {
			// post probably not found, non fatal error
			return null;
		}
	}

}
