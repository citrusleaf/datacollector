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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.destination.aerospike;

import com.streamsets.pipeline.stage.destination.display.Groups;
import com.streamsets.pipeline.api.ConfigDefBean;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.Target;
import com.streamsets.pipeline.configurablestage.DTarget;

@StageDef(
	    version = 1,
	    label = "Aerospike",
	    description = "Writes data to Aerospike",
	    icon = "aerospike.png",
	    recordsByRef = true,
	    onlineHelpRefUrl = ""
	)

	@ConfigGroups(Groups.class)
	@GenerateResourceBundle
	public class AerospikeDTarget extends DTarget{

	  @ConfigDefBean(groups = {"DATABASE"})
	  public AerospikeTargetConfig conf;

	  @Override
	  protected Target createTarget() {
	    return new AerospikeTarget(this.conf);
	  }
	}
/*
@StageDef(
    version = 1,
    label = "Aerospike",
    description = "",
    icon = "default.png",
    recordsByRef = true,
    onlineHelpRefUrl = ""
)
@ConfigGroups(value = Groups.class)
@GenerateResourceBundle
public class AerospikeDTarget extends AerospikeTarget {

//	@ConfigDef(
//			required = true,
//			type = ConfigDef.Type.LIST,
//			)
//	public List<String> strings;
	
  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "127.0.0.1",
      label = "Seed Host",
      description = "Seed node to connect to Aerospike",
      displayPosition = 10,
      group = "DATABASE"
  )
  public String seedHost;

  @Override
  public String getSeedHost() {
    return seedHost;
  }

  @ConfigDef(
	      required = true,
	      type = ConfigDef.Type.NUMBER,
	      defaultValue = "3000",
	      label = "Seed Port",
	      description = "Port on which to connect to Aerospike (default: 3000)",
	      displayPosition = 20,
	      group = "DATABASE"
	  )
	  public String port;

	  @Override
	  public int getSeedPort() {
	    return Integer.parseInt(port);
	  }

	  @ConfigDef(
		      required = true,
		      type = ConfigDef.Type.MODEL,
		      defaultValue = "",
		      label = "Fields",
		      description = "Key names, their values and storage type",
		      displayPosition = 40,
		      group = "DATABASE"
		  )
		  @ListBeanModel
		  public List<AerospikeFieldMappingConfig> redisFieldMapping;
}
*/