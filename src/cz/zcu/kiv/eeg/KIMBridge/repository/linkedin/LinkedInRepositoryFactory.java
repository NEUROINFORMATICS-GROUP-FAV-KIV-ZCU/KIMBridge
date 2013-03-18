package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import cz.zcu.kiv.eeg.KIMBridge.Configurator;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepositoryFactory {
	private Configurator configurator;

	public LinkedInRepositoryFactory(Configurator configurator) {
		this.configurator = configurator;
	}

	public LinkedInRepository createRepository(String groupId) {
		LinkedInConnector connector = new LinkedInConnector(configurator);
		return new LinkedInRepository(connector, groupId);
	}
}
