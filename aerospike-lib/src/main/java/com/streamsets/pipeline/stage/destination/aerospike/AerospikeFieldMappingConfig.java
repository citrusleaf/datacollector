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

import com.streamsets.pipeline.stage.destination.display.DataType;
import com.streamsets.pipeline.stage.destination.display.DataTypeChooserValues;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.FieldSelectorModel;
import com.streamsets.pipeline.api.ValueChooserModel;

public class AerospikeFieldMappingConfig {
  /**
   * Parameter-less constructor required.
   */
  public AerospikeFieldMappingConfig() {
  }

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "/",
      label = "Value",
      description = "Field to use for the value",
      displayPosition = 10)
  @FieldSelectorModel(singleValued = true)
  public String valueExpr;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.STRING,
      defaultValue = "",
      label = "Bin",
      description = "Bin to use for the value",
      displayPosition = 20)
  public String binName;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue = "DEFAULT",
      label = "Data Type",
      description = "The data type for the value",
      displayPosition = 30)
  @ValueChooserModel(DataTypeChooserValues.class)
  public DataType dataType = DataType.STRING;
}