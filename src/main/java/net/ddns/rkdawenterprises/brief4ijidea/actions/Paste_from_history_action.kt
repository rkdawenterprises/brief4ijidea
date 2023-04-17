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

@file:Suppress("RedundantSemicolon",
               "ComponentNotRegistered",
               "unused",
               "ClassName",
               "FunctionName",
               "HardCodedStringLiteral",
               "PrivatePropertyName")

package net.ddns.rkdawenterprises.brief4ijidea.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action
import net.ddns.rkdawenterprises.brief4ijidea.stop_all_marking_modes
import java.util.concurrent.atomic.AtomicBoolean

class Paste_from_history_action(text: String?,
                                description: String?) : Plugin_action(text,
                                                                      description)
{
    // Without this, remote robot may cause the dialog to open multiple times.
    private val dialog_is_open = AtomicBoolean(false);

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent)
    {
        if(dialog_is_open.get()) return;

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        dialog_is_open.set(true);
        do_action("PasteMultiple",
                  e)
        dialog_is_open.set(false);
        
        stop_all_marking_modes(editor,
                               false)
    }
}