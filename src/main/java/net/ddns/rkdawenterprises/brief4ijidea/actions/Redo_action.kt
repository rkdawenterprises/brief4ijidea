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
               "FunctionName")

package net.ddns.rkdawenterprises.brief4ijidea.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.undo.UndoManager
import net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action
import net.ddns.rkdawenterprises.brief4ijidea.get_undo_manager

class Redo_action(text: String?,
                  description: String?) : Plugin_action(text,
                                                        description)
{
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent)
    {
        val dataContext: DataContext = e.dataContext
        val editor = PlatformDataKeys.FILE_EDITOR.getData(dataContext)
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val undoManager: UndoManager = get_undo_manager(project)
        if(undoManager.isRedoAvailable(editor))
        {
            do_action("\$Redo",
                      e,
                      this);
        }
    }
}