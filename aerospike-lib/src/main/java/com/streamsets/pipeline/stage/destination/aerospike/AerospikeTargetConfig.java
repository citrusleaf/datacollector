/**
 * Copyright 2016 StreamSets Inc.
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

package com.streamsets.pipeline.stage.destination.aerospike;

import java.util.List;

import com.streamsets.pipeline.stage.destination.display.BinNameTooLongAction;
import com.streamsets.pipeline.stage.destination.display.BinNameTooLongActionChooserValues;
import com.streamsets.pipeline.stage.destination.display.RecordExistsAction;
import com.streamsets.pipeline.stage.destination.display.RecordExistsActionChooserValues;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;
import com.streamsets.pipeline.api.ListBeanModel;
import com.streamsets.pipeline.api.ValueChooserModel;

public class AerospikeTargetConfig {

  public static final String AEROSPIKE_TARGET_CONFIG_PREFIX = "AerospikeTargetConfig.";

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.STRING,
	      defaultValue = "127.0.0.1",
	      label = "Seed Host",
	      description = "Seed node to connect to Aerospike",
	      displayPosition = 10,
	      group = "DATABASE"
	  )
  public String seedHost = "127.0.0.1";

  @ConfigDef(
		  required = true,
		  type = ConfigDef.Type.NUMBER,
		  defaultValue = "3000",
		  label = "Seed Port",
		  description = "Port on which to connect to Aerospike (default: 3000)",
		  displayPosition = 20,
		  min = 1024,
		  group = "DATABASE"
		  )
  public int port = 3000;

  /** {@inheritDoc} */
  @ConfigDef(
      type = ConfigDef.Type.NUMBER,
      label = "Connection Timeout (sec)",
      defaultValue = "1000",
      required = true,
      min = 1,
      displayPosition = 30,
      group = "DATABASE"
  )
  public int connectionTimeout = 1000;

  @ConfigDef(
	      type = ConfigDef.Type.MODEL,
	      label = "Record Exists Action",
	      defaultValue = "UPDATE",
	      required = true,
	      displayPosition = 10,
	      group = "ADVANCED"
	  )
  @ValueChooserModel(RecordExistsActionChooserValues.class)
  public RecordExistsAction recordExistsAction = RecordExistsAction.UPDATE;

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.MODEL,
	      defaultValue = "/",
	      label = "Primary Key",
	      description = "Field to use for the primary key",
	      group = "FIELD_MAPPING",
	      displayPosition = 40)
  @FieldSelectorModel(singleValued = true)
  public String keyExpr;

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.MODEL,
	      defaultValue = "0",
	      label = "Time To Live (sec)",
	      description = "Field to use for the time to live, -1 is infinite, 0 gets value from cluster, positive number for TTL in seconds.",
	      group = "FIELD_MAPPING",
	      displayPosition = 45)
  @FieldSelectorModel(singleValued = true)
  public String timeToLive;

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.BOOLEAN,
	      label = "Store non-hashed key",
	      description = "Whether the primary key should be written to the database, or just the digest",
	      defaultValue = "false",
	      displayPosition = 45,
	      group = "FIELD_MAPPING"
	  )
  public boolean storePK = true;

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.STRING,
	      defaultValue = "test",
	      label = "Namespace",
	      description = "Namespace which will house the set where the data will be written",
	      displayPosition = 50,
	      group = "FIELD_MAPPING"
	  )
  public String namespaceName = "test";

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.STRING,
	      defaultValue = "testSet",
	      label = "Set",
	      description = "Name of the set to which the data will be written",
	      displayPosition = 60,
	      group = "FIELD_MAPPING"
	  )
  public String setName = "testSet";

  
  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.BOOLEAN,
	      label = "Write all fields",
	      description = "Whether to write the data in batches as key-value pairs or to publish the data as messages",
	      defaultValue = "true",
	      displayPosition = 70,
	      group = "FIELD_MAPPING"
	  )
  public boolean allFields = true;

  @ConfigDef(
	      type = ConfigDef.Type.MODEL,
	      label = "Bin Name Too Long Action",
	      defaultValue = "TRUNCATE",
	      required = true,
	      displayPosition = 90,
	      group = "FIELD_MAPPING",
	      dependsOn = "allFields",
	      triggeredByValue = {"true"}
	  )
  @ValueChooserModel(BinNameTooLongActionChooserValues.class)
  public BinNameTooLongAction binNameTooLongAction = BinNameTooLongAction.TRUNCATE;

  
  @ConfigDef(
      required = false,
      type = ConfigDef.Type.MODEL,
      defaultValue = "",
      label = "Fields",
      description = "Key names, their values and storage type",
      displayPosition = 80,
      group = "FIELD_MAPPING",
      dependsOn = "allFields",
      triggeredByValue = {"false"}
  )
  
  @ListBeanModel
  public List<AerospikeFieldMappingConfig> aerospikeFieldMapping;

//  @ConfigDefBean(groups = "DATABASE")
//  public DataGeneratorFormatConfig dataFormatConfig = new DataGeneratorFormatConfig();

}