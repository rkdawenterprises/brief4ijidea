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

package net.ddns.rkdawenterprises.brief4ijidea;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import net.ddns.rkdawenterprises.brief4ijidea.actions.Plugin_action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Actions_promoter implements ActionPromoter {
    public static boolean should_promote(AnAction an_action,
                                         DataContext context) {
        PsiFile a_PSI_file = context.getData(CommonDataKeys.PSI_FILE);
        Editor an_editor = context.getData(CommonDataKeys.EDITOR);

//    println("brief4ijidea.Actions_promoter.should_promote: ${an_action.toString()} : ${((an_action is Plugin_action) && (a_PSI_file != null) && (an_editor != null))}.");

        return ((an_action instanceof Plugin_action) && (a_PSI_file != null) && (an_editor != null));
    }

    @Override
    public @Nullable List<AnAction> promote(@NotNull List<? extends AnAction> actions, @NotNull DataContext context) {
        AnAction action = ContainerUtil.findInstance(actions, Plugin_action.class);
        return (action != null) ? Collections.singletonList(action) : Collections.emptyList();
    }
}