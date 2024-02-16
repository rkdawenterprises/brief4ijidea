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
               "RedundantSemicolon",
               "PrivatePropertyName",
               "LocalVariableName",
               "PropertyName",
               "PackageName",
               "UnnecessaryVariable",
               "ArrayInDataClass")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*

object Marking_component
{
    private var s_is_marking_mode = false;
    private var s_is_marking_mode_noninclusive = false;
    private var s_selection_origin: LogicalPosition? = null;

    fun is_marking_mode(): Boolean
    {
        return s_is_marking_mode;
    }

    fun is_marking_mode_noninclusive(): Boolean
    {
        return s_is_marking_mode_noninclusive;
    }

    fun toggle_marking_mode(editor: Editor, is_noninclusive: Boolean): Boolean
    {
        if(s_is_marking_mode)
        {
            if(s_is_marking_mode_noninclusive == is_noninclusive)
            {
                stop_marking_mode(editor,
                                  true);
            }
            else
            {
                stop_marking_mode(editor,
                                  true);
                enable_marking_mode(editor, is_noninclusive);

            }
        }
        else
        {
            enable_marking_mode(editor, is_noninclusive);
        }

        return s_is_marking_mode;
    }

    private var s_key_adapter: Key_adapter? = null
    private var s_mouse_adapter: Mouse_adapter? = null

    private fun enable_marking_mode(editor: Editor, is_noninclusive: Boolean)
    {
        add_key_handlers(editor);

        s_is_marking_mode = true;
        s_is_marking_mode_noninclusive = is_noninclusive;
        s_selection_origin = editor.caretModel.logicalPosition;

        if(!is_noninclusive)
        {
            State_component.status_bar_message("<MARKING-MODE>");
            editor.caretModel.moveCaretRelatively(1,
                                                  0,
                                                  true,
                                                  false,
                                                  true);
        }
        else
        {
            State_component.status_bar_message("<NONINCLUSIVE-MARKING-MODE>");
        }
    }

    fun stop_marking_mode(editor: Editor,
                          remove_selection: Boolean)
    {
        s_is_marking_mode = false;
        s_is_marking_mode_noninclusive = false;
        s_selection_origin = null;

        State_component.status_bar_message(null);

        remove_key_handlers(editor);

        if(remove_selection)
        {
            if(has_selection(editor))
            {
                editor.caretModel.removeSecondaryCarets();
                editor.selectionModel.removeSelection();
            }
        }
    }

    private fun add_key_handlers(editor: Editor)
    {
        val editor_action_manager = EditorActionManager.getInstance();

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP_WITH_SELECTION),
                                                                     KeyEvent.VK_UP));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN_WITH_SELECTION),
                                                                     KeyEvent.VK_DOWN));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT_WITH_SELECTION),
                                                                     KeyEvent.VK_RIGHT));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT_WITH_SELECTION),
                                                                     KeyEvent.VK_LEFT));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP_WITH_SELECTION),
                                                                     KeyEvent.VK_PAGE_UP));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN_WITH_SELECTION),
                                                                     KeyEvent.VK_PAGE_DOWN));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_DELETE,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_DELETE),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_DELETE),
                                                                     KeyEvent.VK_DELETE));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE),
                                                                     KeyEvent.VK_BACK_SPACE));

        editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_ENTER,
                                               Editor_action_handler(editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER),
                                                                     editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER),
                                                                     KeyEvent.VK_ENTER));

        // TODO: Handle cursor movement commands also (or add actions for those)...
        s_key_adapter = Key_adapter(editor);
        editor.contentComponent.addKeyListener(s_key_adapter);

        s_mouse_adapter = Mouse_adapter(editor);
        editor.contentComponent.addMouseListener(s_mouse_adapter);
    }

    private fun remove_key_handlers(editor: Editor)
    {
        editor.contentComponent.removeKeyListener(s_key_adapter);
        editor.contentComponent.removeMouseListener(s_mouse_adapter);

        val editor_action_manager = EditorActionManager.getInstance();

        var handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_DELETE);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_DELETE,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_BACKSPACE,
                                                   handler.m_original_handler);
        }

        handler = editor_action_manager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER);
        if(handler is Editor_action_handler)
        {
            editor_action_manager.setActionHandler(IdeActions.ACTION_EDITOR_ENTER,
                                                   handler.m_original_handler);
        }
    }

    @JvmStatic
    fun marking_post_handler(editor: Editor,
                             key_code: Int)
    {
        if((key_code == KeyEvent.VK_DELETE) || (key_code == KeyEvent.VK_BACK_SPACE) || (key_code == KeyEvent.VK_ENTER))
        {
            stop_marking_mode(editor,
                              false);
            return
        }

        val caret_logical_position = editor.caretModel.currentCaret.logicalPosition;

        if(caret_logical_position.compareTo(s_selection_origin) > 0)
        {
            val start = s_selection_origin;
            val end = caret_logical_position;

            editor.caretModel.currentCaret.setSelection(editor.logicalPositionToOffset(start!!),
                                                        editor.logicalPositionToOffset(end));
            return;
        }

        if(caret_logical_position.compareTo(s_selection_origin) < 0)
        {
            val start = caret_logical_position;
            val end = s_selection_origin;

            editor.caretModel.currentCaret.setSelection(editor.logicalPositionToOffset(start),
                                                        editor.logicalPositionToOffset(end!!));
            return;
        }

        if(caret_logical_position.compareTo(s_selection_origin) == 0)
        {
            val start = s_selection_origin;
            val end = caret_logical_position;

            editor.caretModel.currentCaret.setSelection(editor.logicalPositionToOffset(start!!),
                                                        editor.logicalPositionToOffset(end));
            return;
        }
    }

    class Editor_action_handler(val m_original_handler: EditorActionHandler,
                                private val m_substitute_handler: EditorActionHandler?,
                                private val m_key_code: Int) : EditorActionHandler()
    {
        /**
         * Executes the action in the context of given caret. Subclasses should override this method.
         *
         * @param editor      the editor in which the action is invoked.
         * @param caret       the caret for which the action is performed at the moment, or `null` if it's a
         * 'one-off' action executed without current context
         * @param dataContext the data context for the action.
         */
        override fun doExecute(editor: Editor,
                               caret: Caret?,
                               dataContext: DataContext)
        {
            val handler: EditorActionHandler = Objects.requireNonNullElse(m_substitute_handler,
                                                                          m_original_handler);
            handler.execute(editor,
                            caret,
                            dataContext);

            marking_post_handler(editor,
                                 m_key_code);
        }
    }

    class Key_adapter(private val m_editor: Editor) : KeyAdapter()
    {
        override fun keyTyped(e: KeyEvent)
        {
            stop_marking_mode(m_editor,
                              true);
        }
    }

    class Mouse_adapter(private val m_editor: Editor) : MouseAdapter()
    {
        override fun mouseClicked(e: MouseEvent)
        {
            stop_marking_mode(m_editor,
                              true);
        }
    }
}
