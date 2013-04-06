package cz.zcu.kiv.eeg.KIMBridge.repository.linkedin;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.connectors.linekdin.LinkedInConnector;
import cz.zcu.kiv.eeg.KIMBridge.repository.IDocumentRepository;
import cz.zcu.kiv.eeg.KIMBridge.repository.IRepositoryFactory;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public class LinkedInRepositoryFactory implements IRepositoryFactory {
	private static final String KEY_GROUP_ID = "gid";

	private FactoryConfiguration configuration;

	public LinkedInRepositoryFactory(FactoryConfiguration config) {
		configuration = config;
	}

	@Override
	public IDocumentRepository createRepository(String id, RepositoryConfiguration config) throws ConfigurationException {
		LinkedInConnector connector = new LinkedInConnector(configuration);
		return new LinkedInRepository(id, connector, config.getProperty(KEY_GROUP_ID));
	}
}
