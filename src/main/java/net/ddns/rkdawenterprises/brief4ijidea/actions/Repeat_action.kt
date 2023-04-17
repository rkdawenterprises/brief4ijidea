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
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiFile
import com.intellij.util.ui.JBUI
import net.ddns.rkdawenterprises.brief4ijidea.Key_event_to_string.Companion.to_string
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages
import net.ddns.rkdawenterprises.brief4ijidea.State_component
import net.ddns.rkdawenterprises.brief4ijidea.stop_all_marking_modes
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.text.BadLocationException

class Repeat_action(text: String?,
                    description: String?) : Plugin_action(text,
                                                          description)
{
    // Without this, remote robot may cause the dialog to open multiple times.
    private val dialog_is_open = AtomicBoolean(false);

    init
    {
        isEnabledInModalContext = true;
    }

    /**
     * Updates the state of the action. Default implementation does nothing. Override this method to provide the ability
     * to dynamically change action's state and(or) presentation depending on the context (For example when your action
     * state depends on the selection you can check for selection and change the state accordingly).
     *
     *
     *
     *
     * This method can be called frequently, and on UI thread. This means that this method is supposed to work really
     * fast, no real work should be done at this phase. For example, checking selection in a tree or a list, is
     * considered valid, but working with a file system or PSI (especially resolve) is not. If you cannot determine the
     * state of the action fast enough, you should do it in the [.actionPerformed] method and
     * notify the user that action cannot be executed if it's the case.
     *
     *
     *
     *
     * If the action is added to a toolbar, its "update" can be called twice a second, but only if there was any user
     * activity or a focus transfer. If your action's availability is changed in absence of any of these events, please
     * call `ActivityTracker.getInstance().inc()` to notify action subsystem to update all toolbar actions when
     * your subsystem's determines that its actions' visibility might be affected.
     *
     * @param e Carries information on the invocation place and data available
     */
    override fun update(e: AnActionEvent)
    {
        val presentation = e.presentation;
        val project = e.getData(CommonDataKeys.PROJECT);
        if(project == null)
        {
            presentation.isEnabledAndVisible = false;
            return;
        }
        val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        presentation.isEnabledAndVisible = editor != null;
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    override fun actionPerformed(e: AnActionEvent)
    {
        if(dialog_is_open.get()) return;

        dialog_is_open.set(true);

        val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return;
        val project = e.getData(CommonDataKeys.PROJECT) ?: return;
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return;

        if(java.lang.Boolean.TRUE == e.getData(PlatformDataKeys.IS_MODAL_CONTEXT))
        {
            val dialog = Repeat_action_dialog(project,
                                              editor,
                                              e.presentation
                                                  .text,
                                              file);
            dialog.show();
        }
        else
        {
            val processor = CommandProcessor.getInstance();
            processor.executeCommand(
                project,
                {
                    val dialog = Repeat_action_dialog(project,
                                                      editor,
                                                      e.presentation
                                                          .text,
                                                      file);
                    dialog.show();
                    IdeDocumentHistory.getInstance(project)
                        .includeCurrentCommandAsNavigation();
                },
                Repeat_action_dialog.m_title,
                null);
        }
        
        dialog_is_open.set(false);
    }
}

internal class Repeat_action_dialog(private val m_project: Project,
                                    private val m_editor: Editor,
                                    private val m_action_text: String,
                                    private val m_file: PsiFile) : DialogWrapper(m_project,
                                                                                 true)
{
    private var m_count = 1;
    private var m_j_text_field: JTextField? = null;
    private var m_got_initial_focus = false;
    private fun setup()
    {
        title = m_title;
        m_j_text_field!!.text = String.format(m_command_prompt_format,
                                              m_count,
                                              m_command_prompt_initial_instructions);

        m_j_text_field!!.addFocusListener(object : FocusListener
                                          {
                                              override fun focusGained(focusEvent: FocusEvent)
                                              {
                                                  // Only doing this once per instance.
                                                  if(m_got_initial_focus) return;
                                                  m_got_initial_focus = true;
                                                  @NonNls
                                                  val start = m_command_prompt_format.indexOf("%d");
                                                  @NonNls
                                                  val end = start + String.format("%d",
                                                                                  m_count).length;
                                                  m_j_text_field!!.select(start,
                                                                          end)
                                              }

                                              override fun focusLost(focusEvent: FocusEvent)
                                              {
                                              }
                                          });

        m_j_text_field!!.addKeyListener(object : KeyAdapter()
                                        {
                                            var consumed = false;
                                            override fun keyPressed(e: KeyEvent)
                                            {
                                                consumed = key_filter(e);
                                                if(consumed) e.consume();
                                            }

                                            override fun keyTyped(e: KeyEvent)
                                            {
                                                if(consumed) e.consume();
                                            }

                                            override fun keyReleased(e: KeyEvent)
                                            {
                                                if(consumed) e.consume();
                                                consumed = false;
                                            }
                                        });
    }

    /**
     * Factory method. It creates the panel located at the north of the dialog's content pane. The implementation can
     * return `null` value. In this case there will be no input panel.
     *
     * @return north panel
     */
    override fun createNorthPanel(): JComponent
    {
        val panel = JPanel(GridBagLayout());
        val grid_bag_constraints = GridBagConstraints();
        grid_bag_constraints.insets = JBUI.insets(4,
                                                  0,
                                                  8,
                                                  8);
        grid_bag_constraints.weighty = 1.0;
        grid_bag_constraints.anchor = GridBagConstraints.EAST;
        grid_bag_constraints.fill = GridBagConstraints.BOTH;
        grid_bag_constraints.weightx = 1.0;
        m_j_text_field = JTextField();
        panel.add(m_j_text_field as Component,
                  grid_bag_constraints);
        return panel;
    }

    /**
     * @return component which should be focused when the dialog appears on the screen.
     */
    override fun getPreferredFocusedComponent(): JComponent?
    {
        return m_j_text_field;
    }

    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the center of the dialog's
     * content pane. The implementation can return `null` value. In this case there will be no options panel.
     */
    override fun createCenterPanel(): JComponent?
    {
        return null;
    }

    /**
     * This method is invoked by default implementation of "OK" action. It just closes dialog with `OK_EXIT_CODE`.
     * This is convenient place to override functionality of "OK" action. Note that the method does nothing if "OK"
     * action isn't enabled.
     */
    override fun doOKAction()
    {
        try
        {
            val prefix = String.format(m_command_prompt_format,
                                       m_count,
                                       "");
            val document = m_j_text_field!!.document;
            var repeat_string = document.getText(0,
                                                 document.length)
                .replace(prefix,
                         "");

            if(repeat_string == m_command_prompt_initial_instructions || repeat_string == m_command_prompt_final_instructions) return;

            @NonNls
            repeat_string = repeat_string.replace("\\n",
                                                  "\n")
                .replace("\\r",
                         "\r")
                .replace("\\t",
                         "\t")
                .replace("\\n",
                         "\n")
                .replace("\\\\",
                         "\\")
                .replace("\\b",
                         "\b")
                .replace("\\u000c",
                         "\u000c")
            val result = StringBuilder(repeat_string)
            if(m_count > 1)
            {
                for(i in 1 until m_count)
                {
                    result.append(repeat_string)
                }
            }
            stop_all_marking_modes(m_editor)
            WriteCommandAction.runWriteCommandAction(m_project,
                                                     m_action_text,
                                                     null,
                                                     {
                                                         m_editor.document
                                                             .insertString(m_editor.caretModel
                                                                               .currentCaret
                                                                               .offset,
                                                                           result.toString())
                                                     },
                                                     m_file)
            IdeFocusManager.getGlobalInstance()
                .requestFocus(m_editor.contentComponent,
                              true)
        }
        catch(e: BadLocationException)
        {
            throw RuntimeException(e)
        }
        finally
        {
            super.doOKAction()
        }
    }

    private fun update_count()
    {
        if(m_count > MAX_COMMAND_COUNT) m_count = MAX_COMMAND_COUNT
        if(m_count < 1) m_count = 1
        m_j_text_field!!.text = String.format(m_command_prompt_format,
                                              m_count,
                                              m_command_prompt_initial_instructions)
        @NonNls
        val start = m_command_prompt_format.indexOf("%d")
        @NonNls
        val end = start + String.format("%d",
                                        m_count).length
        m_j_text_field!!.select(start,
                                end)
    }

    /**
     * The key event processor states.
     */
    private enum class COMMAND_STATE
    {
        IDLE, COUNT_ACCEPTED, ACCUMULATE_STRING
    }

    /**
     * The key event processor current state.
     */
    private var m_command_state = COMMAND_STATE.IDLE

    init
    {
        m_got_initial_focus = false
        init()
        setup()
    }

    /**
     * State machine that processes the key events.
     * Only supports single keystroke events.
     *
     * @param e The Key Event.
     *
     * @return True if the key event should be consumed, i.e. not passed on to the text field.
     */
    private fun key_filter(e: KeyEvent): Boolean
    {
        val key_string = e.to_string()
            .replace("[\\[\\]\"]".toRegex(),
                     "");

        val key: Array<String> = key_string
            .replace("[<>]".toRegex(),
                     "")
            .split(",")
            .toTypedArray();

        @NonNls
        if(!key[0].equals("KEY_PRESSED",
                          ignoreCase = true)) return false;

        println("key=<${key[1]}>,<${key[2]}>")
        println("state=$m_command_state")

        when(m_command_state)
        {
            COMMAND_STATE.IDLE ->
            {
                // Ignore events that are just modifiers.
                @NonNls
                if(key[2].equals("ALT",
                                 ignoreCase = true) ||
                    key[2].equals("CTRL",
                                  ignoreCase = true) ||
                    key[2].equals("SHIFT",
                                  ignoreCase = true) ||
                    key[2].equals("META",
                                  ignoreCase = true) ||
                    key[2].equals("NUM LOCK",
                                  ignoreCase = true) ||
                    key[2].equals("CAPS LOCK",
                                  ignoreCase = true))
                {
                    return false;
                }

                // Consume unmodified LEFT arrow key.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("LEFT",
                                                                     ignoreCase = true))
                {
                    return true;
                }

                // Look for CTRL + R to double the count.
                if(key[1].equals("CTRL",
                                 ignoreCase = true) && key[2].equals("R",
                                                                     ignoreCase = true))
                {
                    m_count *= 2;
                    update_count();
                    return true;
                }

                // Look for unmodified UP arrow key to increment the count.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("UP",
                                                                     ignoreCase = true))
                {
                    m_count++;
                    update_count();
                    return true;
                }

                // Look for unmodified DOWN arrow key to decrement the count.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("DOWN",
                                                                     ignoreCase = true))
                {
                    m_count--;
                    update_count();
                    return true;
                }

                // Look for unmodified BACKSPACE key to adjust the count.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("BACKSPACE",
                                                                     ignoreCase = true))
                {
println("backspace)")
                    if(m_count < 10)
                    {
                        m_count = 1;
                    }
                    else
                    {
                        m_count /= 10;
                    }

                    update_count();

                    return true;
                }

                // Look for unmodified DELETE key to adjust the count.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("DELETE",
                                                                     ignoreCase = true))
                {
                    m_count = 1;
                    update_count();
                    return true;
                }

                // Look for unmodified number key to adjust the count.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].length == 1 && Character.isDigit(key[2][0]))
                {
                    val value = key[2].toInt();

                    if(m_count == 1 && value != 1 && value != 0)
                    {
                        m_count = value;
                    }
                    else
                    {
                        m_count *= 10;
                        m_count += value;
                    }

                    update_count();

                    return true;
                }

                // Look for unmodified right arrow key to type command or string to repeat.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) && key[2].equals("RIGHT",
                                                                     ignoreCase = true))
                {
                    m_j_text_field!!.text = String.format(m_command_prompt_format,
                                                          m_count,
                                                          m_command_prompt_final_instructions);

                    val prefix = String.format(m_command_prompt_format,
                                               m_count,
                                               "");
                    val start = prefix.length;
                    val end = m_j_text_field!!.document
                        .length;
                    m_j_text_field!!.select(start,
                                            end);

                    m_command_state = COMMAND_STATE.COUNT_ACCEPTED;

                    return true;
                }

                return true;
            }

            COMMAND_STATE.COUNT_ACCEPTED ->
            {
                // Ignore events that are just modifiers.
                if(key[2].equals("ALT",
                                 ignoreCase = true) ||
                    key[2].equals("CTRL",
                                  ignoreCase = true) ||
                    key[2].equals("SHIFT",
                                  ignoreCase = true) ||
                    key[2].equals("META",
                                  ignoreCase = true) ||
                    key[2].equals("NUM LOCK",
                                  ignoreCase = true) ||
                    key[2].equals("CAPS LOCK",
                                  ignoreCase = true))
                {
                    return false;
                }

                // Look for a registered command to repeat.
                val lookup = "<${key[1]}>,<${key[2]}>";
                val map = State_component.get_instance()._active_keystroke_map;
                if( map.containsKey(lookup) )
                {
                    return true;
                }

                // Look for unmodified (except SHIFT) non-numerical printable character that will begin a string to repeat.
                if((key[1].equals("UNMODIFIED",
                                  ignoreCase = true) || key[1].equals("SHIFT",
                                                                      ignoreCase = true)) &&
                    !Character.isDigit(key[2][0]) && (key[2].length == 1))
                {
                    val prefix = String.format(m_command_prompt_format,
                                               m_count,
                                               "");
                    val start = prefix.length
                    val end = m_j_text_field!!.text
                        .length;
                    m_j_text_field!!.select(start,
                                            end);

                    m_command_state = COMMAND_STATE.ACCUMULATE_STRING;

                    return false;
                }

                return true;
            }

            COMMAND_STATE.ACCUMULATE_STRING ->
            {
                val prefix = String.format(m_command_prompt_format,
                                           m_count,
                                           "");

                // Don't allow caret to get left of the start of the repeat string.
                if(key[1].equals("UNMODIFIED",
                                 ignoreCase = true) &&
                    (key[2].equals("LEFT",
                                   ignoreCase = true) || key[2].equals("BACKSPACE",
                                                                       ignoreCase = true)) &&
                    (m_j_text_field!!.caretPosition <= prefix.length))
                {
                    return true;
                }

                return false;
            }
        }
    }

    companion object
    {
        val m_title = Localized_messages.message("repeat.string.or.command");
        val m_command_prompt_format = Localized_messages.message("repeat.count.d.s");
        val m_command_prompt_initial_instructions = Localized_messages.message("modify.count.then.right.arrow");
        val m_command_prompt_final_instructions = Localized_messages.message("type.command.or.string");
        const val MAX_COMMAND_COUNT = 1024;
    }
}
