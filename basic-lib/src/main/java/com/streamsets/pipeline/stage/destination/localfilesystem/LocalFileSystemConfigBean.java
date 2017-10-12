/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.stage.destination.localfilesystem;

import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.config.DataFormat;
import com.streamsets.pipeline.stage.destination.hdfs.HdfsFileType;
import com.streamsets.pipeline.stage.destination.hdfs.HdfsTargetConfigBean;

import java.util.LinkedList;
import java.util.List;

public class LocalFileSystemConfigBean extends HdfsTargetConfigBean {

  public LocalFileSystemConfigBean() {
    // forcing local file system
    hdfsUri = "file:///";
    // as the config is hidden, it is not set, we need to initialize it by hand.
    hdfsUser = "";
    hdfsConfigs = new LinkedList<>();
    fileType = HdfsFileType.TEXT;
  }

  @Override
  protected String getTargetConfigBeanPrefix() {
    return "configs.";
  }

  @Override
  protected void validateStageForWholeFileFormat(Stage.Context context, List<Stage.ConfigIssue> issues) {
    if (dataFormat == DataFormat.WHOLE_FILE) {
      fileType = HdfsFileType.WHOLE_FILE;
    }
    super.validateStageForWholeFileFormat(context, issues);
  }

}
