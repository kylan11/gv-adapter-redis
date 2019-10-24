package it.greenvulcano.gvesb.virtual.redis;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.greenvulcano.gvesb.buffer.GVBuffer;
import it.greenvulcano.gvesb.virtual.redis.dbo.RedisDBO;

public class Utils {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Utils.class);
	
	public static boolean validate(NodeList dbo) {
		
		ArrayList<String> operations = new ArrayList<String>();
		
		for(Method method: new RedisDBO().getClass().getDeclaredMethods()) {
			
			operations.add(method.getName());
		
		}

		for (Node opNode: iterable(dbo)) {
			logger.debug("Reading Redis operation: " + opNode.getNodeName());
			
			if (operations.contains(opNode.getNodeName()))
				return true;
		}
		
		return false;
	}
	
	public static String format(ArrayList<String> resp) {
		if(resp.size() > 1) {
			return resp.toString();
		}
		return resp.get(0);
	}
	
	public static Iterable<Node> iterable(final NodeList nodeList) {
		return () -> new Iterator<Node>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < nodeList.getLength();
			}

			@Override
			public Node next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return nodeList.item(index++);
			}
		};
	}
	
	public static String format(GVBuffer gvBuffer) {
		try {
			byte[] gv = (byte[]) gvBuffer.getObject();
			return String.valueOf(gv);
		}
		catch(Exception e) {
			return gvBuffer.getObject().toString();
		}
	}
	
}
