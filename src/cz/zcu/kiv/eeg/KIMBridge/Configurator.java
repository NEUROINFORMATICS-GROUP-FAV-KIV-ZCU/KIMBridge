package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Map;
import java.util.TreeMap;


/**
 * Configurator loads the repository configuration from specified stream.
 */
public class Configurator {
	private static final String EL_CONFIGURATION = "configuration";

	private static final String EL_FACTORIES = "factories";

	private static final String EL_REPOSITORIES = "repositories";

	private static final String ATTR_CLASS_NAME = "class";

	private static final String ATTR_REPO_TYPE = "type";

	private static final String DEFAULT_CONFIG = "config/default.xml";

	private Map<String, String> configuration = new TreeMap<>();

	private Map<String, FactoryConfiguration> factories = new TreeMap<>();

	private Map<String, RepositoryConfiguration> repositories = new TreeMap<>();

	private DocumentBuilder builder;

	public Configurator() throws ConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException("The XML document parser is not configured properly.", e);
		}
	}

	/**
	 * Loads default configuration.
	 * @throws KIMBridgeException if the default configuration could not be loaded.
	 */
	public void loadDefaults() throws KIMBridgeException {
		InputStream stream = getClass().getResourceAsStream(DEFAULT_CONFIG);
		try {
			load(stream);
		} catch (ConfigurationException e) {
			throw new KIMBridgeException("Error while loading configuration.", e);
		}
	}

	public void load(InputStream configStream) throws ConfigurationException {
		Document doc = createDocument(configStream);
		NodeList list = doc.getDocumentElement().getChildNodes();
		try {
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.getNodeName().equals(EL_CONFIGURATION)) {
						loadConfiguration(node);
					} else if (node.getNodeName().equals(EL_FACTORIES)) {
						loadFactories(node);
					} else if (node.getNodeName().equals(EL_REPOSITORIES)) {
						loadRepositories(node);
					} else {
						throw new ConfigurationException(String.format("Unknown configuration element: %s", node.getNodeName()));
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("Factory class cannot be found.", e);
		}
	}

	private Document createDocument(InputStream stream) throws ConfigurationException {

		try {
			Document doc = builder.parse(stream);
			doc.normalizeDocument();
			return doc;
		} catch (SAXException e) {
			throw new ConfigurationException("Malformed configuration file.", e);
		} catch (IOException e) {
			throw new ConfigurationException("Error while reading configuration file.", e);
		}
	}


	private void loadConfiguration(Node configNode) {
		loadPropertiesFromNode(configNode, configuration);
	}

	private void loadFactories(Node factoriesNode) throws ClassNotFoundException {
		NodeList items = factoriesNode.getChildNodes();
		for (int i = 0; i < items.getLength(); i++) {
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) item;
				String factoryName = el.getNodeName();
				if (factories.containsKey(factoryName)) {
					factories.get(factoryName).addProperties(loadPropertiesFromNode(el));
				} else {
					String className = el.getAttribute(ATTR_CLASS_NAME);
					FactoryConfiguration config = new FactoryConfiguration(className, loadPropertiesFromNode(el));
					factories.put(el.getNodeName(), config);
				}
			}
		}
	}

	private void loadRepositories(Node repositoriesNode) {
		NodeList items = repositoriesNode.getChildNodes();
		for (int i = 0; i < items.getLength(); i++) {
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) item;
				String type = el.getAttribute(ATTR_REPO_TYPE);
				RepositoryConfiguration config = new RepositoryConfiguration(type, loadPropertiesFromNode(el));
				repositories.put(el.getNodeName(), config);
			}
		}
	}


	private Map<String, String> loadPropertiesFromNode(Node node) {
		Map<String, String> props = new TreeMap<>();
		loadPropertiesFromNode(node, props);
		return props;
	}


	private Map<String, String> loadPropertiesFromNode(Node node, Map<String, String> coll) {
		NodeList items = node.getChildNodes();
		for (int i = 0; i < items.getLength(); i++) {
			Node item = items.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				coll.put(item.getNodeName(), item.getTextContent());
			}
		}
		return coll;
	}



	public String get(String key) throws ConfigurationException {
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else {
			throw new ConfigurationException(String.format("Missing required configuration key: %s", key));
		}
	}

	public String get(String key, String defaultValue) {
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else {
			return defaultValue;
		}
	}


	public Map<String, FactoryConfiguration> getFactories() {
		return factories;
	}


	public FactoryConfiguration getFactory(String name) throws ConfigurationException {
		if (factories.containsKey(name)) {
			return factories.get(name);
		} else {
			throw new ConfigurationException(String.format("Missing factory definition: %s", name));
		}
	}


	public Map<String, RepositoryConfiguration> getRepositories() {
		return repositories;
	}

	public void loadFile(File configFile) throws IOException, ConfigurationException {
		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(configFile));
		load(buf);
		buf.close();
	}
}
