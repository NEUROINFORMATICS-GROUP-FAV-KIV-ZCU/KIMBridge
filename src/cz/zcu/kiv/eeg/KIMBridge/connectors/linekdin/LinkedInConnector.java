package cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.PostField;
import com.google.code.linkedinapi.client.oauth.LinkedInApiConsumer;
import com.google.code.linkedinapi.schema.Post;
import com.google.code.linkedinapi.schema.Posts;
import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LinkedIn connector.
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInConnector {
	private static final String KEY_CONSUMER_KEY = "consumerKey";
	private static final String KEY_CONSUMER_SECRET = "consumerSecret";
	private static final String KEY_TOKEN = "token";
	private static final String KEY_TOKEN_SECRET = "tokenSecret";

	/** Number of downloaded post. */
	private static final int POST_COUNT = 10;

	private LinkedInApiClient client;

	/**
	 * Initializes the connector.
	 * @param configuration Connector configuration.
	 * @throws ConfigurationException when the configuration does not contain required values.
	 */
	public LinkedInConnector(FactoryConfiguration configuration) throws ConfigurationException {
		LinkedInApiConsumer consumer = new LinkedInApiConsumer(
				configuration.getProperty(KEY_CONSUMER_KEY),
				configuration.getProperty(KEY_CONSUMER_SECRET)
		);

		LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(consumer);
		client = clientFactory.createLinkedInApiClient(
				configuration.getProperty(KEY_TOKEN),
				configuration.getProperty(KEY_TOKEN_SECRET)
		);
	}


	/**
	 * Gets last 10 posts in the given group.
	 * @param groupId Group ID.
	 * @return List of posts.
	 */
	public List<Post> getGroupPosts(String groupId) {
		Set<PostField> fields = createPostFields();
		Posts posts = client.getPostsByGroup(groupId, fields, 0, POST_COUNT);
		return posts.getPostList();
	}

	/**
	 * Gets the last 10 posts in the given group that have been created after given date and time.
	 * @param groupId Group ID.
	 * @param modifiedSince Date and time.
	 * @return List of posts.
	 */
	public List<Post> getGroupPosts(String groupId, Date modifiedSince) {
		Set<PostField> fields = createPostFields();
		Posts posts = client.getPostsByGroup(groupId, fields, 0, POST_COUNT, modifiedSince);
		return posts.getPostList();
	}

	/**
	 * Creates a set of downloaded post fields.
	 * @return Set of post fields.
	 */
	private Set<PostField> createPostFields() {
		Set<PostField> fields = new HashSet<>();
		fields.add(PostField.ID);
		fields.add(PostField.SITE_GROUP_POST_URL);
		fields.add(PostField.TITLE);
		fields.add(PostField.CREATOR);
		fields.add(PostField.SUMMARY);
		fields.add(PostField.COMMENTS);
		fields.add(PostField.CREATION_TIMESTAMP);
		return fields;
	}

	/**
	 * Fetches post with given ID.
	 * @param postId ID of the post.
	 * @return Post.
	 */
	public Post getPost(String postId) {
		return client.getPost(postId, createPostFields());
	}
}
