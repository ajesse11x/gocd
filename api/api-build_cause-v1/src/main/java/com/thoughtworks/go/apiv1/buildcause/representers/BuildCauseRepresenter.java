/*
 * Copyright 2018 ThoughtWorks, Inc.
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

package com.thoughtworks.go.apiv1.buildcause.representers;

import com.thoughtworks.go.api.base.OutputWriter;
import com.thoughtworks.go.domain.buildcause.BuildCause;

public class BuildCauseRepresenter {

    public static void toJSON(OutputWriter jsonOutputWriter, BuildCause model) {
        jsonOutputWriter
            .add("approver", model.getApprover())
            .add("is_forced", model.isForced())
            .add("trigger_message", model.getBuildCauseMessage())
            .addChildList("material_revisions", listWriter -> model.getMaterialRevisions().forEach(materialRevision -> listWriter.addChild(childWriter -> MaterialRevisionRepresenter.toJSON(childWriter, materialRevision))));
    }
}