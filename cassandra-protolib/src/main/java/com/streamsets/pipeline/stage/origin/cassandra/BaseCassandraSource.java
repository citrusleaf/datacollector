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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.Source;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseSource;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.lib.parser.DataParserFactory;
import com.streamsets.pipeline.stage.common.DefaultErrorRecordHandler;
import com.streamsets.pipeline.stage.common.ErrorRecordHandler;
import com.streamsets.pipeline.stage.lib.cassandra.CassandraResource;
import com.streamsets.pipeline.stage.lib.cassandra.Errors;
import com.streamsets.pipeline.stage.lib.cassandra.Groups;

public abstract class BaseCassandraSource extends BaseSource {

	protected static final Logger LOG = LoggerFactory.getLogger(BaseCassandraSource.class);
	protected final CassandraOriginConfigBean conf;
	protected DataParserFactory parserFactory;
	protected Cluster cassandraClient;
	protected Session cassandraSession;
	private ErrorRecordHandler errorRecordHandler;
	ResultSet rs;
	ColumnDefinitions cds;
	private CassandraResource cassandraResource;

	/**
	 * Creates a new instance of cassandra source.
	 *
	 * @param cassandraOriginConfigBean
	 *            origin configuration
	 */
	public BaseCassandraSource(CassandraOriginConfigBean cassandraOriginConfigBean) {
		this.conf = cassandraOriginConfigBean;
	}

	@Override
	protected List<ConfigIssue> init() {
		errorRecordHandler = new DefaultErrorRecordHandler(getContext());
		List<ConfigIssue> issues = new ArrayList<>();
		cassandraResource = CassandraResource.build(conf, issues, getContext());
		try {
			checkCassandraReachable(issues);
		} catch (StageException e) {
			e.printStackTrace();
		}

		return issues;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		// Clean up any open resources.
		if (null != cassandraClient) {
			cassandraSession.close();
			cassandraSession = null;
			cassandraClient.closeAsync();
			cassandraClient = null;
		}
		super.destroy();
	}

	protected boolean createCassandraClient(List<ConfigIssue> issues) throws StageException {
		boolean isOk = true;
		if (null == cassandraClient) {
			try {
				cassandraClient = cassandraResource.buildCluster();
				cassandraSession = cassandraClient.connect(conf.keyspace);
				SimpleStatement stmt = new SimpleStatement("select * from " + conf.tableName);
				stmt.setFetchSize(conf.maxBatchSize);
				rs = cassandraSession.execute(stmt);
				cds = rs.getColumnDefinitions();
			} catch (Exception e) {
				LOG.error("Can't create cassandra client", e);
				issues.add(getContext().createConfigIssue(Groups.CASSANDRA.name(),
						CassandraResource.CONTACT_NODES_LABEL, Errors.CASSANDRA_01, conf.contactNodes, e.toString()));
				errorRecordHandler.onError(Errors.CASSANDRA_01, e.toString(), e);
				isOk = false;
			}
		}
		return isOk;
	}

	private boolean checkCassandraReachable(List<ConfigIssue> issues) throws StageException {
		boolean isReachable = true;
		try (Cluster validationCluster = cassandraResource.buildCluster()) {
			Session validationSession = validationCluster.connect();
			validationSession.close();
		} catch (NoHostAvailableException | AuthenticationException | IllegalStateException e) {
			isReachable = false;
			Source.Context context = getContext();
			LOG.error(Errors.CASSANDRA_05.getMessage(), e.toString(), e);
			issues.add(context.createConfigIssue(Groups.CASSANDRA.name(), CassandraResource.CONTACT_NODES_LABEL,
					Errors.CASSANDRA_05, e.toString()));

			errorRecordHandler.onError(Errors.CASSANDRA_06, e.toString(), e);
		}
		return isReachable;
	}

}
