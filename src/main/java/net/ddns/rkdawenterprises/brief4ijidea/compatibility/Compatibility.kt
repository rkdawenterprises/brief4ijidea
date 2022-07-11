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

@file:Suppress("FunctionName",
               "LocalVariableName",
               "PrivatePropertyName",
               "HardCodedStringLiteral",
               "unused",
               "RedundantSemicolon",
               "UsePropertyAccessSyntax",
               "KDocUnresolvedReference")

package net.ddns.rkdawenterprises.brief4ijidea.compatibility

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCoreUtil.isTrueSmoothScrollingEnabled
import java.awt.Rectangle

fun get_editor_content_visible_area(editor: Editor): Rectangle
{
    val model = editor.scrollingModel
    return if(isTrueSmoothScrollingEnabled()) model.visibleAreaOnScrollingFinished else model.visibleArea;
}

fun perform_action_dumb_aware_with_callbacks(an_action: AnAction,
                                             an_action_event: AnActionEvent)
{
    ActionUtil.performActionDumbAwareWithCallbacks(an_action,
                                                   an_action_event,
                                                   an_action_event.dataContext);
}
