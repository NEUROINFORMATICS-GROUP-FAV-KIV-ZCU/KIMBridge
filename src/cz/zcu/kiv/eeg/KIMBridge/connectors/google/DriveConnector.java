package cz.zcu.kiv.eeg.KIMBridge.connectors.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

/**
 * Google Drive connector.
 */
public class DriveConnector {
	private static final String KEY_SERVICE_ACCOUNT_ID = "accountId";
	private static final String KEY_SERVICE_PRIVATE_KEY = "privateKey";
	private static final String KEY_APP_NAME = "appName";

	private Drive drive;


	public DriveConnector(FactoryConfiguration configuration) throws GeneralSecurityException, IOException, ConfigurationException {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();

		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(jsonFactory)
				.setServiceAccountId(configuration.getProperty(KEY_SERVICE_ACCOUNT_ID))
				.setServiceAccountPrivateKeyFromP12File(new java.io.File(configuration.getProperty(KEY_SERVICE_PRIVATE_KEY)))
				.setServiceAccountScopes(DriveScopes.DRIVE_READONLY)
				.build();

		drive = new Drive.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName(configuration.getProperty(KEY_APP_NAME))
				.build();
	}


	public FileList listFilesInFolder(String folderId) throws IOException {
		return drive.files().list().setQ(createFolderFilesQuery(folderId)).execute();
	}

	public ChangeList listChanges(BigInteger startChangeId) throws IOException {
		Drive.Changes.List list = drive.changes().list();
		if (startChangeId != null) {
			// startChangeId is inclusive, so we want to skip the first item
			list.setStartChangeId(getNextChangeId(startChangeId));
		}
		return list.execute();
	}

	private BigInteger getNextChangeId(BigInteger startChangeId) {
		return startChangeId.add(BigInteger.valueOf(1));
	}

	private String createFolderFilesQuery(String folderId) {
		return String.format("'%s' in parents", folderId);
	}

	public HttpResponse sendDownloadFileRequest(File file) throws IOException {
		GenericUrl downloadUrl = new GenericUrl(file.getDownloadUrl());
		return drive.getRequestFactory().buildGetRequest(downloadUrl).execute();
	}
}
