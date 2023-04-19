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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;

import static net.ddns.rkdawenterprises.brief4ijidea.Actions_promoter.should_promote;

public class Miscellaneous
{
    public static void do_action( String action_ID,
                                  AnActionEvent an_action_event )
    {
        ActionManagerEx action_manager_ex = ActionManagerImpl.getInstanceEx();
        AnAction action = action_manager_ex.getAction(action_ID);
        ActionUtil.performActionDumbAwareWithCallbacks(action,
                an_action_event);

    }

    public static void do_action( String action_ID,
                                  AnActionEvent an_action_event,
                                  AnAction an_action )
    {
        if(!should_promote( an_action,
                            an_action_event.getDataContext() )
        ) return;
        ActionManagerEx action_manager_ex = ActionManagerImpl.getInstanceEx();
        AnAction action = action_manager_ex.getAction(action_ID);

        ActionUtil.performActionDumbAwareWithCallbacks(action,
                an_action_event);
    }
}