package cz.zcu.kiv.eeg.KIMBridge.repository;

import cz.zcu.kiv.eeg.KIMBridge.ConfigurationException;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;

/**
 * @author Jan Smitka <jan@smitka.org>
 */
public interface IRepositoryFactory {
	public IDocumentRepository createRepository(String id, RepositoryConfiguration configuration) throws ConfigurationException;
}
