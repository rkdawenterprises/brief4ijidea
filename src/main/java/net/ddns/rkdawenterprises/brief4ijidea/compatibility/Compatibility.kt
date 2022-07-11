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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.util.SystemProperties.isTrueSmoothScrollingEnabled
import com.intellij.util.ui.UIUtil
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages
import net.ddns.rkdawenterprises.brief4ijidea.State_component
import java.awt.Rectangle
import javax.swing.Icon

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

fun virtual_space_setting_warning(editor: Editor)
{
    val do_not_show_virtual_space_setting_dialog = State_component.get_instance()
        .get_do_not_show_virtual_space_setting_dialog();
    if(!do_not_show_virtual_space_setting_dialog)
    {
        val editor_settings = editor.settings;
        if(!editor_settings.isVirtualSpace)
        {
            ApplicationManager.getApplication()
                .invokeLater {
                    warning_message("Change Settings for this Command",
                                    Localized_messages.message("you.must.enable.settings.editor.general.virtual.space.after.the.end.of.line.for.some.commands.right.side.of.window.and.column.marking.mode.to.work.properly"),
                                    object : DialogWrapper.DoNotAskOption.Adapter()
                                    {
                                        /**
                                         * Save the state of the checkbox in the settings, or perform some other related action.
                                         * This method is called right after the dialog is [closed][.close].
                                         * <br></br>
                                         * Note that this method won't be called in the case when the dialog is closed by [Cancel][.CANCEL_EXIT_CODE]
                                         * if [saving the choice on cancel is disabled][.shouldSaveOptionsOnCancel] (which is by default).
                                         *
                                         * @param isSelected true if user selected "don't show again".
                                         * @param exitCode   the [exit code][.getExitCode] of the dialog.
                                         * @see .shouldSaveOptionsOnCancel
                                         */
                                        /**
                                         * Save the state of the checkbox in the settings, or perform some other related action.
                                         * This method is called right after the dialog is [closed][.close].
                                         * <br></br>
                                         * Note that this method won't be called in the case when the dialog is closed by [Cancel][.CANCEL_EXIT_CODE]
                                         * if [saving the choice on cancel is disabled][.shouldSaveOptionsOnCancel] (which is by default).
                                         *
                                         * @param isSelected true if user selected "don't show again".
                                         * @param exitCode   the [exit code][.getExitCode] of the dialog.
                                         * @see .shouldSaveOptionsOnCancel
                                         */
                                        override fun rememberChoice(isSelected: Boolean,
                                                                    exitCode: Int)
                                        {
                                            State_component.get_instance()._do_not_show_virtual_space_setting_dialog = isSelected;
                                        }
                                    });
                }
        }
    }
}

fun warning_message(title: String?,
                    message: String,
                    option: DialogWrapper.DoNotAskOption.Adapter? = null): String?
{
    val button_name = "OK";

    return Message(title,
                   message)
        .buttons(button_name)
        .default_button(button_name)
        .focused_button(button_name)
        .do_not_ask(option)
        .as_warning()
        .show();
}

class Message internal constructor(private val title: String?,
                                   private val message: String)
{
    private var m_icon: Icon? = null;
    private var m_do_not_ask_option: DialogWrapper.DoNotAskOption? = null;
    private lateinit var m_buttons: List<String>;
    private var m_default_button_name: String? = null;
    private var m_focused_button_name: String? = null;

    fun icon(icon: Icon?): Message
    {
        m_icon = icon;
        return this;
    }

    fun as_warning(): Message
    {
        m_icon = UIUtil.getWarningIcon();
        return this;
    }

    fun do_not_ask(option: DialogWrapper.DoNotAskOption.Adapter?): Message
    {
        m_do_not_ask_option = option;
        return this;
    }

    fun buttons(vararg button_names: String): Message
    {
        m_buttons = button_names.toList();
        return this;
    }

    fun default_button(default_button_name: String): Message
    {
        m_default_button_name = default_button_name;
        return this;
    }

    fun focused_button(focused_button_name: String): Message
    {
        m_focused_button_name = focused_button_name;
        return this;
    }

    fun show(project: Project? = null): String?
    {
        val options = m_buttons.toTypedArray();
        val default_option_index = m_buttons.indexOf(m_default_button_name);
        val result = Messages.showIdeaMessageDialog(project,
                                                    message,
                                                    title,
                                                    options,
                                                    default_option_index,
                                                    m_icon,
                                                    m_do_not_ask_option);

        return if(result < 0) null else m_buttons[result];
    }
}
