package cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.PostField;
import com.google.code.linkedinapi.client.oauth.LinkedInApiConsumer;
import com.google.code.linkedinapi.schema.Post;
import com.google.code.linkedinapi.schema.Posts;
import cz.zcu.kiv.eeg.KIMBridge.Configurator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInConnector {
	private static final String CONFIG_PREFIX = "linkedin";
	private static final String KEY_CONSUMER_KEY = "consumerKey";
	private static final String KEY_CONSUMER_SECRET = "consumerSecret";
	private static final String KEY_TOKEN = "token";
	private static final String KEY_TOKEN_SECRET = "tokenSecret";

	private static final int POST_COUNT = 10;

	private LinkedInApiClient client;

	public LinkedInConnector(Configurator configurator) {
		LinkedInApiConsumer consumer = new LinkedInApiConsumer(
				configurator.getPrefixed(CONFIG_PREFIX, KEY_CONSUMER_KEY),
				configurator.getPrefixed(CONFIG_PREFIX, KEY_CONSUMER_SECRET)
		);

		LinkedInApiClientFactory clientFactory = LinkedInApiClientFactory.newInstance(consumer);
		client = clientFactory.createLinkedInApiClient(
				configurator.getPrefixed(CONFIG_PREFIX, KEY_TOKEN),
				configurator.getPrefixed(CONFIG_PREFIX, KEY_TOKEN_SECRET)
		);
	}


	public List<Post> getGroupPosts(String groupId) {
		Set<PostField> fields = createPostFields();
		Posts posts = client.getPostsByGroup(groupId, fields, 0, POST_COUNT);
		return posts.getPostList();
	}

	public List<Post> getGroupPosts(String groupId, Date modifiedSince) {
		Set<PostField> fields = createPostFields();
		Posts posts = client.getPostsByGroup(groupId, fields, 0, POST_COUNT, modifiedSince);
		return posts.getPostList();
	}

	private Set<PostField> createPostFields() {
		Set<PostField> fields = new HashSet<>();
		fields.add(PostField.ID);
		fields.add(PostField.TITLE);
		fields.add(PostField.CREATOR);
		fields.add(PostField.SUMMARY);
		fields.add(PostField.COMMENTS);
		fields.add(PostField.CREATION_TIMESTAMP);
		return fields;
	}


	public Post getPost(String postId) {
		return client.getPost(postId, createPostFields());
	}
}
