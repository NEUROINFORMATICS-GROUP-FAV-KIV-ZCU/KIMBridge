package cz.zcu.kiv.eeg.KIMBridge.repository.google;

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.model.File;
import cz.zcu.kiv.eeg.KIMBridge.connectors.google.DriveConnector;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
import cz.zcu.kiv.eeg.KIMBridge.repository.IBinaryDocument;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class DriveDocument implements IBinaryDocument {
	private DriveConnector drive;

	private File file;

	private ILogger logger;

	public DriveDocument(DriveConnector connector, File fileMetadata) {
		drive = connector;
		file = fileMetadata;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public boolean isNew() {
		return true;
	}

	@Override
	public String getTitle() {
		return file.getOriginalFilename();
	}

	@Override
	public String getUrl() {
		return file.getAlternateLink();
	}

	@Override
	public String getExtension() {
		return file.getFileExtension();
	}

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
