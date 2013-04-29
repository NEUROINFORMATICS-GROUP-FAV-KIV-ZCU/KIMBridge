package cz.zcu.kiv.eeg.KIMBridge;

import cz.zcu.kiv.eeg.KIMBridge.config.FactoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.config.RepositoryConfiguration;
import cz.zcu.kiv.eeg.KIMBridge.logging.ILogger;
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
	public static final String LOG_COMPONENT = "Configurator";

	private static final String EL_CONFIGURATION = "configuration";

	private static final String EL_FACTORIES = "factories";

	private static final String EL_REPOSITORIES = "repositories";

	private static final String ATTR_CLASS_NAME = "class";

	private static final String ATTR_REPO_TYPE = "type";

	private ILogger logger;

	private Map<String, String> configuration = new TreeMap<>();

	private Map<String, FactoryConfiguration> factories = new TreeMap<>();

	private Map<String, RepositoryConfiguration> repositories = new TreeMap<>();

	private DocumentBuilder builder;

	/**
	 * Initializes configurator.
	 * @param logger Logger.
	 * @throws ConfigurationException when the XML document parser is not configured properly.
	 */
	public Configurator(ILogger logger) throws ConfigurationException {
		this.logger = logger;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException("The XML document parser is not configured properly.", e);
		}
	}

	/**
	 * Loads configuration from specified file.
	 * @param configFile Configuration file.
	 * @throws IOException when the file could not be read.
	 * @throws ConfigurationException when the configuration is malformed.
	 */
	public void loadFile(File configFile) throws IOException, ConfigurationException {
		logger.logMessage("Loading %s", configFile.getPath());
		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(configFile));
		load(buf);
		buf.close();
	}

	/**
	 * Loads configuration from specified stream.
	 * @param configStream Stream with XML configuration.
	 * @throws ConfigurationException when the configuration is malformed.
	 */
	private void load(InputStream configStream) throws ConfigurationException {
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

	/**
	 * Creates XML document of configuration.
	 * @param stream Input stream.
	 * @return Configuration document.
	 * @throws ConfigurationException when the document could not be created, e.g. XML is malformed.
	 */
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

	/**
	 * Loads configuration properties from specified node.
	 * @param configNode Node.
	 */
	private void loadConfiguration(Node configNode) {
		loadPropertiesFromNode(configNode, configuration);
	}

	/**
	 * Load factories configuration.
	 * @param factoriesNode Node with factories configuration.
	 * @throws ClassNotFoundException when class of factory cannot be found.
	 */
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

	/**
	 * Loads repositories configuration.
	 * @param repositoriesNode Node with repositories configuration.
	 */
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

	/**
	 * Loads properties from specified node.
	 * @param node Node.
	 * @return Loaded properties.
	 */
	private Map<String, String> loadPropertiesFromNode(Node node) {
		Map<String, String> props = new TreeMap<>();
		loadPropertiesFromNode(node, props);
		return props;
	}

	/**
	 * Loads properties from specified node and stores them in specified collection.
	 * @param node Node.
	 * @param coll Collection of properties.
	 * @return Collection of properties.
	 */
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


	/**
	 * Gets the configuration key.
	 * @param key Key.
	 * @return Value.
	 * @throws ConfigurationException when the configuration key could not be found.
	 */
	public String get(String key) throws ConfigurationException {
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else {
			throw new ConfigurationException(String.format("Missing required configuration key: %s", key));
		}
	}

	/**
	 * Gets the configuration key or default value when the key cannot be found.
	 * @param key Key.
	 * @param defaultValue Default value.
	 * @return Value.
	 */
	public String get(String key, String defaultValue) {
		if (configuration.containsKey(key)) {
			return configuration.get(key);
		} else {
			return defaultValue;
		}
	}


	/**
	 * Gets map of configured factories.
	 * @return Map with factory configurations.
	 */
	public Map<String, FactoryConfiguration> getFactories() {
		return factories;
	}

	/**
	 * Gets configuration of factory with specified name.
	 * @param name Name.
	 * @return Configuration of the factory.
	 * @throws ConfigurationException when the factory configuration could not be found.
	 */
	public FactoryConfiguration getFactory(String name) throws ConfigurationException {
		if (factories.containsKey(name)) {
			return factories.get(name);
		} else {
			throw new ConfigurationException(String.format("Missing factory definition: %s", name));
		}
	}

	/**
	 * Gets map of configured repositories.
	 * @return Map with repository configurations.
	 */
	public Map<String, RepositoryConfiguration> getRepositories() {
		return repositories;
	}
}
