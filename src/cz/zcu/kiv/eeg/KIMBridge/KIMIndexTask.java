package cz.zcu.kiv.eeg.KIMBridge;

import java.util.TimerTask;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class KIMIndexTask extends TimerTask {
	private KIMBridgeDaemon kimDaemon;

	private KIMBridge kimBridge;

	public KIMIndexTask(KIMBridgeDaemon daemon, KIMBridge bridge) {
		kimDaemon = daemon;
		kimBridge = bridge;
	}

	@Override
	public void run() {
		try {
			kimBridge.annotateNewDocuments();
		} catch (KIMBridgeException e) {
			// error was logged by KIMBridge
		}
	}
}
