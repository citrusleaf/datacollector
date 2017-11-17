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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Value;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.streamsets.pipeline.api.Batch;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseTarget;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.stage.destination.display.BinNameTooLongAction;
import com.streamsets.pipeline.stage.destination.display.DataType;
import com.streamsets.pipeline.stage.destination.display.Groups;

/**
 * This target is an example and does not actually write to any destination.
 */
public class AerospikeTarget extends BaseTarget {

	private static final int MAX_BIN_NAME = 14;
	private static final Logger LOG = LoggerFactory.getLogger(AerospikeTarget.class);
	private AerospikeClient client = null;
	private AerospikeTargetConfig conf = null;
	public AerospikeTarget(AerospikeTargetConfig conf) {
		this.conf = conf;

	}

	/** {@inheritDoc} */
	@Override
	protected List<ConfigIssue> init() {
		// Validate configuration values and open any required resources.
		List<ConfigIssue> issues = super.init();

		if (conf.seedHost.isEmpty()) {
			issues.add(
					getContext().createConfigIssue(
							Groups.DATABASE.name(), "config", Errors.INVALID_CONFIGURATION, "Missing Aerospike host"
							)
					);
		}

		if (conf.port <= 1024 || conf.port > 65535) {
			issues.add(
					getContext().createConfigIssue(
							Groups.DATABASE.name(), "config", Errors.INVALID_CONFIGURATION, "Invalid Aerospike port"
							)
					);
		}

		if (conf.connectionTimeout < 0) {
			issues.add(
					getContext().createConfigIssue(
							Groups.DATABASE.name(), "config", Errors.INVALID_CONFIGURATION, "Invalid connection timeout"
							)
					);
		}

		if (conf.namespaceName.isEmpty()) {
			issues.add(
					getContext().createConfigIssue(
							Groups.FIELD_MAPPING.name(), "config", Errors.INVALID_CONFIGURATION, "Missing namespace name"
							)
					);
		}

		if (issues.size() == 0) {
			// Try to connect to the cluster
			ClientPolicy policy = new ClientPolicy();
			if(conf.authProviderOption != AuthProviderOption.NONE){
				policy.user = conf.username;
				policy.password = conf.password;
			}
			
			policy.failIfNotConnected = true;
			policy.timeout = conf.connectionTimeout;
			try {
				client = new AerospikeClient(policy, conf.seedHost, conf.port);
				if (conf != null) {
					if (conf.aerospikeFieldMapping.isEmpty()) {
						issues.add(
								getContext().createConfigIssue(
										"DATABASE",
										"conf.aerospikeFieldMapping",
										Errors.INVALID_FIELD_MAPPING,
										conf.aerospikeFieldMapping,
										Errors.INVALID_FIELD_MAPPING.getMessage()
										)
								);

					}
				}
			}
			catch (AerospikeException ae) {
				issues.add(getContext().createConfigIssue("DATABASE", "Seed Host", Errors.COULD_NOT_CONNECT, 
						conf.seedHost, conf.port, ae.toString()));
			}
		}
		// If issues is not empty, the UI will inform the user of each configuration issue in the list.
		return issues;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		// Clean up any open resources.
		super.destroy();
		if (client != null) {
			client.close();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void write(Batch batch) throws StageException {
		Iterator<Record> batchIterator = batch.getRecords();

		while (batchIterator.hasNext()) {
			Record record = batchIterator.next();
			try {
				write(record);
			} catch (Exception e) {
				switch (getContext().getOnErrorRecord()) {
				case DISCARD:
					break;
				case TO_ERROR:
					getContext().toError(record, Errors.SAMPLE_01, e.toString());
					break;
				case STOP_PIPELINE:
					throw new StageException(Errors.SAMPLE_01, e.toString());
				default:
					throw new IllegalStateException(
							Utils.format("Unknown OnError value '{}'", getContext().getOnErrorRecord(), e)
							);
				}
			}
		}
	}

	private int getTimeToLive(String ttl, Record record) {
		try {
			return Integer.parseInt(ttl);
		}
		catch (NumberFormatException nfe) {
			Field field = record.get(ttl);
			if (field != null) {
				try {
					return field.getValueAsInteger();
				}
				catch (Exception e) {
					LOG.error("Field {} has a value of {}, which is not a valid integer", ttl, field.getValue());
				}
			}
		}
		LOG.error("Could not find a valid TTL from configuration value '{}', assuming 0", ttl);
		return 0;
	}
	
	private Bin addBin(String binName, Field value, DataType dataType, List<Bin> bins) {
		Bin bin = null;
		if (value != null && binName != null) {
			switch (value.getType()) {
			case STRING:
				bin = new Bin(binName, value.getValueAsString());
				break;
			case INTEGER:
				bin = new Bin(binName, value.getValueAsInteger());
				break;
			case LONG:
				bin = new Bin(binName, value.getValueAsLong());
				break;
			case BYTE_ARRAY:
				bin = new Bin(binName, value.getValueAsByteArray());
				break;
			case MAP:
				bin = new Bin(binName, Value.get(unpackField(value)));
				break;
			default:
				bin = new Bin(binName, Value.get(value.getValue()));
				break;
			}
		}
		if (bin != null) {
			bins.add(bin);
		}
		return bin;
	}
	/**
	 * Writes a single record to the destination.
	 *
	 * @param record the record to write to the destination.
	 * @throws OnRecordErrorException when a record cannot be written.
	 */
	private void write(Record record) throws OnRecordErrorException {
		if (!record.has(conf.keyExpr)) {
			throw new OnRecordErrorException(Errors.SAMPLE_01, record, "Missing key from field {}", conf.keyExpr);
		}
		int ttl = getTimeToLive(conf.timeToLive, record);
		List<Bin> bins = new ArrayList<Bin>();
		Field keyField = record.get(conf.keyExpr);
		
		Key key = null;
		switch (keyField.getType()) {
		case STRING:
			key = new Key(conf.namespaceName, conf.setName, keyField.getValueAsString());
			break;
		case INTEGER:
			key = new Key(conf.namespaceName, conf.setName, keyField.getValueAsInteger());
			break;
		case LONG:
			key = new Key(conf.namespaceName, conf.setName, keyField.getValueAsLong());
			break;
		case BYTE_ARRAY:
			key = new Key(conf.namespaceName, conf.setName, keyField.getValueAsByteArray());
			break;
		default:
			key = new Key(conf.namespaceName, conf.setName, Value.get(keyField.getValue()));
			break;
		}

		if (conf.allFields) {
			for (String path : record.getEscapedFieldPaths()) {
				String binName = null;
				Field field = record.get(path);
				LOG.info("path: {}, value: {}", path, field.getValue());
				if (path.matches(".*/[a-zA-Z0-9-_]+")) {
					binName = path.substring(path.lastIndexOf("/")+1);
					if (binName.length() > MAX_BIN_NAME) {
						LOG.error("Ignoring bin '{}': Name too long ({} max)", binName, MAX_BIN_NAME);
					}
				}
				else {
					LOG.error("Path '{}' contains no valid bin name after a final / character", path);
				}
				if (binName != null && field != null && binName != conf.keyExpr) {
					if (conf.binNameTooLongAction == BinNameTooLongAction.TRUNCATE && binName.length() > MAX_BIN_NAME) {
						binName = binName.substring(0, MAX_BIN_NAME);
					}
					addBin(binName, field, DataType.DEFAULT, bins);
				}
			}
		}
		else {
			for(AerospikeFieldMappingConfig parameters : conf.aerospikeFieldMapping) {
				Field value = null;
				if (record.has(parameters.valueExpr)) {
					value = record.get(parameters.valueExpr);
				}
				addBin(parameters.binName, value, parameters.dataType, bins);
			}
		}
		if (bins.size() > 0) {
			WritePolicy writePolicy = new WritePolicy();
			writePolicy.sendKey = conf.storePK;
			writePolicy.expiration = ttl;
			writePolicy.recordExistsAction = conf.recordExistsAction.getAction();
			client.put(writePolicy, key, bins.toArray(new Bin[bins.size()]));
		}
		else {
			throw new OnRecordErrorException(Errors.SAMPLE_01, record, "No bins to write, check log for details.");
		}
	}
	
	private Object unpackField(Field field){
		switch (field.getType()) {
		case STRING:
			return field.getValueAsString();
		case INTEGER:
			return field.getValueAsInteger();
		case LONG:
			return field.getValueAsLong();
		case BYTE_ARRAY:
			return field.getValueAsByteArray();
		case MAP:
			@SuppressWarnings("unchecked") Map<String, ?> fieldMap = (Map<String, ?>) field.getValue();
			Map<String, Object> map = new LinkedHashMap<>();
			for (Map.Entry<String, ?> entry : fieldMap.entrySet()) {
				if(entry.getValue() instanceof Field)
					map.put(entry.getKey(), unpackField((Field)entry.getValue()));
				else map.put(entry.getKey(), entry.getValue());
			}
			return map;
		case LIST:
			List<?> fList = (List<?>) field.getValue();
			List<Object> list = new ArrayList<>(fList.size());
			for (Object element : fList) {
				if(element instanceof Field) list.add(unpackField((Field)element));
				else list.add(element);
			}
			return list;			
		default: return field.getValue();
		}
	}
}
