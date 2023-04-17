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
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import net.ddns.rkdawenterprises.brief4ijidea.Line_marking_component;
import net.ddns.rkdawenterprises.brief4ijidea.Marking_component;
import net.ddns.rkdawenterprises.brief4ijidea.State_component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;

import static net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.get_editor_content_visible_area;

@SuppressWarnings({ "ComponentNotRegistered", "unused", "UnnecessaryReturnStatement" })
public class Beginning_of_line_action
        extends Plugin_action
{
    public Beginning_of_line_action( String text,
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

        final LogicalPosition caret_position = editor.getCaretModel()
                                                     .getLogicalPosition();

        boolean at_file_start = ( ( caret_position.line == 0 ) && ( caret_position.column == 0 ) );
        if( at_file_start ) { return; }

        Rectangle visible_area = get_editor_content_visible_area( editor );
        int visible_area_top_line_number = editor.yToVisualLine( visible_area.y );
        if( ( visible_area.y > editor.visualLineToY( visible_area_top_line_number ) ) &&
                ( ( visible_area.y + visible_area.height ) > editor.visualLineToY( visible_area_top_line_number + 1 ) ) )
        {
            visible_area_top_line_number++;
        }

        // Because of the way the editor scrolls, sometimes can't get to the top of the window.
        // So we need to consider the next line as the top of the window also.
        int visible_area_top_line_alternate_number = visible_area_top_line_number + 1;
        int last_line_number = editor.getDocument()
                                     .getLineNumber( editor.getDocument()
                                                           .getTextLength() );
        if( visible_area_top_line_alternate_number > last_line_number )
            visible_area_top_line_alternate_number = visible_area_top_line_number;

        LogicalPosition visible_area_top_beginning_of_line_position =
                editor.visualToLogicalPosition( new VisualPosition( visible_area_top_line_number,
                                                                    0 ) );
        LogicalPosition visible_area_top_alternate_beginning_of_line_position =
                editor.visualToLogicalPosition( new VisualPosition( visible_area_top_line_alternate_number,
                                                                    0 ) );
        boolean at_window_start = ( ( caret_position.line <= visible_area_top_beginning_of_line_position.line ) ||
                ( caret_position.line <= visible_area_top_alternate_beginning_of_line_position.line ) ) && ( caret_position.column == 0 );
        if( at_window_start )
        {
            if( Marking_component.is_marking_mode() )
            {
                do_action( "EditorTextStartWithSelection", e );
                return;
            }

            if( Line_marking_component.is_line_marking_mode() )
            {
                do_action( "EditorTextStartWithSelection", e );
                Line_marking_component.line_marking_post_handler( editor,
                                                                  KeyEvent.VK_HOME );
                return;
            }

            do_action( "EditorTextStart", e );
            return;
        }

        boolean at_line_start = ( caret_position.column == 0 ) || Line_marking_component.is_line_marking_mode();
        if( at_line_start )
        {
            if( Marking_component.is_marking_mode() )
            {
                do_action( "EditorMoveToPageTopWithSelection", e );
                return;
            }

            if( Line_marking_component.is_line_marking_mode() )
            {
                do_action( "EditorMoveToPageTopWithSelection", e );
                Line_marking_component.line_marking_post_handler( editor,
                                                                  KeyEvent.VK_HOME );

                return;
            }

            do_action( "EditorMoveToPageTop", e );
            return;
        }

        if( State_component.get_instance()
                           .get_use_brief_home() )
        {
            EditorActionUtil.moveCaretToLineStartIgnoringSoftWraps( editor );
            EditorModificationUtil.scrollToCaret( editor );
            if( Marking_component.is_marking_mode() )
            {
                Marking_component.marking_post_handler( editor,
                                                        KeyEvent.VK_HOME );
            }
        }
        else
        {
            if( Marking_component.is_marking_mode() )
            {
                do_action( "EditorLineStartWithSelection", e );
            }
            else
            {
                do_action( "EditorLineStart", e );
            }
        }

        return;
    }
}
