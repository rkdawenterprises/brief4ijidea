/*
 * Copyright (c) 2019-2022 RKDAW Enterprises and Ralph Williamson
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

@file:Suppress("ClassName",
               "FunctionName",
               "RedundantSemicolon",
               "PrivatePropertyName",
               "LocalVariableName",
               "PropertyName",
               "PackageName")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class Startup_activity : ProjectActivity
{
    private var m_initialized = false;

    override suspend fun execute(project: Project)
    {
        if(m_initialized && State_component.enabled())
        {
            println("brief4ijidea.Startup_activity.execute: Project scope already initialized.")
        }

        if(m_initialized) return;

        m_initialized = true;

        State_component.get_instance().initialize()
    }
}
