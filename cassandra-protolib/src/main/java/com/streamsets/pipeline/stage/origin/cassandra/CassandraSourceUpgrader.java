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

package com.streamsets.pipeline.stage.origin.cassandra;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.streamsets.pipeline.api.Config;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.StageUpgrader;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.config.upgrade.DataFormatUpgradeHelper;
import com.streamsets.pipeline.stage.destination.cassandra.AuthProviderOption;
import com.streamsets.pipeline.stage.lib.cassandra.CassandraCompressionCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CassandraSourceUpgrader implements StageUpgrader {
  private static final String DATA_FORMAT_CONFIG = "cassandraOriginConfigBean.advancedConfig.";

  private final List<Config> configsToRemove = new ArrayList<>();
  private final List<Config> configsToAdd = new ArrayList<>();

  @Override
  public List<Config> upgrade(
      String library, String stageName, String stageInstance, int fromVersion, int toVersion, List<Config> configs
  ) throws StageException {
    List<Config> newConfigs = configs;

    switch (fromVersion) {
	    case 1:
	        upgradeV1ToV2(configs);
	        if (toVersion == 2) {
	          break;
	        }
	        // fall through
	      case 2:
	        upgradeV2ToV3(configs);
	        if (toVersion == 3) {
	          break;
	        }
	        // fall through
	      case 3:
	        newConfigs = upgradeV3ToV4(configs);
	        if (toVersion == 4) {
	          break;
	        }
	        // fall through
	      case 4:
	        newConfigs = upgradeV4ToV5(newConfigs);
	        break;
	      default:
	        throw new IllegalStateException(Utils.format("Unexpected fromVersion {}", fromVersion));
	    }
	    return newConfigs;
  }
  
  private List<Config> upgradeV4ToV5(List<Config> configs) {
	    configs.add(new Config("conf.authProviderOption", AuthProviderOption.NONE));
	    return configs
	        .stream()
	        .filter(c -> !"conf.useCredentials".equals(c.getName()))
	        .collect(Collectors.toList());
	  }

	  private void upgradeV1ToV2(List<Config> configs) {
	    configs.add(new Config("compression", CassandraCompressionCodec.NONE));
	  }

	  private void upgradeV2ToV3(List<Config> configs) {
	    configs.add(new Config("protocolVersion", ProtocolVersion.V3));
	  }

	  private List<Config> upgradeV3ToV4(List<Config> configs) {
	    List<Config> configsToRemove = new ArrayList<>();
	    List<Config> configsToAdd = new ArrayList<>();

	    for(Config conf: configs) {
	      switch (conf.getName()) {
	        case "contactNodes":
	          configsToAdd.add(new Config("conf.contactPoints", conf.getValue()));
	          configsToRemove.add(conf);
	          break;
	        case "port":
	        case "protocolVersion":
	        case "compression":
	        case "useCredentials":
	        case "qualifiedTableName":
	        case "columnNames":
	        case "username":
	        case "password":
	          configsToAdd.add(new Config("conf." + conf.getName(), conf.getValue()));
	          configsToRemove.add(conf);
	          break;
	      }
	    }

	    configsToAdd.add(new Config("conf.batchType", BatchStatement.Type.LOGGED));
	    configsToAdd.add(new Config("conf.maxBatchSize", 65535));

	    configs.addAll(configsToAdd);
	    configs.removeAll(configsToRemove);
	    return configs;
	  }
//  private void upgradeV1ToV2(List<Config> configs) {
//    for (Config config : configs) {
//      switch (config.getName()) {
//        case "cassandraOriginConfigBean.readStrategy":
//        case "cassandraOriginConfigBean.queueName":
//        case DATA_FORMAT_CONFIG + "keysPattern":
//        case DATA_FORMAT_CONFIG + "namespaceSeparator":
//        case DATA_FORMAT_CONFIG + "executionTimeout":
//          configsToRemove.add(config);
//          break;
//        case DATA_FORMAT_CONFIG + "connectionTimeout":
//          configsToAdd.add(new Config("cassandraOriginConfigBean.connectionTimeout", config.getValue()));
//          configsToRemove.add(config);
//          break;
//        default:
//          //no-op
//      }
//    }
//
//    configs.addAll(configsToAdd);
//    configs.removeAll(configsToRemove);
//  }
//
//  private void upgradeV2toV3(List<Config> configs) {
//    DataFormatUpgradeHelper.upgradeAvroParserWithSchemaRegistrySupport(configs);
//  }
}
