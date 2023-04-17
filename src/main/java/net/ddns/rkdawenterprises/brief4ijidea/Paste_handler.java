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

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.editorActions.*;
import com.intellij.ide.PasteProvider;
import com.intellij.lang.LanguageFormatting;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actions.BasePasteHandler;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.actions.PasteAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.Producer;
import com.intellij.util.text.CharArrayUtil;
import net.ddns.rkdawenterprises.brief4ijidea.compatibility.TypingActionsExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.*;

/**
 * This handler is used to move the cursor to the beginning of the current line when pasting clipboard content that are
 * lines (ends in line termination), with no active selection. This could be done in the paste action, but then the
 * language formatters would not kick in, nor would it work with the paste-multiple dialog. But this method seems to
 * work for all that.
 * <p>
 * It also handles inserting/pasting the Brief emulation column marking mode MIME type clipboard content.
 * <p>
 * TODO: com.intellij.ide.actions.PasteReferenceProvider
 * TODO: com.intellij.codeInsight.editorActions.PasteHandler
 * TODO: com.intellij.codeInsight.editorActions.DefaultTypingActionsExtension
 */
public class Paste_handler
        implements PasteProvider
{
    @Override
    public void performPaste( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        final Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        final PsiFile file = CommonDataKeys.PSI_FILE.getData( dataContext );
        final Transferable transferable = CopyPasteManager.getInstance()
                                                          .getContents();
        if( ( project != null ) && ( editor != null ) && ( file != null ) && ( transferable != null ) )
        {
            final TypingActionsExtension typing_actions_extension = TypingActionsExtension.findForContext( project,
                                                                                                           editor );
            try
            {
                typing_actions_extension.startPaste( project,
                                                     editor );
                do_paste( editor,
                          project,
                          file,
                          editor.getDocument(),
                          transferable,
                          typing_actions_extension );
            }
            finally
            {
                typing_actions_extension.endPaste( project,
                                                   editor );
            }
        }
    }

    /**
     * Should perform fast and memory cheap negation. May return incorrect true. See #12326
     *
     * @param dataContext The data context
     */
    @Override
    public boolean isPastePossible( @NotNull DataContext dataContext )
    {
        return is_good_to_go( dataContext );
    }

    @Override
    public boolean isPasteEnabled( @NotNull DataContext dataContext )
    {
        return is_good_to_go( dataContext );
    }

    private boolean is_good_to_go( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        final Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        final PsiFile file = CommonDataKeys.PSI_FILE.getData( dataContext );
        String paste_data = get_paste_data( dataContext );

        if( ( project != null ) && ( editor != null ) && ( file != null ) && !file.getFileType()
                                                                                  .isBinary() && ( paste_data != null ) && ( paste_data.length() > 0 ) )
        {
            if( paste_data.contains( Column_marking_component.Column_mode_block_transferable.get_mime_type() ) )
            {
                return true;
            }
            else
            {
                return State_component.get_instance()
                                      .get_paste_lines_at_home() && !editor.getSelectionModel()
                                                                           .hasSelection() &&
                        ( paste_data.endsWith( "\n" ) || paste_data.endsWith( "\r" ) || paste_data.endsWith( "\r\n" ) || paste_data.endsWith( "\n\r" ) );
            }
        }

        return false;
    }

    @Nullable
    private static String get_paste_data( final DataContext context )
    {
        Producer<Transferable> producer = PasteAction.TRANSFERABLE_PROVIDER.getData( context );

        if( producer != null )
        {
            Transferable transferable = producer.produce();
            if( transferable != null )
            {
                try
                {
                    return (String)transferable.getTransferData( DataFlavor.stringFlavor );
                }
                catch( Exception ignored ) { }
            }
            return null;
        }

        return CopyPasteManager.getInstance()
                               .getContents( DataFlavor.stringFlavor );
    }

    private static class ProcessorAndData<Data extends TextBlockTransferableData>
    {
        final CopyPastePostProcessor<Data> processor;
        final @NotNull List<Data> data;

        private ProcessorAndData( @NotNull CopyPastePostProcessor<Data> processor,
                                  @NotNull List<Data> data )
        {
            this.processor = processor;
            this.data = data;
        }

        void process( @NotNull Project project,
                      @NotNull Editor editor,
                      @NotNull RangeMarker bounds,
                      int caretOffset,
                      @NotNull Ref<Boolean> skipIndentation )
        {
            processor.processTransferableData( project,
                                               editor,
                                               bounds,
                                               caretOffset,
                                               skipIndentation,
                                               data );
        }

        static <T extends TextBlockTransferableData> @Nullable ProcessorAndData<T> create(
                @NotNull CopyPastePostProcessor<T> processor,
                @NotNull Transferable content
                                                                                         )
        {
            List<T> data = processor.extractTransferableData( content );
            if( data.isEmpty() ) return null;
            return new ProcessorAndData<>( processor,
                                           data );
        }
    }

    private static void do_paste( @NotNull final Editor editor,
                                  @NotNull final Project project,
                                  final PsiFile file,
                                  final Document document,
                                  @NotNull final Transferable content,
                                  @NotNull final TypingActionsExtension typingActionsExtension )
    {
        CopyPasteManager.getInstance()
                        .stopKillRings();

        String text = null;
        try
        {
            text = (String)content.getTransferData( DataFlavor.stringFlavor );
        }
        catch( Exception e )
        {
            editor.getComponent()
                  .getToolkit()
                  .beep();
        }
        if( text == null ) return;
        int textLength = text.length();
        if( BasePasteHandler.isContentTooLarge( textLength ) )
        {
            BasePasteHandler.contentLengthLimitExceededMessage( textLength );
            return;
        }

        final CodeInsightSettings settings = CodeInsightSettings.getInstance();

        final List<ProcessorAndData<?>> extraData = new ArrayList<>();
        final Collection<TextBlockTransferableData> allValues = new ArrayList<>();

        for( CopyPastePostProcessor<? extends TextBlockTransferableData> processor : CopyPastePostProcessor.EP_NAME.getExtensionList() )
        {
            ProcessorAndData<? extends TextBlockTransferableData> data = ProcessorAndData.create( processor,
                                                                                                  content );
            if( data != null )
            {
                extraData.add( data );
                allValues.addAll( data.data );
            }
        }

        text = TextBlockTransferable.convertLineSeparators( editor,
                                                            text,
                                                            allValues );

        final CaretModel caretModel = editor.getCaretModel();
        final SelectionModel selectionModel = editor.getSelectionModel();
        int col = caretModel.getLogicalPosition().column;

        // There is a possible case that we want to perform paste while there is an active selection at the editor and caret is located
        // inside it (e.g. Ctrl+A is pressed while caret is not at the zero column). We want to insert the text at selection start column
        // then, hence, inserted block of text should be indented according to the selection start as well.
        final int blockIndentAnchorColumn;
        final int caretOffset = caretModel.getOffset();
        if( selectionModel.hasSelection() && caretOffset >= selectionModel.getSelectionStart() )
        {
            blockIndentAnchorColumn = editor.offsetToLogicalPosition( selectionModel.getSelectionStart() ).column;
        }
        else
        {
            if( !text.contains( Column_marking_component.Column_mode_block_transferable.get_mime_type() ) )
            {
                /*
                 * The pasted content are full lines, based on "isPasteEnabled()/isPastePossible() above,
                 * and it is not column mode content, so paste at the beginning of the line.
                 */
                if( col != 0 )
                {
                    EditorActionUtil.moveCaretToLineStart( editor,
                                                           false );
                    col = editor.getCaretModel()
                                .getLogicalPosition().column;
                }
            }

            blockIndentAnchorColumn = col;
        }

        if( text.contains( Column_marking_component.Column_mode_block_transferable.get_mime_type() ) )
        {
            // Pasting column marking mode content. Pasting "as-is", no further processing.
            Column_marking_component.paste( project,
                                            editor,
                                            text );
            return;
        }

        // We assume that EditorModificationUtil.insertStringAtCaret() is smart enough to remove currently selected text (if any).

        RawText rawText = RawText.fromTransferable( content );
        String newText = text;
        for( CopyPastePreProcessor preProcessor : CopyPastePreProcessor.EP_NAME.getExtensionList() )
        {
            newText = preProcessor.preprocessOnPaste( project,
                                                      file,
                                                      editor,
                                                      newText,
                                                      rawText );
        }

        final boolean pastedTextWasChanged = !text.equals( newText );
        int indentOptions = pastedTextWasChanged ? CodeInsightSettings.REFORMAT_BLOCK : settings.REFORMAT_ON_PASTE;
        text = newText;

        if( LanguageFormatting.INSTANCE.forContext( file ) == null && indentOptions != CodeInsightSettings.NO_REFORMAT )
        {
            indentOptions = CodeInsightSettings.INDENT_BLOCK;
        }

        final String _text = text;
        ApplicationManager.getApplication()
                          .runWriteAction( () ->
                                           {
                                               EditorModificationUtil.insertStringAtCaret( editor,
                                                                                           _text,
                                                                                           false,
                                                                                           true );
                                               if( !project.isDisposed() )
                                               {
                                                   ( (UndoManagerImpl)UndoManager.getInstance( project ) ).addDocumentAsAffected( editor.getDocument() );
                                               }
                                           } );

        int length = text.length();
        int offset = caretModel.getOffset() - length;
        if( offset < 0 )
        {
            length += offset;
            offset = 0;
        }

        final RangeMarker bounds = document.createRangeMarker( offset,
                                                               offset + length );

        caretModel.moveToOffset( bounds.getEndOffset() );
        editor.getScrollingModel()
              .scrollToCaret( ScrollType.RELATIVE );
        selectionModel.removeSelection();

        // `skipIndentation` is additionally used as marker for changed pasted test
        // Any value, except `null` is a signal that the text was transformed.
        // For the `CopyPasteFoldingProcessor` it means that folding data is not valid and cannot be applied.
        final Ref<Boolean> skipIndentation = new Ref<>( pastedTextWasChanged ? Boolean.FALSE : null );
        for( ProcessorAndData<?> data : extraData )
        {
//            SlowOperations.allowSlowOperations( () ->
//                                                {
            data.process( project,
                          editor,
                          bounds,
                          caretOffset,
                          skipIndentation );
//                                                } );
        }

        boolean pastedTextContainsWhiteSpacesOnly =
                CharArrayUtil.shiftForward( document.getCharsSequence(),
                                            bounds.getStartOffset(),
                                            " \n\t" ) >= bounds.getEndOffset();

        VirtualFile virtualFile = file.getVirtualFile();
        if( !pastedTextContainsWhiteSpacesOnly &&
                ( virtualFile == null || !SingleRootFileViewProvider.isTooLargeForIntelligence( virtualFile ) ) )
        {
            final int howtoReformat =
                    skipIndentation.get() == Boolean.TRUE
                            && ( indentOptions == CodeInsightSettings.INDENT_BLOCK || indentOptions == CodeInsightSettings.INDENT_EACH_LINE )
                            ? CodeInsightSettings.NO_REFORMAT
                            : indentOptions;
            ApplicationManager.getApplication()
                              .runWriteAction(
                                      () -> typingActionsExtension
                                              .format( project,
                                                       editor,
                                                       howtoReformat,
                                                       bounds.getStartOffset(),
                                                       bounds.getEndOffset(),
                                                       blockIndentAnchorColumn,
                                                       true )
                                             );
        }

        if( bounds.isValid() )
        {
            caretModel.moveToOffset( bounds.getEndOffset() );
            editor.getScrollingModel()
                  .scrollToCaret( ScrollType.RELATIVE );
            selectionModel.removeSelection();
            editor.putUserData( EditorEx.LAST_PASTED_REGION,
                                TextRange.create( bounds ) );
        }
    }
}
