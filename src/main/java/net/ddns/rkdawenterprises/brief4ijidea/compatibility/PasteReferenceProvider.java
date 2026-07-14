// intellij-community/platform/lang-impl/src/com/intellij/ide/actions/PasteReferenceProvider.java
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
//package com.intellij.ide.actions;
package net.ddns.rkdawenterprises.brief4ijidea.compatibility;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.ide.IdeBundle;
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
        return project != null && editor != null && getCopiedFqn( dataContext ) != null;
    }

    @Override
    public boolean isPasteEnabled( @NotNull DataContext dataContext )
    {
        final Project project = CommonDataKeys.PROJECT.getData( dataContext );
        String fqn = getCopiedFqn( dataContext );
        final @Nullable Editor editor = CommonDataKeys.EDITOR.getData( dataContext );
        final @Nullable PsiFile file = CommonDataKeys.PSI_FILE.getData( dataContext );
        if( ( project != null ) && ( editor != null ) && ( fqn != null ) && ( file != null ) && !file.getFileType().isBinary() )
        {
            boolean paste_lines_at_home = State_component.get_instance().get_paste_lines_at_home();
            boolean has_selection = editor.getSelectionModel().hasSelection();
            boolean ends_with_line_end = fqn.endsWith( "\n" ) || fqn.endsWith( "\r" ) || fqn.endsWith( "\r\n" ) || fqn.endsWith( "\n\r" );
            boolean is_qualified_line = paste_lines_at_home && !has_selection && ends_with_line_end;
            boolean is_column_mode_item = fqn.contains( Column_marking_component.Column_mode_block_transferable.get_mime_type() );
            return ( is_qualified_line || is_column_mode_item );
        }

        return false;
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
                                                       IdeBundle.message( "command.pasting.reference" ),
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
