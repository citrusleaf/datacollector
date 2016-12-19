package com.streamsets.pipeline.stage.destination.display;

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

import com.streamsets.pipeline.api.Label;

public enum RecordExistsAction implements Label {
	CREATE_ONLY("Create Only", com.aerospike.client.policy.RecordExistsAction.CREATE_ONLY),
	REPLACE("Replace", com.aerospike.client.policy.RecordExistsAction.REPLACE),
	REPLACE_ONLY("Replace Only", com.aerospike.client.policy.RecordExistsAction.REPLACE_ONLY),
	UPDATE("Update", com.aerospike.client.policy.RecordExistsAction.UPDATE),
	UPDATE_ONLY("Update Only", com.aerospike.client.policy.RecordExistsAction.UPDATE_ONLY);

	private String label;
	private com.aerospike.client.policy.RecordExistsAction action;

	RecordExistsAction(String label, com.aerospike.client.policy.RecordExistsAction action) {
		this.label = label;
		this.action = action;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public com.aerospike.client.policy.RecordExistsAction getAction() {
		return action;
	}
}
