/*
 * Copyright (c) 2019-2024 RKDAW Enterprises and Ralph Williamson
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
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import com.intellij.psi.PsiFile;
import net.ddns.rkdawenterprises.brief4ijidea.State_component;
import org.jetbrains.annotations.NotNull;

import static net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action;

@SuppressWarnings({ "ComponentNotRegistered", "unused" })
public class Exit_action
        extends Plugin_action
{
    public Exit_action( String text,
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
        /// If unable to determine file modification status, ask to save anyway.
        boolean is_modified = true;
        String file_name = null;

        final Project project = e.getData( CommonDataKeys.PROJECT );
        final PsiFile a_PSI_file = e.getData( CommonDataKeys.PSI_FILE );
        if( ( project != null ) && ( a_PSI_file != null ) )
        {
            final VirtualFileImpl virtual_file = (VirtualFileImpl)a_PSI_file.getVirtualFile();
            if( virtual_file != null )
            {
                file_name = virtual_file.getName();
                if( !is_file_modified( project,
                                       virtual_file ) ) is_modified = false;
            }
        }

        if( is_modified )
        {
            String message_file_name = ( file_name != null ? file_name : net.ddns.rkdawenterprises.brief4ijidea.Localized_messages.message( "dialog.message.file" ) );
            if( Messages.showYesNoDialog( net.ddns.rkdawenterprises.brief4ijidea.Localized_messages.message( "dialog.message.write.changes.to.before.closing.if.you.want.them.externally.accessible", message_file_name ),
                                          net.ddns.rkdawenterprises.brief4ijidea.Localized_messages.message( "dialog.title.write.changes" ),
                                          Messages.getQuestionIcon() ) == Messages.YES )
            {
                do_action( "SaveDocument", e );
            }
        }

        if( State_component.get_instance()
                           .get_exit_only_closes_editor() )
        {
            do_action( "CloseEditor", e );
        }
        else
        {
            do_action( "Exit", e );
        }
    }

    public static boolean is_file_modified( Project project,
                                            VirtualFileImpl file )
    {
        FileEditorManager file_editor_manager = FileEditorManager.getInstance( project );
        FileEditor[] editors = file_editor_manager.getAllEditors( file );
        boolean modified = false;
        for(FileEditor editor: editors)
        {
            modified |= editor.isModified();
        }

        return modified;
    }
}
