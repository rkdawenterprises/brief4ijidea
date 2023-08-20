/*
 * Copyright (c) 2019-2023 RKDAW Enterprises and Ralph Williamson
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
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.codeInsight.editorActions.TextBlockTransferable;
import com.intellij.codeInsight.editorActions.TextBlockTransferableData;
import com.intellij.ide.PasteProvider;
import com.intellij.lang.LanguageFormatting;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.impl.UndoManagerImpl;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actions.BasePasteHandler;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.actions.PasteAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.CopiedFromEmptySelectionPasteMode;
import com.intellij.openapi.editor.impl.EditorCopyPasteHelperImpl;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.util.text.CharArrayUtil;
import net.ddns.rkdawenterprises.brief4ijidea.compatibility.TypingActionsExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static com.intellij.openapi.editor.impl.CopiedFromEmptySelectionPasteMode.*;
import static com.intellij.openapi.editor.impl.EditorCopyPasteHelperImpl.getCopiedFromEmptySelectionPasteMode;

/**
 * This handler is used to move the cursor to the beginning of the current line when pasting clipboard content
 * that are full lines (ends in line termination), with no active selection.
 * <p>
 * <p>
 * It also handles inserting/pasting the "Brief Emulation Column Marking Mode" MIME type clipboard content.
 * <p>
 * <p>
 * This could be done in the paste from scrap action, but then the language formatters would not kick in,
 * nor would it work with the paste-multiple dialog. But this method seems to work for all that.
 * <p>
 * <p>
 * TODO: This class borrows heavily the concepts from the following two classes.
 * TODO: Need to periodically review them for updates.
 * TODO:     com.intellij.ide.actions.PasteReferenceProvider
 * TODO:     com.intellij.codeInsight.editorActions.PasteHandler
 * TODO: The following two classes are experimental (so not allowed in a plugin) but needed here,
 * TODO: so they are simply duplicated in this plugin.
 * TODO:     com.intellij.codeInsight.editorActions.TypingActionsExtension
 * TODO:     com.intellij.codeInsight.editorActions.DefaultTypingActionsExtension
 */
public class Paste_handler
    implements PasteProvider
{
    @Override
    public void performPaste(@NotNull DataContext dataContext)
    {
        final @Nullable Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final @Nullable Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if(project == null || editor == null)
        {
            return;
        }

        final @Nullable String fqn = getCopiedFqn(dataContext);
        if(fqn == null)
        {
            return;
        }

        execute(editor,
                dataContext,
                PasteAction.TRANSFERABLE_PROVIDER.getData(dataContext));
    }

    /**
     * Should perform fast and memory cheap negation. May return incorrect true.
     * See #12326
     *
     * @param dataContext Paste action context.
     */
    @Override
    public boolean isPastePossible(@NotNull DataContext dataContext)
    {
        final @Nullable Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final @Nullable Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        return ((project != null) && (editor != null) && (getCopiedFqn(dataContext) != null));
    }

    @Override
    public boolean isPasteEnabled(@NotNull DataContext dataContext)
    {
        final @Nullable Project project = CommonDataKeys.PROJECT.getData(dataContext);
        final @Nullable Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        final @Nullable PsiFile file = CommonDataKeys.PSI_FILE.getData(dataContext);
        final @Nullable String fqn = getCopiedFqn(dataContext);
        if((project != null) && (editor != null) && (fqn != null) && (file != null) && !file.getFileType().isBinary())
        {
            boolean paste_lines_at_home = State_component.get_instance().get_paste_lines_at_home();
            boolean has_selection = editor.getSelectionModel().hasSelection();
            boolean ends_with_line_end = fqn.endsWith("\n") || fqn.endsWith("\r") || fqn.endsWith("\r\n") || fqn.endsWith("\n\r");
            boolean is_qualified_line = paste_lines_at_home && !has_selection && ends_with_line_end;
            boolean is_column_mode_item = fqn.contains(Column_marking_component.Column_mode_block_transferable.get_mime_type());
            return (is_qualified_line || is_column_mode_item);
        }

        return false;
    }

    /**
     * Specifies the thread and the way {@link AnAction#update(AnActionEvent)},
     * {@link ActionGroup#getChildren(AnActionEvent)} or other update-like method shall be called.
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread()
    {
        return ActionUpdateThread.BGT;
    }

    @Nullable
    private String getCopiedFqn(final DataContext context)
    {
        @Nullable Supplier<Transferable> supplier = PasteAction.TRANSFERABLE_PROVIDER.getData(context);

        if(supplier != null)
        {
            Transferable transferable = supplier.get();
            if(transferable != null)
            {
                try
                {
                    return (String) transferable.getTransferData(DataFlavor.stringFlavor);
                }
                catch(Exception ignored)
                {
                }
            }

            return null;
        }

        return CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
    }

    private Transferable getContentsToPasteToEditor(@Nullable Supplier<? extends Transferable> supplier)
    {
        if(supplier == null)
        {
            return CopyPasteManager.getInstance().getContents();
        }
        else
        {
            return supplier.get();
        }
    }

    private void execute(@NotNull Editor editor,
                         DataContext dataContext,
                         @Nullable Supplier<? extends Transferable> supplier)
    {
        final Transferable transferable = getContentsToPasteToEditor(supplier);
        if(transferable == null)
        {
            return;
        }

        if(!EditorModificationUtil.checkModificationAllowed(editor))
        {
            return;
        }
        if(!EditorModificationUtil.requestWriting(editor))
        {
            return;
        }

        final Project project = editor.getProject();
        final Document document = editor.getDocument();
        final PsiFile file = (project == null) ? null : PsiDocumentManager.getInstance(project).getPsiFile(document);

        DumbService.getInstance(project).runWithAlternativeResolveEnabled(() ->
                                                                          {
                                                                              document.startGuardedBlockChecking();
                                                                              try
                                                                              {
                                                                                  doPaste(editor,
                                                                                          project,
                                                                                          file,
                                                                                          document,
                                                                                          transferable);
                                                                              }
                                                                              catch(ReadOnlyFragmentModificationException e)
                                                                              {
                                                                                  EditorActionManager.getInstance().getReadonlyFragmentModificationHandler(document).handle(e);
                                                                              }
                                                                              finally
                                                                              {
                                                                                  document.stopGuardedBlockChecking();
                                                                              }
                                                                          });
    }

    private static void doPaste(@NotNull final Editor editor,
                                @NotNull final Project project,
                                final PsiFile file,
                                final Document document,
                                @NotNull final Transferable content)
    {
        final TypingActionsExtension typingActionsExtension =
            TypingActionsExtension.findForContext(project,
                                                  editor);
        try
        {
            typingActionsExtension.startPaste(project,
                                              editor);
            doPasteAction(editor,
                          project,
                          file,
                          document,
                          content,
                          typingActionsExtension);
        }
        finally
        {
            typingActionsExtension.endPaste(project,
                                            editor);
        }
    }

    private static void doPasteAction(@NotNull final Editor editor,
                                      @NotNull final Project project,
                                      final PsiFile file,
                                      final Document document,
                                      @NotNull final Transferable content,
                                      @NotNull final TypingActionsExtension typingActionsExtension)
    {
        CopyPasteManager.getInstance().stopKillRings();

        String text = null;
        try
        {
            text = (String) content.getTransferData(DataFlavor.stringFlavor);
        }
        catch(Exception e)
        {
            editor.getComponent().getToolkit().beep();
        }

        if(text == null)
        {
            return;
        }

        int textLength = text.length();
        if(BasePasteHandler.isContentTooLarge(textLength))
        {
            BasePasteHandler.contentLengthLimitExceededMessage(textLength);
            return;
        }

        final CodeInsightSettings settings = CodeInsightSettings.getInstance();

        final List<ProcessorAndData<?>> extraData = new ArrayList<>();
        final Collection<TextBlockTransferableData> allValues = new ArrayList<>();

        for(CopyPastePostProcessor<? extends TextBlockTransferableData> processor : CopyPastePostProcessor.EP_NAME.getExtensionList())
        {
            ProcessorAndData<? extends TextBlockTransferableData> data = ProcessorAndData.create(processor,
                                                                                                 content);
            if(data != null)
            {
                extraData.add(data);
                allValues.addAll(data.data);
            }
        }

        text = TextBlockTransferable.convertLineSeparators(editor,
                                                           text,
                                                           allValues);

        final CaretModel caretModel = editor.getCaretModel();
        final SelectionModel selectionModel = editor.getSelectionModel();
        int col = caretModel.getLogicalPosition().column;
        final int blockIndentAnchorColumn;
        final int caretOffset = caretModel.getOffset();
        boolean is_column_mode_item = text.contains(Column_marking_component.Column_mode_block_transferable.get_mime_type());
        if(selectionModel.hasSelection() && caretOffset >= selectionModel.getSelectionStart())
        {
            blockIndentAnchorColumn = editor.offsetToLogicalPosition(selectionModel.getSelectionStart()).column;
        }
        else
        {
            if( !is_column_mode_item )
            {
                // The pasted content are full lines and not column mode content,
                // so paste at the beginning of the line, moving the cursor also.
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

        if( is_column_mode_item )
        {
            // Pasting column marking mode content. Pasting "as-is", no further processing.
            Column_marking_component.paste( project,
                                            editor,
                                            text );
            return;
        }

        EditorCopyPasteHelper.CopyPasteOptions copyPasteOptions =
            EditorCopyPasteHelperImpl.CopyPasteOptionsTransferableData.valueFromTransferable(content);
        CopiedFromEmptySelectionPasteMode pasteMode = copyPasteOptions.isCopiedFromEmptySelection()
            ? getCopiedFromEmptySelectionPasteMode() : AT_CARET;
        boolean isInsertingEntireLineAboveCaret = pasteMode == ENTIRE_LINE_ABOVE_CARET &&
            !selectionModel.hasSelection();
        List<CaretState> caretStateToRestore = null;
        if(isInsertingEntireLineAboveCaret)
        {
            caretStateToRestore = caretModel.getCaretsAndSelections();
            int lineStartOffset = EditorUtil.getNotFoldedLineStartOffset(editor,
                                                                         caretOffset);
            caretModel.moveToOffset(lineStartOffset);
        }

        RawText rawText = RawText.fromTransferable(content);
        String newText = text;
        for(CopyPastePreProcessor preProcessor : CopyPastePreProcessor.EP_NAME.getExtensionList())
        {
            newText = preProcessor.preprocessOnPaste(project,
                                                     file,
                                                     editor,
                                                     newText,
                                                     rawText);
        }

        if(caretStateToRestore != null)
        {
            caretModel.setCaretsAndSelections(caretStateToRestore);
        }

        final boolean pastedTextWasChanged = !text.equals(newText);
        int indentOptions = pastedTextWasChanged ? CodeInsightSettings.REFORMAT_BLOCK : settings.REFORMAT_ON_PASTE;
        text = newText;

        if(LanguageFormatting.INSTANCE.forContext(file) == null && indentOptions != CodeInsightSettings.NO_REFORMAT)
        {
            indentOptions = CodeInsightSettings.INDENT_BLOCK;
        }

        final String _text = text;
        final TextRange pastedRange = ApplicationManager.getApplication().runWriteAction((Computable<TextRange>) () ->
        {
            if(!project.isDisposed())
            {
                ((UndoManagerImpl) UndoManager.getInstance(project)).addDocumentAsAffected(editor.getDocument());
            }

            return isInsertingEntireLineAboveCaret ? EditorCopyPasteHelperImpl.insertEntireLineAboveCaret(editor,
                                                                                                          _text)
                : EditorCopyPasteHelperImpl.insertStringAtCaret(editor,
                                                                _text,
                                                                pasteMode == TRIM_IF_MIDDLE_LINE);
        });

        final RangeMarker bounds = document.createRangeMarker(pastedRange);
        final Ref<Boolean> skipIndentation = new Ref<>(pastedTextWasChanged ? Boolean.FALSE : null);
        for(ProcessorAndData<?> data : extraData)
        {
            data.process(project,
                         editor,
                         bounds,
                         caretOffset,
                         skipIndentation);
        }

        boolean pastedTextContainsWhiteSpacesOnly =
            CharArrayUtil.shiftForward(document.getCharsSequence(),
                                       bounds.getStartOffset(),
                                       " \n\t") >= bounds.getEndOffset();

        VirtualFile virtualFile = file.getVirtualFile();
        if(!pastedTextContainsWhiteSpacesOnly &&
            (virtualFile == null || !SingleRootFileViewProvider.isTooLargeForIntelligence(virtualFile)))
        {
            final int howtoReformat =
                skipIndentation.get() == Boolean.TRUE
                    && (indentOptions == CodeInsightSettings.INDENT_BLOCK || indentOptions == CodeInsightSettings.INDENT_EACH_LINE)
                    ? CodeInsightSettings.NO_REFORMAT
                    : indentOptions;
            ApplicationManager.getApplication().runWriteAction(
                () -> typingActionsExtension
                    .format(project,
                            editor,
                            howtoReformat,
                            bounds.getStartOffset(),
                            bounds.getEndOffset(),
                            blockIndentAnchorColumn,
                            true)
            );
        }

        if(bounds.isValid())
        {
            if(!isInsertingEntireLineAboveCaret)
            {
                caretModel.moveToOffset(bounds.getEndOffset());
                editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
            }
            
            editor.putUserData(EditorEx.LAST_PASTED_REGION,
                               bounds.getTextRange());
        }
    }

    private static class ProcessorAndData<Data extends TextBlockTransferableData>
    {
        final CopyPastePostProcessor<Data> processor;
        final @NotNull List<? extends Data> data;

        private ProcessorAndData(@NotNull CopyPastePostProcessor<Data> processor,
                                 @NotNull List<? extends Data> data)
        {
            this.processor = processor;
            this.data = data;
        }

        void process(@NotNull Project project,
                     @NotNull Editor editor,
                     @NotNull RangeMarker bounds,
                     int caretOffset,
                     @NotNull Ref<? super Boolean> skipIndentation)
        {
            processor.processTransferableData(project,
                                              editor,
                                              bounds,
                                              caretOffset,
                                              skipIndentation,
                                              data);
        }

        static <T extends TextBlockTransferableData> @Nullable ProcessorAndData<T> create(@NotNull CopyPastePostProcessor<T> processor,
                                                                                          @NotNull Transferable content)
        {
            List<T> data = processor.extractTransferableData(content);
            if(data.isEmpty())
            {
                return null;
            }
            return new ProcessorAndData<>(processor,
                                          data);
        }
    }
}
