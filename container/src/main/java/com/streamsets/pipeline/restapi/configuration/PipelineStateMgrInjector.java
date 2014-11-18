/**
 * Licensed to the Apache Software Foundation (ASF) under one
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
package com.streamsets.pipeline.restapi.configuration;

import com.streamsets.pipeline.prodmanager.PipelineProductionManagerTask;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class PipelineStateMgrInjector implements Factory<PipelineProductionManagerTask> {

  public static final String PIPELINE_STATE_MGR = "pipeline-state-mgr";
  private PipelineProductionManagerTask stateMgr;

  @Inject
  public PipelineStateMgrInjector(HttpServletRequest request) {
    stateMgr = (PipelineProductionManagerTask) request.getServletContext().getAttribute(PIPELINE_STATE_MGR);
  }

  @Override
  public PipelineProductionManagerTask provide() {
    return stateMgr;
  }

  @Override
  public void dispose(PipelineProductionManagerTask pipelineStore) {
  }
}
