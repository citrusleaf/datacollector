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
package com.streamsets.pipeline.main;


import com.streamsets.pipeline.http.WebServerTask;
import com.streamsets.pipeline.prodmanager.PipelineProductionManagerTask;
import com.streamsets.pipeline.stagelibrary.StageLibraryTask;
import com.streamsets.pipeline.store.PipelineStoreTask;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestPipelineTask {

  @Test
  public void testPipelineAgentDelegation() {
    StageLibraryTask library = Mockito.mock(StageLibraryTask.class);
    PipelineStoreTask store = Mockito.mock(PipelineStoreTask.class);
    WebServerTask webServer = Mockito.mock(WebServerTask.class);
    PipelineProductionManagerTask pipelineManager = Mockito.mock(PipelineProductionManagerTask.class);
    PipelineTask task = new PipelineTask(library, store, pipelineManager, webServer);
    Assert.assertEquals("pipelineNode", task.getName());
  }

}
