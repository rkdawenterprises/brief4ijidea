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

package net.ddns.rkdawenterprises.brief4ijidea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import net.ddns.rkdawenterprises.brief4ijidea.Line_marking_component;
import net.ddns.rkdawenterprises.brief4ijidea.Marking_component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;

import static net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.get_editor_content_visible_area;

@SuppressWarnings({ "ComponentNotRegistered", "unused" })
public class End_of_line_action
        extends Plugin_action
{
    public End_of_line_action( String text,
                               String description )
    {
        super( text,
               description );
    }

    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed( @NotNull AnActionEvent e )
    {
        Editor editor = e.getData( CommonDataKeys.EDITOR );
        if( editor == null ) return;

        final LogicalPosition caret_logical_position = editor.getCaretModel()
                                                             .getLogicalPosition();

        final VisualPosition caret_visual_position = editor.getCaretModel()
                                                           .getVisualPosition();

        int caret_logical_offset = editor.getCaretModel()
                                         .getOffset();

        int caret_visual_offset = editor.visualPositionToOffset( caret_visual_position );

        boolean at_file_end = ( caret_logical_offset == editor.getDocument()
                                                              .getTextLength() );
        if( at_file_end ) { return; }

        Rectangle visible_area = get_editor_content_visible_area( editor );
        int max_Y = visible_area.y + visible_area.height - editor.getLineHeight();
        int visible_area_bottom_line_number = editor.yToVisualLine( max_Y );
        if( ( visible_area_bottom_line_number > 0 ) &&
                ( max_Y < editor.visualLineToY( visible_area_bottom_line_number ) ) &&
                ( visible_area.y <= editor.visualLineToY( visible_area_bottom_line_number - 1 ) ) )
        {
            visible_area_bottom_line_number--;
        }

        // Because of the way the editor scrolls, sometimes can't get to the bottom of the window.
        // So we need to consider the previous line as the bottom of the window also.
        int visible_area_bottom_line_alternate_number = visible_area_bottom_line_number - 1;
        if( visible_area_bottom_line_alternate_number < 0 ) visible_area_bottom_line_alternate_number = visible_area_bottom_line_number;

        VisualPosition visible_area_bottom_end_of_line_position
                = new VisualPosition( visible_area_bottom_line_number,
                                      EditorUtil.getLastVisualLineColumnNumber( editor,
                                                                                visible_area_bottom_line_number ),
                                      true );
        VisualPosition visible_area_bottom_alternate_end_of_line_position
                = new VisualPosition( visible_area_bottom_line_alternate_number,
                                      EditorUtil.getLastVisualLineColumnNumber( editor,
                                                                                visible_area_bottom_line_alternate_number ),
                                      true );

        int last_line_number = editor.getDocument()
                                     .getLineNumber( editor.getDocument()
                                                           .getTextLength() );

        boolean at_window_end = caret_visual_position.equals( visible_area_bottom_end_of_line_position ) ||
                caret_visual_position.equals( visible_area_bottom_alternate_end_of_line_position ) ||
                ( Line_marking_component.is_line_marking_mode() &&
                        ( caret_visual_position.line == visible_area_bottom_end_of_line_position.line ) );
        if( at_window_end )
        {
            if( Marking_component.INSTANCE.is_marking_mode() )
            {
                do_action( "EditorTextEndWithSelection", e );
                return;
            }

            if( Line_marking_component.is_line_marking_mode() )
            {
                do_action( "EditorTextEndWithSelection", e );
                Line_marking_component.line_marking_post_handler( editor,
                                                                  KeyEvent.VK_HOME );
                return;
            }
// TODO: Account for column marking mode...
            do_action( "EditorTextEnd", e );
            return;
        }

        VisualPosition carets_current_line_end_position
                = new VisualPosition( caret_visual_position.line,
                                      EditorUtil.getLastVisualLineColumnNumber( editor,
                                                                                caret_visual_position.line ),
                                      true );

        boolean at_line_end = caret_visual_position.equals( carets_current_line_end_position ) ||
                ( Line_marking_component.is_line_marking_mode() &&
                        ( caret_logical_position.line != last_line_number ) );
        if( at_line_end )
        {
            if( Marking_component.INSTANCE.is_marking_mode() )
            {
                do_action( "EditorMoveToPageBottomWithSelection", e );
                return;
            }

            if( Line_marking_component.is_line_marking_mode() )
            {
                do_action( "EditorMoveToPageBottomWithSelection", e );
                Line_marking_component.line_marking_post_handler( editor,
                                                                  KeyEvent.VK_END );

                return;
            }

// TODO: Account for column marking mode...
            do_action( "EditorMoveToPageBottom", e );
            do_action( "EditorLineEnd", e );
            return;
        }

        if( Marking_component.INSTANCE.is_marking_mode() )
        {
            do_action( "EditorLineEndWithSelection", e );
            return;
        }

        if( Line_marking_component.is_line_marking_mode() &&
                ( caret_logical_position.line == last_line_number ) )
        {
            do_action( "EditorLineEndWithSelection",
                                         e );
            Line_marking_component.line_marking_post_handler( editor,
                                                              KeyEvent.VK_END );
            return;
        }

// TODO: Account for column marking mode...
        do_action( "EditorLineEnd", e );
    }
}
