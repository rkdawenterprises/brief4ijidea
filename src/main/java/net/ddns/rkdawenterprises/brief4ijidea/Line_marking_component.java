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

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.has_selection;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.validate_position;

@SuppressWarnings({ "UnnecessaryReturnStatement", "UnnecessaryLocalVariable" })
public class Line_marking_component
{
    private static boolean s_is_line_marking_mode = false;
    private static LogicalPosition s_line_selection_origin = null;

    public static boolean is_line_marking_mode() { return s_is_line_marking_mode; }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean toggle_line_marking_mode( @NotNull Editor editor )
    {
        s_is_line_marking_mode = !s_is_line_marking_mode;
        if( s_is_line_marking_mode )
        {
            enable_line_marking_mode( editor );
        }
        else
        {
            stop_line_marking_mode( editor,
                                    true );
        }

        return s_is_line_marking_mode;
    }

    private static Key_adapter s_key_adapter = null;
    private static Mouse_adapter s_mouse_adapter = null;

    public static void enable_line_marking_mode( @NotNull Editor editor )
    {
        add_key_handlers( editor );

        s_is_line_marking_mode = true;

        State_component.status_bar_message( "<LINE-MARKING-MODE>" );

        editor.getCaretModel()
              .getCurrentCaret()
              .selectLineAtCaret();
        s_line_selection_origin = new LogicalPosition( EditorUtil.calcCaretLineRange( editor ).first.line,
                                                       0 );
        LogicalPosition end = adjust_line_marking_position( editor,
                                                            EditorUtil.calcCaretLineRange( editor ).second );
        editor.getCaretModel()
              .moveToLogicalPosition( end );
    }

    public static void stop_line_marking_mode( @NotNull Editor editor,
                                               boolean remove_selection )
    {
        s_is_line_marking_mode = false;
        s_line_selection_origin = null;

        State_component.status_bar_message( null );

        remove_key_handlers( editor );

        if( remove_selection )
        {
            if( has_selection( editor ) )
            {
                editor.getCaretModel()
                      .removeSecondaryCarets();
                editor.getSelectionModel()
                      .removeSelection();
            }
        }
    }

    private static void add_key_handlers( @NotNull Editor editor )
    {
        EditorActionManager editor_action_manager = EditorActionManager.getInstance();

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP_WITH_SELECTION ),
                                                                           KeyEvent.VK_UP ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN_WITH_SELECTION ),
                                                                           KeyEvent.VK_DOWN ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN_WITH_SELECTION ),
                                                                           KeyEvent.VK_RIGHT ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP_WITH_SELECTION ),
                                                                           KeyEvent.VK_LEFT ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP_WITH_SELECTION ),
                                                                           KeyEvent.VK_PAGE_UP ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN_WITH_SELECTION ),
                                                                           KeyEvent.VK_PAGE_DOWN ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_DELETE,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_DELETE ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_DELETE ),
                                                                           KeyEvent.VK_DELETE ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_BACKSPACE,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_BACKSPACE ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_BACKSPACE ),
                                                                           KeyEvent.VK_BACK_SPACE ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_ENTER,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_ENTER ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_ENTER ),
                                                                           KeyEvent.VK_ENTER ) );
        s_key_adapter = new Key_adapter( editor );
        editor.getContentComponent()
              .addKeyListener( s_key_adapter );

        s_mouse_adapter = new Mouse_adapter( editor );
        editor.getContentComponent()
              .addMouseListener( s_mouse_adapter );
    }

    private static void remove_key_handlers( @NotNull Editor editor )
    {
        editor.getContentComponent()
              .removeKeyListener( s_key_adapter );
        editor.getContentComponent()
              .removeMouseListener( s_mouse_adapter );

        EditorActionManager editor_action_manager = EditorActionManager.getInstance();

        EditorActionHandler handler;

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_DELETE );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_DELETE,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_BACKSPACE );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_BACKSPACE,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }

        handler = editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_ENTER );
        if( handler instanceof Editor_action_handler )
        {
            editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_ENTER,
                                                    ( (Editor_action_handler)handler ).m_original_handler );
        }
    }

    /**
     * Verifies the position is at column 0 and adjusts it there if not, unless it is on the last line of the document.
     * If it is already at column 0, it returns the given logical position.
     *
     * @param logical_position The logical position to possibly adjust to the beginning of the line.
     *
     * @return An adjusted (possibly) position.
     */
    private static @NotNull LogicalPosition adjust_line_marking_position( @NotNull Editor editor,
                                                                          @NotNull LogicalPosition logical_position )
    {
        if( logical_position.column == 0 ) return logical_position;

        int last_line = Math.max( 0,
                                  editor.getDocument()
                                        .getLineCount() - 1 );
        if( logical_position.line == last_line ) return logical_position;

        return new LogicalPosition( logical_position.line,
                                    0 );
    }

    /**
     * Verifies the caret position is at column 0 and adjusts it there if not, unless it is on the last line of the
     * document. If it is already at column 0, it returns the given caret's logical position.
     *
     * @param caret The caret to possibly adjust to the beginning of the line.
     *
     * @return An adjusted (possibly) caret position.
     */
    private static @NotNull LogicalPosition adjust_line_marking_caret( @NotNull Editor editor,
                                                                       @NotNull Caret caret )
    {
        if( caret.getLogicalPosition().column == 0 ) return caret.getLogicalPosition();

        int last_line = Math.max( 0,
                                  editor.getDocument()
                                        .getLineCount() - 1 );
        if( caret.getLogicalPosition().line == last_line ) return caret.getLogicalPosition();

        EditorActionUtil.moveCaretToLineStartIgnoringSoftWraps( editor );

        return caret.getLogicalPosition();
    }

    public static void line_marking_post_handler( @NotNull Editor editor,
                                                   int key_code )
    {
        if( ( key_code == KeyEvent.VK_DELETE ) ||
                ( key_code == KeyEvent.VK_BACK_SPACE ) ||
                ( key_code == KeyEvent.VK_ENTER ) )
        {
            stop_line_marking_mode( editor,
                                    false );
            return;
        }

        // TODO: "Up" from last line, if caret not at column 0, should place caret at beginning of last line...

        LogicalPosition caret_logical_position = adjust_line_marking_caret( editor,
                                                                            editor.getCaretModel()
                                                                                  .getCurrentCaret() );

        if( caret_logical_position.compareTo( s_line_selection_origin ) > 0 )
        {
            LogicalPosition start = s_line_selection_origin;
            LogicalPosition end = caret_logical_position;

            editor.getCaretModel()
                  .getCurrentCaret()
                  .setSelection( editor.logicalPositionToOffset( start ),
                                 editor.logicalPositionToOffset( end ) );
            return;
        }

        if( caret_logical_position.compareTo( s_line_selection_origin ) < 0 )
        {
            LogicalPosition start = caret_logical_position;
            LogicalPosition end = validate_position( editor,
                                                     new LogicalPosition( s_line_selection_origin.line + 1,
                                                                          0 ) );
            editor.getCaretModel()
                  .getCurrentCaret()
                  .setSelection( editor.logicalPositionToOffset( start ),
                                 editor.logicalPositionToOffset( end ) );
            return;
        }

        if( caret_logical_position.compareTo( s_line_selection_origin ) == 0 )
        {
            LogicalPosition start = s_line_selection_origin;
            LogicalPosition end = validate_position( editor,
                                                     new LogicalPosition( caret_logical_position.line + 1,
                                                                          0 ) );
            editor.getCaretModel()
                  .getCurrentCaret()
                  .setSelection( editor.logicalPositionToOffset( start ),
                                 editor.logicalPositionToOffset( end ) );
            return;
        }
    }

    public static final class Editor_action_handler
            extends EditorActionHandler
    {
        private final EditorActionHandler m_original_handler;
        private final EditorActionHandler m_substitute_handler;
        private final int m_key_code;

        public Editor_action_handler( @NotNull EditorActionHandler original_handler,
                                      @Nullable EditorActionHandler substitute_handler,
                                      int key_code )
        {
            m_original_handler = original_handler;
            m_substitute_handler = substitute_handler;
            m_key_code = key_code;
        }

        /**
         * Executes the action in the context of given caret. Subclasses should override this method.
         *
         * @param editor      the editor in which the action is invoked.
         * @param caret       the caret for which the action is performed at the moment, or {@code null} if it's a
         *                    'one-off' action executed without current context
         * @param dataContext the data context for the action.
         */
        @Override
        protected void doExecute( @NotNull Editor editor,
                                  @Nullable Caret caret,
                                  DataContext dataContext )
        {
            Objects.requireNonNullElse( m_substitute_handler,
                                        m_original_handler )
                   .execute( editor,
                             caret,
                             dataContext );

            line_marking_post_handler( editor,
                                       m_key_code );
        }
    }

    public static final class Key_adapter
            extends KeyAdapter
    {
        private final Editor m_editor;

        public Key_adapter( @NotNull Editor editor ) { m_editor = editor; }

        @Override
        public void keyTyped( KeyEvent e )
        {
            stop_line_marking_mode( m_editor,
                                    true );
        }
    }

    public static final class Mouse_adapter
            extends MouseAdapter
    {
        private final Editor m_editor;

        public Mouse_adapter( @NotNull Editor editor ) { m_editor = editor; }

        @Override
        public void mouseClicked( MouseEvent e )
        {
            stop_line_marking_mode( m_editor,
                                    true );
        }
    }
}
