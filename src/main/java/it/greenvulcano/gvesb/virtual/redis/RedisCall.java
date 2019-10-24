package it.greenvulcano.gvesb.virtual.redis;

import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.configuration.XMLConfigException;
import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.channel.redis.RedisDBChannel;
import it.greenvulcano.gvesb.virtual.*;
import it.greenvulcano.gvesb.virtual.redis.dbo.RedisDBO;
import it.greenvulcano.util.metadata.PropertiesHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RedisCall implements CallOperation {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RedisCall.class);

	private OperationKey key = null;

	private String name;

	private String uri;

	NodeList dboList = null;

	private RedisDBO dbo;

	@Override
	public void init(Node node) throws InitializationException {

		logger.debug("Initializing redis-call...");

		try {
			// gets node name to show in logger
			name = XMLConfig.get(node, "@name");

			// gets corresponding redisClient from channel xpath
			uri = RedisDBChannel.getRedisClient(node);

			// gets child elements of redis-call
			dboList = XMLConfig.getNodeList(node, "./*");

			if (uri == null || uri.equals("")) {
				throw new NoSuchElementException("RedisClient instance not found for Operation " + name);
			}

			// checks whether child elements match the available operation types else throws
			// exception
			if (!Utils.validate(dboList)) {
				throw new XMLConfigException(
						"No valid DBO found for child nodes on: " + (node != null ? " " + node.getNodeName() : ""));
			}
			logger.debug("Configured Redis Call: " + name);

		} catch (Exception e) {

			throw new InitializationException("GV_INIT_SERVICE_ERROR", new String[][] { { "message", e.getMessage() } },
					e);

		}

	}

	@Override
	public GVBuffer perform(GVBuffer gvBuffer) throws ConnectionException, CallException, InvalidDataException {

		try {

			logger.debug("Preparing Redis call: " + name);

			// creating an instance of RedisOperation by opening connection through client
			// this will build a response list from all the operations
			uri = PropertiesHandler.expand(uri, gvBuffer);

			dbo = new RedisDBO(uri);

			ArrayList<String> actualResp = new ArrayList<String>();

			for (Node operation : Utils.iterable(dboList)) {

				// gets method corresponding to the type of operation to be executed
				// and invokes it, adding its response to the list.

				logger.debug("Executing Redis operation: " + operation.getNodeName());

				Method action = dbo.getClass().getDeclaredMethod(operation.getNodeName(), Node.class, GVBuffer.class);
				String currentResp = (String) action.invoke(dbo, operation, gvBuffer);

				logger.debug(String.format("Operation '%s:%s' performed succesfully with output: { %s }.", name,
						operation.getNodeName(), currentResp));
				
				actualResp.add(currentResp);

			}

			// formats response and overwrites buffer

			gvBuffer.setObject(Utils.format(actualResp));

		} catch (Exception exc) {
			throw new CallException("GV_CALL_SERVICE_ERROR",
					new String[][] { { "service", gvBuffer.getService() }, { "system", gvBuffer.getSystem() },
							{ "tid", gvBuffer.getId().toString() }, { "message", exc.getMessage() } },
					exc);
		}
		return gvBuffer;
	}

	@Override
	public void cleanUp() {
		// do nothing
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void setKey(OperationKey operationKey) {
		this.key = operationKey;
	}

	@Override
	public OperationKey getKey() {
		return key;
	}

	@Override
	public String getServiceAlias(GVBuffer gvBuffer) {
		return gvBuffer.getService();
	}

}
