
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
import net.ddns.rkdawenterprises.brief4ijidea.Column_marking_component;
import org.jetbrains.annotations.NotNull;

import static net.ddns.rkdawenterprises.brief4ijidea.Miscellaneous.do_action;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.stop_all_marking_modes;

@SuppressWarnings({ "ComponentNotRegistered", "unused" })
public class Cut_to_scrap_action
        extends Plugin_action
{
    public Cut_to_scrap_action( String text,
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

        if( !Column_marking_component.is_column_marking_mode() )
        {
            do_action( "EditorCut", e );

            if( editor != null )
            {
                stop_all_marking_modes( editor );
            }
        }
        else
        {
            if( editor != null )
            {
                Column_marking_component.cut_to_scrap( editor );
                stop_all_marking_modes( editor );
            }
        }
    }
}
