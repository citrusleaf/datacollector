package com.streamsets.pipeline.stage.lib.cassandra;

/*
 * Copyright 2012-2016 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.SocketOptions;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.api.Stage.ConfigIssue;

public final class CassandraResource {
	private static final Logger LOG = LoggerFactory.getLogger(CassandraResource.class);
	public static final String CONTACT_NODES_LABEL = "contactNodes";
	private List<InetAddress> contactPoints;
	private final int port;
	private final String username;
	private final String password;
	private final int connectionTimeout;
	private final int readTimeout;

	protected CassandraResource(int cpCount, int port, int connectTimeout, int readTimeout, String username, String password) {
		this.port = port;
		this.username = username;
		this.password = password;
		this.contactPoints = new ArrayList<>(cpCount);
		this.connectionTimeout = connectTimeout * 1000;  //sec. to millis
		this.readTimeout = readTimeout;
		
		LOG.info("connection timeout: " + this.connectionTimeout);
	}

	public static CassandraResource build(CassandraConfigBean config, List<ConfigIssue> issues, Stage.Context context) {
		CassandraResource cResource = new CassandraResource(config.contactNodes.size(), config.port, config.connectionTimeout, config.maxWaitTime, config.username,
				config.password);
		if (config.contactNodes.isEmpty()) {
			issues.add(context.createConfigIssue(Groups.CASSANDRA.name(), CONTACT_NODES_LABEL, Errors.CASSANDRA_00));
		}

		for (String address : config.contactNodes) {
			if (address.isEmpty()) {
				issues.add(
						context.createConfigIssue(Groups.CASSANDRA.name(), CONTACT_NODES_LABEL, Errors.CASSANDRA_01));
			}
		}

		for (String address : config.contactNodes) {
			if (null == address) {
				LOG.warn("A null value was passed in as a contact point.");
				continue;
			}

			try {
				cResource.contactPoints.add(InetAddress.getByName(address));
			} catch (UnknownHostException e) {
				LOG.error(Errors.CASSANDRA_04.getMessage(), address, e);
				issues.add(context.createConfigIssue(Groups.CASSANDRA.name(), CONTACT_NODES_LABEL, Errors.CASSANDRA_04, address));
			}
		}

		if (cResource.contactPoints.isEmpty()) {
			issues.add(context.createConfigIssue(Groups.CASSANDRA.name(), CONTACT_NODES_LABEL, Errors.CASSANDRA_00));
		}

		return cResource;
	}

	public Cluster buildCluster() {
		SocketOptions so = new SocketOptions().setReadTimeoutMillis(readTimeout).setConnectTimeoutMillis(connectionTimeout);
		
		return Cluster.builder().addContactPoints(contactPoints).withCredentials(username, password).withPort(port).withSocketOptions(so)
				.build();
	}
}
