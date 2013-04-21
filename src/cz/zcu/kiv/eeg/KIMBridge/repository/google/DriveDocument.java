package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.model.File;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.IBinaryDocument;

import java.io.IOException;
import java.io.InputStream;

/**
 * Document stored in Google Drive. Does not support document updates.
 *
 * Document data is downloaded each time the {@code getData()} method is invoked.
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveDocument implements IBinaryDocument {
	private DriveConnector drive;

	private File file;

	private ILogger logger;

	/**
	 * Creates the document from Google Drive File metadata.
	 * @param connector Google Drive connector.
	 * @param fileMetadata File metadata.
	 */
	public DriveDocument(DriveConnector connector, File fileMetadata) {
		drive = connector;
		file = fileMetadata;
	}

	/**
	 * Sets the logger.
	 * @param logger Logger.
	 */
	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	/**
	 * Gets the random dummy document ID.
	 * @return See {@code http://xkcd.com/221/}.
	 */
	@Override
	public long getId() {
		return 4;
	}

	/**
	 * Is this a new document?
	 * @return Always {@code true}.
	 */
	@Override
	public boolean isNew() {
		return true;
	}

	/**
	 * Gets the document title.
	 * @return Original filename.
	 */
	@Override
	public String getTitle() {
		return file.getOriginalFilename();
	}

	/**
	 * Gets the document URL.
	 * @return URL in Google Drive.
	 */
	@Override
	public String getUrl() {
		return file.getAlternateLink();
	}

	/**
	 * Gets the document extension.
	 * @return Document file extension.
	 */
	@Override
	public String getExtension() {
		return file.getFileExtension();
	}

	/**
	 * Downloads the document data.
	 * @return Raw file data.
	 * @throws IOException when the data cannot be downloaded.
	 */
	@Override
	public byte[] getData() throws IOException {
		logger.logMessage("Downloading %s", file.getOriginalFilename());
		HttpResponse response = drive.sendDownloadFileRequest(file);
		int length = response.getHeaders().getContentLength().intValue();
		int offset = 0;
		byte[] data = new byte[length];
		InputStream stream = response.getContent();
		while (offset < length) {
			offset += stream.read(data, offset, length - offset);
		}
		stream.close();
		response.disconnect();
		logger.logMessage("%d bytes downloaded", length);
		return data;
	}
}
