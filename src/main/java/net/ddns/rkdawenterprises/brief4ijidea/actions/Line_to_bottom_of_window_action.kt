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
               "LocalVariableName",
               "unused",
               "RedundantSemicolon",
               "ComponentNotRegistered",
               "PrivatePropertyName")

package net.ddns.rkdawenterprises.brief4ijidea.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import net.ddns.rkdawenterprises.brief4ijidea.get_bottom_of_window_line_number
import net.ddns.rkdawenterprises.brief4ijidea.scroll_lines

class Line_to_bottom_of_window_action(text: String? = null,
                                      description: String? = null) : Plugin_action(text,
                                                                                   description)
{
    init
    {
        isEnabledInModalContext = true;
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent)
    {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return;

        val caret_position_visual = editor.caretModel.visualPosition;
        val lines_to_bottom_of_window = get_bottom_of_window_line_number(editor) - caret_position_visual.line;

        scroll_lines(editor,
                     lines_to_bottom_of_window);
    }
}
