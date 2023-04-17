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

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.util.SmartList
import javax.swing.Icon

data class Keymap_action_data
(
    var version: Int = 1,
    var text: String? = null,
    var description: String? = null,
    var action_ID: String? = null,
    var icon: Icon? = null,
    var shortcuts: List<KeyboardShortcut> = SmartList()
)