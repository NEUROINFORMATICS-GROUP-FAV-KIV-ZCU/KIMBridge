package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;

import java.util.TimerTask;

/**
 * Indexing task for the scheduler. Triggers annotation of new documents in the KIMBridge.
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMIndexTask extends TimerTask {
	private KIMBridge kimBridge;
	private ILogger logger;

	/**
	 * Initializes task for the given KIMBridge instance.
	 * @param bridge KIMBridge instance.
	 */
	public KIMIndexTask(KIMBridge bridge, ILogger daemonLogger) {
		kimBridge = bridge;
		logger = daemonLogger;
	}

	/**
	 * Triggers the annotation of new documents.
	 */
	@Override
	public void run() {
		try {
			kimBridge.annotateNewDocuments();
			kimBridge.saveRepositoryStates();
			kimBridge.persistStates();
		} catch (KIMBridgeException e) {
			// Error was logged by KIMBridge, shall be ignored by the task, we don't want to crash the KIMBridge service.
		} catch (Exception e) {
			// Catch-all fault barrier for unexpected exceptions. Yes, talking to you, linkedin-j developers.
			logger.logException(e);
		}
	}
}
