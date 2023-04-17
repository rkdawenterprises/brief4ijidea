@file:Suppress("ClassName")

package net.ddns.rkdawenterprises.brief4ijidea

object Test_data
{
    const val java_example =
        """
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
            import org.apache.commons.lang.StringUtils;
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
            
                    State_component.status_bar_message( "<COLUMN-MARKING-MODE>" );
            
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
                        if( Actions_component.has_selection( editor ) )
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
                                                            new Editor_action_handler( editor_action_manager.getActionHandler( IdeActions.ACTION_EDITOR_MOVE_CARET_UP ), // This comment is just to make sure this line is much longer than the current editor window to be able to test the left side of window command.
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
            }
        """
}
