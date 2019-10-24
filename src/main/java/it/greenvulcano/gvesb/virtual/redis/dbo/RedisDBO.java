package it.greenvulcano.gvesb.virtual.redis.dbo;

import org.w3c.dom.Node;
import redis.clients.jedis.Jedis;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.redis.Utils;
import it.greenvulcano.util.metadata.PropertiesHandler;
import it.greenvulcano.util.metadata.PropertiesHandlerException;

public class RedisDBO {

	private Jedis conn;

	public RedisDBO(String uri) {
		this.conn = new Jedis(uri);
	}

	public RedisDBO() {
	}

	public String get(Node node, GVBuffer gvBuffer) throws XMLConfigException, PropertiesHandlerException {
		String key = XMLConfig.get(node, "@key");
		key = PropertiesHandler.expand(key, gvBuffer);
		return conn.get(key);
	}

	public String set(Node node, GVBuffer gvBuffer) throws XMLConfigException, PropertiesHandlerException {
		String key = XMLConfig.get(node, "@key");
		key = PropertiesHandler.expand(key, gvBuffer);
		String value = XMLConfig.get(node, "@value");
		if (value == null || value.equals(""))
			value = Utils.format(gvBuffer);
		return conn.set(key, value);
	}

	public String delete(Node node, GVBuffer gvBuffer) throws XMLConfigException, PropertiesHandlerException {
		String key = XMLConfig.get(node, "@key");
		key = PropertiesHandler.expand(key, gvBuffer);
		return conn.del(key).toString();
	}

	public String keys(Node node, GVBuffer gvBuffer) throws XMLConfigException, PropertiesHandlerException {
		String pattern = XMLConfig.get(node, "@expression");
		pattern = PropertiesHandler.expand(pattern, gvBuffer);
		return conn.keys(pattern).toString();
	}

	public void close() {
		conn.close();
	}
}
