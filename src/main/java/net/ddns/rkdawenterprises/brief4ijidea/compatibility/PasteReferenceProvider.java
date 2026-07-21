// intellij-community/platform/lang-impl/src/com/intellij/ide/actions/PasteReferenceProvider.java
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
//package com.intellij.ide.actions;
package net.ddns.rkdawenterprises.brief4ijidea.compatibility;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actions.PasteAction;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import net.ddns.rkdawenterprises.brief4ijidea.Column_marking_component;
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages;
import net.ddns.rkdawenterprises.brief4ijidea.State_component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.function.Supplier;

//@ApiStatus.Internal
public final class PasteReferenceProvider implements PasteProvider
{
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread()
    {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void performPaste( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        final Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        if( project == null || editor == null )
        {
            return;
        }

        final String fqn = getCopiedFqn( dataContext );
        if( fqn == null )
        {
            return;
        }

//    QualifiedNameProvider theProvider = null;
//    PsiElement element = null;
//    for(QualifiedNameProvider provider: QualifiedNameProvider.EP_NAME.getExtensionList()) {
//      element = provider.qualifiedNameToElement(fqn, project);
//      if (element != null) {
//        theProvider = provider;
//        break;
//      }
//    }
//
//    if (theProvider != null) {
//      insert(fqn, element, editor, theProvider);
//    }

        insert( fqn,
                editor,
                new PasteHandler( null ),
                dataContext );
    }

    @Override
    public boolean isPastePossible( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        final Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        return ( ( project != null ) && ( editor != null ) && ( getCopiedFqn( dataContext ) != null ) );
    }

    /**
     * Only use our paste provider for specific cases as follows.
     * 1) The clipboard contains full line(s) (content ends in a line termination),
     * there's no current selection, and the settings set to "paste lines at home".
     * or
     * 2) The clipboard contains column mode content.
     * and
     * 3) The paste was initiated using our Brief Emulator key-mapping actions. This is done
     * so there is an easy way to use the built-in past handler since this one doesn't do J2K.
     */
    @Override
    public boolean isPasteEnabled( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        String fqn = getCopiedFqn( dataContext );
        final @Nullable Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        final @Nullable PsiFile file = CommonDataKeys.PSI_FILE.getData( dataContext );
        boolean is_paste_enabled = false;
        if( ( project != null ) && ( editor != null ) && ( fqn != null ) && ( file != null ) && !file.getFileType().isBinary() )
        {
            boolean paste_lines_at_home = State_component.get_instance().get_paste_lines_at_home();
            boolean has_selection = editor.getSelectionModel().hasSelection();
            boolean ends_with_line_end = fqn.endsWith( "\n" ) || fqn.endsWith( "\r" ) || fqn.endsWith( "\r\n" ) || fqn.endsWith( "\n\r" );
            boolean is_qualified_line = paste_lines_at_home && !has_selection && ends_with_line_end;
            boolean is_column_mode_item = fqn.contains( Column_marking_component.Column_mode_block_transferable.get_mime_type() );
            is_paste_enabled = ( is_qualified_line || is_column_mode_item )
                && !State_component.get_instance().get_paste_using_system();
        }

        System.out.println(">>> is_paste_enabled = " + is_paste_enabled);

        return is_paste_enabled;
//    return project != null && fqn != null && QualifiedNameProviderUtil.qualifiedNameToElement( fqn, project) != null;
    }

    private static void insert(
            @NotNull String fqn,
//    @NotNull PsiElement element,
            @NotNull Editor editor,
            @NotNull PasteHandler provider,
            @NotNull DataContext dataContext
//    @NotNull QualifiedNameProvider provider
    )
    {
        final Project project = editor.getProject();
        if( project == null )
        {
            return;
        }

        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance( project );
        documentManager.commitDocument( editor.getDocument() );

        final PsiFile file = documentManager.getPsiFile( editor.getDocument() );
        if( !FileModificationService.getInstance().prepareFileForWrite( file ) )
        {
            return;
        }

        CommandProcessor.getInstance().executeCommand( project,
                                                       () -> ApplicationManager.getApplication().runWriteAction( () ->
                                                                                                                 {
                                                                                                                     Document document = editor.getDocument();
                                                                                                                     documentManager.doPostponedOperationsAndUnblockDocument( document );
                                                                                                                     documentManager.commitDocument( document );
                                                                                                                     EditorModificationUtil.deleteSelectedText( editor );
                                                                                                                     provider.doExecute( editor,
                                                                                                                                         null,
                                                                                                                                         dataContext );
//      provider.insertQualifiedName(fqn, element, editor, project);
                                                                                                                 } ),
                                                       Localized_messages.message( "command.pasting.reference" ),
                                                       null );
    }

    private static @Nullable String getCopiedFqn( final DataContext context )
    {
        @Nullable Supplier<Transferable> producer = PasteAction.TRANSFERABLE_PROVIDER.getData( context );

        if( producer != null )
        {
            Transferable transferable = producer.get();
            if( transferable != null )
            {
                try
                {
                    return (String) transferable.getTransferData( DataFlavor.stringFlavor );
//          return (String)transferable.getTransferData( CopyReferenceAction.ourFlavor);
                }
                catch( Exception ignored )
                {
                }
            }
            return null;
        }

        return CopyPasteManager.getInstance().getContents( DataFlavor.stringFlavor );
//    return CopyPasteManager.getInstance().getContents(CopyReferenceAction.ourFlavor);
    }
}
