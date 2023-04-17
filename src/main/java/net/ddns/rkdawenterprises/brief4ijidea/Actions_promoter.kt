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

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import net.ddns.rkdawenterprises.brief4ijidea.actions.Plugin_action

fun should_promote(an_action: AnAction,
                   context: DataContext): Boolean
{
    val a_PSI_file: PsiFile?  = context.getData(CommonDataKeys.PSI_FILE);
    val an_editor: Editor? = context.getData(CommonDataKeys.EDITOR);

//    println("brief4ijidea.Actions_promoter.should_promote: ${an_action.toString()} : ${((an_action is Plugin_action) && (a_PSI_file != null) && (an_editor != null))}.");

    return ((an_action is Plugin_action) && (a_PSI_file != null) && (an_editor != null));
}

class Actions_promoter : ActionPromoter
{
    override fun promote(actions: MutableList<out AnAction>,
                         context: DataContext): MutableList<AnAction>
    {
        return actions.firstOrNull { should_promote(it, context); }
            ?.let { mutableListOf(it); }
            ?: mutableListOf();
    }
}