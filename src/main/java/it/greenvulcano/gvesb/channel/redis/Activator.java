/*******************************************************************************
 * Copyright (c) 2009, 2019 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.channel.redis;

import it.greenvulcano.configuration.ConfigurationEvent;
import it.greenvulcano.configuration.ConfigurationListener;
import it.greenvulcano.configuration.XMLConfig;
import it.greenvulcano.gvesb.core.config.GreenVulcanoConfig;
import it.greenvulcano.gvesb.virtual.OperationFactory;
import it.greenvulcano.gvesb.virtual.redis.RedisCall;
import it.greenvulcano.gvesb.channel.redis.RedisDBChannel;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	// logger object
	private final static Logger log = LoggerFactory.getLogger(Activator.class);

	private final static ConfigurationListener configurationListener = event -> {

		log.debug("GV ESB Redis plugin module - handling configuration event");

		// if the GV ESB configuration file gets deleted, then shutdown the opened channel with the Redis database
		if (event.getCode() == ConfigurationEvent.EVT_FILE_REMOVED && event.getFile().equals(GreenVulcanoConfig.getSystemsConfigFileName())) {

			RedisDBChannel.shutdown();

		}

		else if (event.getCode() == ConfigurationEvent.EVT_FILE_LOADED && event.getFile().equals(GreenVulcanoConfig.getSystemsConfigFileName())) {

			RedisDBChannel.setup();

		}

	};

	@Override
	public void start(BundleContext context) throws Exception {

		// register the operations associated to the Redis connector

		RedisDBChannel.setup();
				
		OperationFactory.registerSupplier("redis-call", RedisCall::new);

		// register a configuration listener to watch for changes to the GV ESB systems configuration file

		XMLConfig.addConfigurationListener(configurationListener, GreenVulcanoConfig.getSystemsConfigFileName());

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
		// unregister the configuration listener to watch for changes to the GV ESB systems configuration file

		XMLConfig.removeConfigurationListener(configurationListener);

		// unregister the operations associated to the Redis connector

		OperationFactory.unregisterSupplier("redis-call");

	}

}
