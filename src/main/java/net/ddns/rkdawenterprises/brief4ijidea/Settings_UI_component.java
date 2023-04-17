
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

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class Settings_UI_component
        implements Disposable
{
    private final JPanel myMainPanel;

    private final JBCheckBox m_enabled = new JBCheckBox( Localized_messages.message( "checkbox.enable.or.disable.plugin" ) );
    private final JBCheckBox m_show_icon_in_status_bar = new JBCheckBox( Localized_messages.message( "checkbox.show.or.hide.status.bar.widget" ) );
    private final JBCheckBox m_paste_lines_at_home = new JBCheckBox( Localized_messages.message( "checkbox.paste.whole.lines.at.home.position" ) );
    private final JBCheckBox m_use_brief_home = new JBCheckBox( Localized_messages.message( "checkbox.use.brief.home.key.functionality" ) );
    private final JBCheckBox m_check_active_keymap_is_brief = new JBCheckBox( Localized_messages.message( "checkbox.startup.check.active.keymap.brief" ) );
    private final JBCheckBox m_exit_only_closes_editor = new JBCheckBox( Localized_messages.message( "checkbox.exit.command.only.closes.active.editor" ) );
    private final JBCheckBox m_write_all_and_exit_closes_IDEA = new JBCheckBox( Localized_messages.message( "checkbox.write.all.exit.command.closes.idea" ) );
    private final JBCheckBox m_do_not_show_virtual_space_setting_dialog = new JBCheckBox( Localized_messages.message( "checkbox.do.not.show.virtual.space.setting.dialog.again" ) );
    private final JBCheckBox m_show_document_information = new JBCheckBox( Localized_messages.message( "checkbox.show.document.information.in.status.bar" ) );

    public Settings_UI_component()
    {
        m_enabled.setToolTipText( Localized_messages.message( "enable.the.brief.editor.emulator.functionality.uncheck.to.disable.the.plugin" ) );
        m_show_icon_in_status_bar.setToolTipText( Localized_messages.message( "shows.or.hides.the.plugin.widget.icon.in.the.status.bar.uncheck.to.hide.the.status.bar.icon" ) );
        m_paste_lines_at_home.setToolTipText( Localized_messages.message( "if.the.item.in.the.scrap.history.buffer.being.pasted.is.a.full.line.ends.with.a.line.termination.then.paste.it.at.the.beginning.of.the.current.line.uncheck.to.paste.at.the.current.cursor.location" ) );
        m_use_brief_home.setToolTipText( Localized_messages.message( "use.the.home.key.functionality.as.documented.in.brief.this.disables.the.normal.smart.home.functionality.uncheck.to.restore.smart.home.key.functionality.but.still.maintain.brief.home.home.home.key.functionality" ) );
        m_check_active_keymap_is_brief.setToolTipText( Localized_messages.message( "initial.check.for.active.keymap.is.set.to.use.the.included.brief.keymap.at.startup.uncheck.this.if.you.modify.the.keymap.which.makes.a.copy.of.the.default.brief.keymap.because.startup.will.configure.default.brief.keymap.as.active.keymap.if.this.is.checked" ) );
        m_exit_only_closes_editor.setToolTipText( Localized_messages.message( "exit.command.will.close.the.currently.active.editor.not.the.idea.original.brief.functionality.would.close.the.application.uncheck.this.if.you.want.the.original.functionality" ) );
        m_write_all_and_exit_closes_IDEA.setToolTipText( Localized_messages.message( "write.all.and.exit.command.will.close.the.idea.this.is.original.brief.functionality.uncheck.this.if.you.want.to.close.only.the.editors.but.keep.the.idea.running" ) );
        m_do_not_show_virtual_space_setting_dialog.setToolTipText( Localized_messages.message( "disables.showing.of.the.virtual.space.setting.dialog.again.when.initiating.the.right.side.of.window.action.uncheck.if.you.want.to.see.this.dialog.again" ) );
        m_show_document_information.setToolTipText( Localized_messages.message( "shows.additional.document.information.offsets.lengths.in.the.status.bar.uncheck.if.you.don.t.want.to.see.extra.document.information.in.the.status.bar" ) );

        myMainPanel = FormBuilder.createFormBuilder()
                                 .addComponent( m_enabled,
                                                1 )
                                 .addComponent( m_show_icon_in_status_bar,
                                                1 )
                                 .addComponent( m_paste_lines_at_home,
                                                1 )
                                 .addComponent( m_use_brief_home,
                                                1 )
                                 .addComponent( m_check_active_keymap_is_brief,
                                                1 )
                                 .addComponent( m_exit_only_closes_editor,
                                                1 )
                                 .addComponent( m_write_all_and_exit_closes_IDEA,
                                                1 )
                                 .addComponent( m_do_not_show_virtual_space_setting_dialog,
                                                1 )
                                 .addComponent( m_show_document_information,
                                                1 )
                                 .addComponentFillVertically( new JPanel(),
                                                              0 )
                                 .getPanel();
    }

    /**
     * Usually not invoked directly, see class javadoc.
     */
    @Override
    public void dispose() { }

    public JPanel getPanel()
    {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent()
    {
        return m_enabled;
    }

    public boolean get_enabled()
    {
        return m_enabled.isSelected();
    }
    public void set_enabled( boolean check )
    {
        m_enabled.setSelected( check );
    }

    public boolean get_show_icon_in_status_bar() { return m_show_icon_in_status_bar.isSelected(); }
    public void set_show_icon_in_status_bar( boolean check ) { m_show_icon_in_status_bar.setSelected( check ); }

    public boolean get_paste_lines_at_home()
    {
        return m_paste_lines_at_home.isSelected();
    }
    public void set_paste_lines_at_home( boolean check )
    {
        m_paste_lines_at_home.setSelected( check );
    }

    public boolean get_use_brief_home()
    {
        return m_use_brief_home.isSelected();
    }
    public void set_use_brief_home( boolean check )
    {
        m_use_brief_home.setSelected( check );
    }

    public boolean get_check_active_keymap_is_brief()
    {
        return m_check_active_keymap_is_brief.isSelected();
    }
    public void set_check_active_keymap_is_brief( boolean check ) { m_check_active_keymap_is_brief.setSelected( check ); }

    public boolean get_exit_only_closes_editor()
    {
        return m_exit_only_closes_editor.isSelected();
    }
    public void set_exit_only_closes_editor( boolean check ) { m_exit_only_closes_editor.setSelected( check ); }

    public boolean get_write_all_and_exit_closes_IDEA()
    {
        return m_write_all_and_exit_closes_IDEA.isSelected();
    }
    public void set_write_all_and_exit_closes_IDEA( boolean check ) { m_write_all_and_exit_closes_IDEA.setSelected( check ); }

    public boolean get_do_not_show_virtual_space_setting_dialog() { return m_do_not_show_virtual_space_setting_dialog.isSelected(); }
    public void set_do_not_show_virtual_space_setting_dialog( boolean check ) { m_do_not_show_virtual_space_setting_dialog.setSelected( check ); }

    public boolean get_show_document_information()
    {
        return m_show_document_information.isSelected();
    }
    public void set_show_document_information( boolean check ) { m_show_document_information.setSelected( check ); }
}
