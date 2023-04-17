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
               "RedundantSemicolon")

package net.ddns.rkdawenterprises.brief4ijidea.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages
import net.ddns.rkdawenterprises.brief4ijidea.State_component

class Enabled_toggle_action : DumbAwareToggleAction(Localized_messages.message("action.brief.editor.emulator.text"),
                                                    Localized_messages.message("action.enable.or.disable.brief.editor.emulation.plugin.description"),
                                                    null)
{
    /**
     * Returns the selected (checked, pressed) state of the action.
     *
     * @param e the action event representing the place and context in which the selected state is queried.
     *
     * @return true if the action is selected, false otherwise
     */
    override fun isSelected(e: AnActionEvent): Boolean
    {
        return State_component.enabled();
    }

    /**
     * Sets the selected state of the action to the specified value.
     *
     * @param e     the action event which caused the state change.
     * @param state the new selected state of the action.
     */
    override fun setSelected(e: AnActionEvent,
                             state: Boolean)
    {
        try
        {
            State_component.enable(state);
        }
        catch(ignored: Exception){}
    }
}