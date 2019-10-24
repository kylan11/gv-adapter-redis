package it.greenvulcano.gvesb.channel.redis;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.xml.XMLUtils;
import it.greenvulcano.util.xpath.XPathFinder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class RedisDBChannel {

	private final static Logger LOG = LoggerFactory.getLogger(RedisDBChannel.class);
	private final static Map<String, String> redisClients = new HashMap<>();

	public static void setup() {
		// finds all channel of type: RedisDBAdapter and builds corresponding uri strings from xpath
		try {

			NodeList redisChannelList = XMLConfig.getNodeList(GreenVulcanoConfig.getSystemsConfigFileName(),
					"//Channel[@type='RedisDBAdapter']");

			LOG.debug("Enabled RedisDBAdapter channels found: " + redisChannelList.getLength());
			IntStream.range(0, redisChannelList.getLength()).mapToObj(redisChannelList::item)
					.forEach(RedisDBChannel::buildRedisClient);

		} catch (XMLConfigException e) {
			LOG.error("Error reading configuration", e);
		}
	}

	public static void shutdown() {
		
		redisClients.clear();

	}

	private static void buildRedisClient(Node redisChannelNode) {
		try {

			if (XMLConfig.exists(redisChannelNode, "@endpoint")
					&& XMLConfig.getBoolean(redisChannelNode, "@enabled", true)) {

				LOG.debug("Configuring Redis instance for Channel " + XMLUtils.get_S(redisChannelNode, "@id-channel")
						+ " in System" + XMLUtils.get_S(redisChannelNode.getParentNode(), "@id-system"));

				String uri = PropertiesHandler.expand(XMLUtils.get_S(redisChannelNode, "@endpoint"));
				redisClients.put(XPathFinder.buildXPath(redisChannelNode), uri);
			}

		} catch (Exception e) {
			LOG.error("Error configuring RedisClient", e);
		}

	}

	public static String getRedisClient(Node callOperationNode) {

		Optional<String> uri = Optional.empty();
		String xpath = XPathFinder.buildXPath(callOperationNode.getParentNode());
		LOG.debug("Retrieving Redis Instance from Channel map using key: " + xpath);

		uri = Optional.ofNullable(redisClients.get(xpath));
		LOG.debug("Connection found: " + uri.get());

		return uri.get();
		
	}

}