/*
 * Copyright (c) 2019-2026 RKDAW Enterprises and Ralph Williamson
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

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.time.toKotlinDuration

@Service(Service.Level.PROJECT)
class Status_bar_messages_timer(private val project: Project,
                                private val coroutine_scope: CoroutineScope)
{
    companion object
    {
        val MESSAGE_PERSISTENT_DURATION: Duration = Duration.ofMinutes(5);
        val MESSAGE_TEMPORARY_DURATION: Duration = Duration.ofSeconds(10);
    }

    var running_job: Job? = null;

    fun schedule_timer(duration: Duration = MESSAGE_TEMPORARY_DURATION,
                       action: () -> Unit?)
    {
        running_job?.cancel();
        running_job = coroutine_scope.launch()
        {
            delay(duration.toKotlinDuration());
            action();
        }
    }

    fun cancel_timer()
    {
        running_job?.cancel();
    }
}
