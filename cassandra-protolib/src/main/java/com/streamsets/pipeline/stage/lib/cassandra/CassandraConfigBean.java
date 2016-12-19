package com.streamsets.pipeline.stage.lib.cassandra;

/**
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

import java.util.List;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.lib.el.VaultEL;

public class CassandraConfigBean {

	public static final String DATA_FROMAT_CONFIG_BEAN_PREFIX = "dataFormatConfig.";

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.LIST,
		defaultValue = "[\"localhost\"]",
		label = "Cassandra Contact Points",
		description = "Hostnames of Cassandra nodes to use as contact points. To ensure a connection, enter several.",
		displayPosition = 10,
		group = "CASSANDRA"
	)
	public List<String> contactNodes;

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.NUMBER,
		defaultValue = "9042",
		label = "Cassandra Port",
		description = "Port number to use when connecting to Cassandra nodes",
		displayPosition = 20,
		group = "CASSANDRA"
	  )
	  public int port;

  
	@ConfigDef(
		required = true,
		type = ConfigDef.Type.NUMBER,
		label = "Connection Timeout (sec)",
		description = "Connection timeout (sec)",
		defaultValue = "60",
		min = 1,
		group = "CASSANDRA",
		displayPosition = 40
	)
	public int connectionTimeout;
  
	@ConfigDef(
		required = true,
		type = ConfigDef.Type.BOOLEAN,
		label = "Use Credentials",
		defaultValue = "false",
		displayPosition = 40,
		group = "CASSANDRA"
	)
	public boolean useCredentials = false;

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.STRING,
		label = "Username",
		defaultValue = "",
		displayPosition = 10,
		elDefs = VaultEL.class,
		group = "CREDENTIALS",
		dependsOn = "useCredentials",
		triggeredByValue = "true"
	 )
	public String username;

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.STRING,
		label = "Password",
		defaultValue = "",
		displayPosition = 20,
		elDefs = VaultEL.class,
		group = "CREDENTIALS",
		dependsOn = "useCredentials",
		triggeredByValue = "true"
	)
	public String password;

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.NUMBER,
		label = "Batch Wait Time (ms)",
	    defaultValue = "2000",
	    description = "Maximum time to wait for data before sending a partial or empty batch",
	    group = "CASSANDRA",
	    min = 1,
	    max = Integer.MAX_VALUE,
	    displayPosition = 1000
	)
	public int maxWaitTime = 2000;

}