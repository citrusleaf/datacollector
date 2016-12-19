/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.origin.cassandra;

import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.stage.lib.cassandra.CassandraConfigBean;

public class CassandraOriginConfigBean extends CassandraConfigBean{

	public static final String DATA_FROMAT_CONFIG_BEAN_PREFIX = "dataFormatConfig.";

	@ConfigDef(
		required = false,
		type = ConfigDef.Type.STRING,
		label = "Keyspace",
		description = "Subscribes to channels with names that match the pattern",
		group = "CASSANDRA",
		displayPosition = 50
	)
	public String keyspace;

	@ConfigDef(
		required = false,
		type = ConfigDef.Type.STRING,
		label = "TableName",
		description = "Subscribes to channels with names that match the pattern",
		group = "CASSANDRA",
		displayPosition = 60
	)
	public String tableName;

	@ConfigDef(
		required = true,
		type = ConfigDef.Type.NUMBER,
		defaultValue = "1000",
		label = "Max Batch Size (records)",
		description = "Max number of records per batch",
		group = "CASSANDRA",
		min = 1,
		max = Integer.MAX_VALUE,
		displayPosition = 1001
	)
	public int maxBatchSize = 1000;
}