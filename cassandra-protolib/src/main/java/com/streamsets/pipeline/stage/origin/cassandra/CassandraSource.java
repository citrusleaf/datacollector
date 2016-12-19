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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.google.common.reflect.TypeToken;
import com.streamsets.pipeline.api.BatchMaker;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.lib.util.JsonUtil;

public class CassandraSource extends BaseCassandraSource {

  /**
   * Creates a new instance of cassandra source.
   *
   * @param cassanddraOriginConfigBean origin configuration
   */
  public CassandraSource(CassandraOriginConfigBean cassandraOriginConfigBean) {
    super(cassandraOriginConfigBean);
   }

  @Override
  protected List<ConfigIssue> init() {
    // Validate configuration values and open any required resources.
    List<ConfigIssue> issues = super.init();

    try{
    	createCassandraClient(issues);    	
    }catch(StageException se){}

    // If issues is not empty, the UI will inform the user of each configuration issue in the list.
    return issues;
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String produce(String lastSourceOffset, int maxBatchSize, BatchMaker batchMaker) throws StageException {
    // Offsets can vary depending on the data source. Here we use an integer as an example only.
    long nextSourceOffset = 0;
    lastSourceOffset = lastSourceOffset == null ? "" : lastSourceOffset;
    if (!lastSourceOffset.equals("")) {
      nextSourceOffset = Long.parseLong(lastSourceOffset);
    }

    int recordCounter = 0;
    long startTime = System.currentTimeMillis();
    int maxRecords = Math.min(maxBatchSize, conf.maxBatchSize);

    while (recordCounter < maxRecords && (startTime + conf.maxWaitTime) > System.currentTimeMillis()) {
      
    	Row row = rs.one();
    	if(row == null) break;
    	recordCounter++;
   // 	Record rec = new RecordImpl();
        final String recordContext = conf.contactNodes.get(0) + "::" +
                conf.keyspace + "::" + conf.tableName + "::" +
                nextSourceOffset;
        Record record = getContext().createRecord(recordContext);

        Map<String, Field> fields = new HashMap<>();
        try {
			fields.put("id", JsonUtil.jsonToField(UUID.randomUUID()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
            ColumnDefinitions colDef = row.getColumnDefinitions();
            List<ColumnDefinitions.Definition>    cdList = colDef.asList();
            for (ColumnDefinitions.Definition cd : cdList) {
                Field value;
                Object fieldObject = null;
                DataType dt = cd.getType();
                switch (dt.getName()) {
                case ASCII :
                case VARCHAR :
                case TEXT :
                    fieldObject = row.getString(cd.getName());
                    break;
                    
                case COUNTER :
                case INT :
                    fieldObject = new Integer( row.getInt(cd.getName()));
                    break;
                    
                case BLOB :
                case CUSTOM :
                	fieldObject = row.getBytes(cd.getName());
                    break;
                    
                case BOOLEAN :
                	fieldObject = row.getBool(cd.getName());
                    break;
                
                case DECIMAL :
                	fieldObject = row.getDecimal(cd.getName());
                    break;
                    
                case DOUBLE :
                	fieldObject = new Double( row.getDouble(cd.getName()));
                    break;
                    
                case FLOAT :
                	fieldObject = new Float( row.getFloat(cd.getName()));
                    break;
                    
                case LIST :
                    row.getList(cd.getName(), TypeToken.class);
                    break;

                case TIMESTAMP :
                	fieldObject = row.getTimestamp(cd.getName());
                    break;

                case UUID :
                	fieldObject = row.getUUID(cd.getName());
                    break;

                case BIGINT :
                	fieldObject = new Long(row.getLong(cd.getName()));
                    break;
                    
                case VARINT :    
                	fieldObject = row.getVarint(cd.getName());
                    break;    
                    
                case MAP :
                case SET :
                    
                case INET :
                case TIMEUUID :
                case TUPLE :
                case UDT :
                    
                default:
                    continue;
                    
                }
                try {
                value = JsonUtil.jsonToField(fieldObject);
                fields.put(cd.getName(), value);
                } catch (Exception e) {
                	e.printStackTrace();
                }
                
            }
            
            record.set(Field.create(fields));
            batchMaker.addRecord(record);
            //System.out.format("%s %f\n", row.getString("ticker"), row.getDecimal("close"));
        }
    	
    return lastSourceOffset;
  }

}
