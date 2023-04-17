
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
import com.intellij.openapi.project.Project;
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages;
import org.jetbrains.annotations.NotNull;

import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.*;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.virtual_space_setting_warning;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.warning_message;

@SuppressWarnings({ "ComponentNotRegistered", "unused" })
public class Column_mark_action
        extends Plugin_action
{
    public Column_mark_action( String text,
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
        final Editor editor = e.getData( CommonDataKeys.EDITOR );
        if( editor == null ) return;

        final Project project = e.getData( CommonDataKeys.PROJECT );
        if( ( project == null ) || ( editor.getProject() == null ) )
        {
            warning_message( null,
                             Localized_messages.message( "column.marking.mode.currently.uses.highlightmanager.which.requires.a.project.to.obtain.an.instance" ),
                             null );
            return;
        }

        virtual_space_setting_warning( editor );

        toggle_column_marking_mode( editor );
    }
}
