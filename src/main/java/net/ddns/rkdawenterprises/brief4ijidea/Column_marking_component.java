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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInsight.highlighting.HighlightManagerImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.has_selection;

@SuppressWarnings({ "UnnecessaryLocalVariable", "UnnecessaryReturnStatement" })
public class Column_marking_component
{
    private static boolean s_is_column_marking_mode = false;
    private static LogicalPosition s_column_selection_origin = null;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean is_column_marking_mode() { return s_is_column_marking_mode; }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean toggle_column_marking_mode( @NotNull Editor editor )
    {
        s_is_column_marking_mode = !s_is_column_marking_mode;
        if( s_is_column_marking_mode )
        {
            enable_column_marking_mode( editor );
        }
        else
        {
            stop_column_marking_mode( editor,
                                      true );
        }

        return s_is_column_marking_mode;
    }

    private static Key_adapter s_key_adapter = null;

    private static Mouse_adapter s_mouse_adapter = null;

    private static ConcurrentHashMap<Integer, RangeHighlighter> s_range_highlighters;
    private static int s_block_start_line = -1;
    private static int s_block_end_line = -1;

    public static void enable_column_marking_mode( @NotNull Editor editor )
    {
        s_range_highlighters = new ConcurrentHashMap<>();
        s_block_start_line = -1;
        s_block_end_line = -1;

        add_key_handlers( editor );

        s_is_column_marking_mode = true;

        State_component.status_bar_message( Localized_messages.message( "column.marking.mode" ) );

        s_column_selection_origin = editor.getCaretModel()
                                          .getLogicalPosition();

        editor.getCaretModel()
              .moveCaretRelatively( 1,
                                    0,
                                    false,
                                    false,
                                    true );

        column_marking_post_handler( editor,
                                     KeyEvent.VK_LEFT );
    }

    public static void stop_column_marking_mode( @NotNull Editor editor,
                                                 boolean remove_selection )
    {
        s_is_column_marking_mode = false;
        s_column_selection_origin = null;

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

        remove_all_highlighters( editor );
    }

    private static void remove_all_highlighters( @NotNull Editor editor )
    {
        Project project = editor.getProject();
        if( project == null || project.isDisposed() || ( s_range_highlighters == null ) ) return;

        for( Map.Entry<Integer, RangeHighlighter> range_highlighter : s_range_highlighters.entrySet() )
        {
            HighlightManager.getInstance( project )
                            .removeSegmentHighlighter( editor,
                                                       range_highlighter.getValue() );
        }

        s_range_highlighters.clear();
        s_range_highlighters = null;
        s_block_start_line = -1;
        s_block_end_line = -1;

    }

    private static void add_key_handlers( @NotNull Editor editor )
    {
        EditorActionManager editor_action_manager = EditorActionManager.getInstance();

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP ),
                                                                           KeyEvent.VK_UP ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN ),
                                                                           KeyEvent.VK_DOWN ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT ),
                                                                           KeyEvent.VK_RIGHT ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT ),
                                                                           KeyEvent.VK_LEFT ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_UP ),
                                                                           KeyEvent.VK_PAGE_UP ) );

        editor_action_manager.setActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN,
                                                new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN ),
                                                                           editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_PAGE_DOWN ),
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
        s_key_adapter = new Column_marking_component.Key_adapter( editor );
        editor.getContentComponent()
              .addKeyListener( s_key_adapter );

        s_mouse_adapter = new Column_marking_component.Mouse_adapter( editor );
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

    public static void column_marking_post_handler( @NotNull Editor editor,
                                                    int key_code )
    {
        if( ( key_code == KeyEvent.VK_DELETE ) ||
                ( key_code == KeyEvent.VK_BACK_SPACE ) ||
                ( key_code == KeyEvent.VK_ENTER ) )
        {
            stop_column_marking_mode( editor,
                                      true );
            return;
        }

        LogicalPosition caret_logical_position = editor.getCaretModel()
                                                       .getCurrentCaret()
                                                       .getLogicalPosition();

        if( caret_logical_position.line > s_column_selection_origin.line )
        {
            if( caret_logical_position.column > s_column_selection_origin.column )
            {
                LogicalPosition start = s_column_selection_origin;
                LogicalPosition end = caret_logical_position;

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else if( caret_logical_position.column < s_column_selection_origin.column )
            {
                LogicalPosition start = new LogicalPosition( s_column_selection_origin.line,
                                                             caret_logical_position.column );
                LogicalPosition end = new LogicalPosition( caret_logical_position.line,
                                                           s_column_selection_origin.column );

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else // if( caret_logical_position.column == s_column_selection_origin.column )
            {
                remove_all_highlighters( editor );
                return;
            }
        }
        else if( caret_logical_position.line < s_column_selection_origin.line )
        {
            if( caret_logical_position.column > s_column_selection_origin.column )
            {
                LogicalPosition start = new LogicalPosition( caret_logical_position.line,
                                                             s_column_selection_origin.column );
                LogicalPosition end = new LogicalPosition( s_column_selection_origin.line,
                                                           caret_logical_position.column );

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else if( caret_logical_position.column < s_column_selection_origin.column )
            {
                LogicalPosition start = caret_logical_position;
                LogicalPosition end = s_column_selection_origin;

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else // if( caret_logical_position.column == s_column_selection_origin.column )
            {
                remove_all_highlighters( editor );
                return;
            }
        }
        else // if( caret_logical_position.line == s_column_selection_origin.line )
        {
            if( caret_logical_position.column > s_column_selection_origin.column )
            {
                LogicalPosition start = s_column_selection_origin;
                LogicalPosition end = caret_logical_position;

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else if( caret_logical_position.column < s_column_selection_origin.column )
            {
                LogicalPosition start = caret_logical_position;
                LogicalPosition end = s_column_selection_origin;

                set_column_selection( editor,
                                      start,
                                      end );
                return;
            }
            else // if( caret_logical_position.column == s_column_selection_origin.column )
            {
                remove_all_highlighters( editor );
                return;
            }
        }
    }

    private static int s_previous_start_column = -1;
    private static int s_previous_end_column = -1;

    private static void set_column_selection( Editor editor,
                                              LogicalPosition start,
                                              LogicalPosition end )
    {
        boolean try_to_reuse = ( s_previous_start_column == start.column ) &&
                ( s_previous_end_column == end.column );

        s_previous_start_column = start.column;
        s_previous_end_column = end.column;

        if( !try_to_reuse )
        {
            remove_all_highlighters( editor );
            s_range_highlighters = new ConcurrentHashMap<>();
        }
        else
        {
            for( Map.Entry<Integer, RangeHighlighter> range_highlighter : s_range_highlighters.entrySet() )
            {
                if( ( range_highlighter.getKey() < start.line ) ||
                        ( range_highlighter.getKey() > end.line ) )
                {
                    remove_highlighter( editor,
                                        range_highlighter.getValue(),
                                        range_highlighter.getKey() );
                }
            }
        }

        s_block_start_line = start.line;
        s_block_end_line = end.line;
        for( int line = start.line; line <= end.line; line++ )
        {
            if( try_to_reuse && s_range_highlighters.containsKey( line ) ) continue;

            int start_offset = editor.logicalPositionToOffset( new LogicalPosition( line,
                                                                                    start.column ) );
            int end_offset = editor.logicalPositionToOffset( new LogicalPosition( line,
                                                                                  end.column ) );
            add_highlighter( editor,
                             start_offset,
                             end_offset,
                             line );
        }

        // TODO: Remove...
        if( s_range_highlighters.size() != ( ( end.line - start.line ) + 1 ) )
            throw new RuntimeException( "Column mode selection error" );
    }

    private static void add_highlighter( Editor editor,
                                         int start_offset,
                                         int end_offset,
                                         int line )
    {
        Project project = editor.getProject();
        if( project == null || project.isDisposed() ) return;

        TextAttributes text_attributes = new TextAttributes( editor.getColorsScheme()
                                                                   .getColor( EditorColors.SELECTION_FOREGROUND_COLOR ),
                                                             editor.getColorsScheme()
                                                                   .getColor( EditorColors.SELECTION_BACKGROUND_COLOR ),
                                                             editor.getColorsScheme()
                                                                   .getColor( EditorColors.SELECTION_BACKGROUND_COLOR ),
                                                             EffectType.BOXED,
                                                             Font.PLAIN );
        ArrayList<RangeHighlighter> temporary = new ArrayList<>( 1 );
        ( (HighlightManagerImpl)HighlightManager.getInstance( project ) )
                .addRangeHighlight( editor,
                                    start_offset,
                                    end_offset,
                                    text_attributes,
                                    false,
                                    temporary );
        s_range_highlighters.put( line,
                                  temporary.get( 0 ) );
    }

    public static void remove_highlighter( Editor editor,
                                           RangeHighlighter highlighter,
                                           int line )
    {
        s_range_highlighters.remove( line );

        Project project = editor.getProject();
        if( project == null || project.isDisposed() ) return;

        HighlightManager.getInstance( project )
                        .removeSegmentHighlighter( editor,
                                                   highlighter );
    }

    private static void delete_selection( Editor editor )
    {
        @Nullable Project project = editor.getProject();
        WriteCommandAction.runWriteCommandAction( project,
                                                  Localized_messages.message( "command.name.delete.column.selection" ),
                                                  null,
                                                  () -> delete_selection_write_action( editor ),
                                                  project != null ? ( PsiDocumentManager.getInstance( project )
                                                                                        .getCachedPsiFile( editor.getDocument() ) ) : null );

        stop_column_marking_mode( editor,
                                  true );
    }

    private static void delete_selection_write_action( Editor editor )
    {
        for( Map.Entry<Integer, RangeHighlighter> range_highlighter : s_range_highlighters.entrySet() )
        {
            int start = range_highlighter.getValue()
                                         .getStartOffset();
            int end = range_highlighter.getValue()
                                       .getEndOffset();

            editor.getDocument()
                  .deleteString( start,
                                 end );
        }
    }

    public static void copy_to_scrap( Editor editor )
    {
        ApplicationManager.getApplication()
                          .runReadAction( () -> copy_to_scrap_read_action( editor ) );
    }

    public static void copy_to_scrap_read_action( Editor editor )
    {
        Transferable transferable = get_selection_transferable( editor );
        if( transferable == null ) return;

        CopyPasteManager.getInstance()
                        .setContents( transferable );
    }

    public static void cut_to_scrap( Editor editor )
    {
        @Nullable Project project = editor.getProject();
        WriteCommandAction.runWriteCommandAction( project,
                                                  Localized_messages.message( "command.name.cut.column.selection" ),
                                                  null,
                                                  () ->
                                                  {
                                                      Transferable transferable = get_selection_transferable( editor );
                                                      if( transferable == null ) return;

                                                      CopyPasteManager.getInstance()
                                                                      .setContents( transferable );

                                                      delete_selection_write_action( editor );
                                                  },
                                                  project != null ? ( PsiDocumentManager.getInstance( project )
                                                                                        .getCachedPsiFile( editor.getDocument() ) ) : null );
    }

    public static void paste( Project project,
                              Editor editor,
                              String block_JSON )
    {
        Column_mode_block_data block_data = Column_mode_block_data.deserialize_from_JSON( block_JSON );
        if( block_data == null ) return;
        
        ApplicationManager.getApplication()
                          .runWriteAction( () ->
                                           {
                                               Caret caret = editor.getCaretModel()
                                                                   .getPrimaryCaret();
                                               VisualPosition starting_visual_position = caret.getVisualPosition();
                                               int running_line_number = starting_visual_position.line;
                                               for( String string : block_data.rows )
                                               {
                                                   EditorModificationUtil.insertStringAtCaret( editor,
                                                                                               string,
                                                                                               false,
                                                                                               false );

                                                   int last_line = editor.getDocument()
                                                                         .getLineCount() - 1;
                                                   if( last_line < 0 ) last_line = 0;
                                                   if( running_line_number <
                                                           editor.logicalToVisualPosition( new LogicalPosition( last_line,
                                                                                                                0 ) ).line )
                                                   {
                                                       running_line_number++;
                                                   }
                                                   else
                                                   {
                                                       break;
                                                   }

                                                   caret.moveToVisualPosition( new VisualPosition( running_line_number,
                                                                                                   starting_visual_position.column,
                                                                                                   true ) );
                                               }

                                               if( !project.isDisposed() )
                                               {
                                                   ( (UndoManagerImpl)UndoManager.getInstance( project ) ).addDocumentAsAffected( editor.getDocument() );
                                               }
                                           } );

        editor.getScrollingModel()
              .scrollToCaret( ScrollType.RELATIVE );
    }

    private static @Nullable Transferable get_selection_transferable( Editor editor )
    {
        int size = s_range_highlighters.size();
        if( size == 0 ) return null;

        int[] start_offsets = new int[size];
        int[] end_offsets = new int[size];

        for( int i = s_block_start_line; i <= s_block_end_line; i++ )
        {
            int index = i - s_block_start_line;
            start_offsets[index] = s_range_highlighters.get( i )
                                                       .getStartOffset();
            end_offsets[index] = s_range_highlighters.get( i )
                                                     .getEndOffset();
        }

        ArrayList<String> list = new ArrayList<>();
        for( int i = 0; i < start_offsets.length; i++ )
        {
            String row_text = editor.getDocument()
                                    .getText( new TextRange( start_offsets[i],
                                                             end_offsets[i] ) );

            row_text = row_text.replace( System.getProperty( "line.separator" ),
                                         "" );

            list.add( row_text );
        }

        return new Column_mode_block_transferable( list.toArray( new String[0] ) );
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
            if( ( m_key_code == KeyEvent.VK_DELETE ) ||
                    ( m_key_code == KeyEvent.VK_BACK_SPACE ) ||
                    ( m_key_code == KeyEvent.VK_ENTER ) )
            {
                delete_selection( editor );
                return;
            }

            EditorActionHandler handler = Objects.requireNonNullElse( m_substitute_handler,
                                                                      m_original_handler );
            handler.execute( editor,
                             caret,
                             dataContext );

            column_marking_post_handler( editor,
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
            stop_column_marking_mode( m_editor,
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
            stop_column_marking_mode( m_editor,
                                      true );
        }
    }

    public static final class Column_mode_block_data
    {
        public final String mime;
        public final int width;
        public final String[] rows;

        public Column_mode_block_data( String mime,
                                       String[] rows,
                                       int width )
        {
            this.mime = mime;
            this.width = width;
            this.rows = rows;
        }

        public static String serialize_to_JSON( Column_mode_block_data block_data )
        {
            Gson gson = new GsonBuilder().disableHtmlEscaping()
                                         .setPrettyPrinting()
                                         .create();
            return gson.toJson( block_data );
        }

        public static Column_mode_block_data deserialize_from_JSON( String block_JSON )
        {
            Column_mode_block_data block_data = null;
            try
            {
                Gson gson = new GsonBuilder().disableHtmlEscaping()
                                             .setPrettyPrinting()
                                             .create();
                block_data = gson.fromJson( block_JSON,
                                            Column_mode_block_data.class );
            }
            catch( com.google.gson.JsonSyntaxException exception )
            {
                System.out.println( "Bad paste data format for " + Column_mode_block_transferable.get_mime_type() + ": " + exception );
                System.out.println( ">>>" + block_JSON + "<<<");
            }

            return block_data;
        }
    }

    public static final class Column_mode_block_transferable
            implements Transferable
    {
        private final String[] m_data;

        public Column_mode_block_transferable( String[] data ) { m_data = data; }

        @Override
        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.stringFlavor };
        }

        @Override
        public boolean isDataFlavorSupported( DataFlavor dataFlavor )
        {
            return ( dataFlavor.equals( DataFlavor.stringFlavor ) );
        }

        @NotNull
        @Override
        public Object getTransferData( DataFlavor dataFlavor )
                throws UnsupportedFlavorException
        {
            if( !isDataFlavorSupported( dataFlavor ) )
            {
                throw new UnsupportedFlavorException( dataFlavor );
            }

            // Determine maximum string width.
            int maximum_width = 0;
            for( String string : m_data )
            {
                if( string.length() > maximum_width ) maximum_width = string.length();
            }

            // Fill empty or smaller rows.
            for( int i = 0; i < m_data.length; i++ )
            {
                if( m_data[i].length() < maximum_width )
                {
                    m_data[i] = m_data[i] + StringUtils.repeat( " ",
                                                                maximum_width - m_data[i].length() );
                }
            }

            Column_mode_block_data block_data = new Column_mode_block_data( get_mime_type(),
                                                                            m_data,
                                                                            maximum_width );
            Gson gson = new GsonBuilder().disableHtmlEscaping()
                                         .setPrettyPrinting()
                                         .create();
            return gson.toJson( block_data );
        }

        public static @NonNls String get_mime_type()
        {
            return "text/brief-column-mode-block; class=" + Column_mode_block_transferable.class.getName();
        }
    }
}
