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

@file:Suppress("ClassName")

package net.ddns.rkdawenterprises.brief4ijidea.actions

import com.intellij.openapi.project.DumbAwareAction

/**
 * Parent for all plugin defined actions so they can be identified by the Actions Promoter.
 *
 * @constructor
 *
 * @param text
 * @param description
 */
abstract class Plugin_action(text: String?,
                             description: String?) : DumbAwareAction(text,
                                                                     description,
                                                                     null)
