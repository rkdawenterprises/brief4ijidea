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

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Application_configurable
        implements Configurable
{
    private Settings_UI_component m_settings_ui_component;

    /**
     * Returns the visible name of the configurable component. Note, that this method must return the display name that
     * is equal to the display name declared in XML to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    @Override
    public String getDisplayName()
    {
        return Localized_messages.message( "configurable.name.brief.editor.emulation.settings" );
    }

    /**
     * @return component which should be focused when the dialog appears on the screen.
     */
    @Override
    public @Nullable JComponent getPreferredFocusedComponent()
    {
        return m_settings_ui_component.getPreferredFocusedComponent();
    }

    /**
     * Creates new Swing form that enables user to configure the settings. Usually this method is called on the EDT, so
     * it should not take a long time.
     * <p>
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     *
     * @return new Swing form to show, or {@code null} if it cannot be created
     *
     * @see #disposeUIResources
     */
    @Override
    public @Nullable JComponent createComponent()
    {
        m_settings_ui_component = new Settings_UI_component();
        return m_settings_ui_component.getPanel();
    }

    /**
     * Indicates whether the Swing form was modified or not. This method is called very often, so it should not take a
     * long time.
     *
     * @return {@code true} if the settings were modified, {@code false} otherwise
     */
    @Override
    public boolean isModified()
    {
        State_component state_component = State_component.get_instance();
        boolean modified = false;
        modified |= m_settings_ui_component.get_enabled() != state_component.get_enabled();
        modified |= m_settings_ui_component.get_show_icon_in_status_bar() != state_component.get_show_icon_in_status_bar();
        modified |= m_settings_ui_component.get_paste_lines_at_home() != state_component.get_paste_lines_at_home();
        modified |= m_settings_ui_component.get_use_brief_home() != state_component.get_use_brief_home();
        modified |= m_settings_ui_component.get_check_active_keymap_is_brief() != state_component.get_check_active_keymap_is_brief();
        modified |= m_settings_ui_component.get_exit_only_closes_editor() != state_component.get_exit_only_closes_editor();
        modified |= m_settings_ui_component.get_write_all_and_exit_closes_IDEA() != state_component.get_write_all_and_exit_closes_IDEA();
        modified |= m_settings_ui_component.get_do_not_show_virtual_space_setting_dialog() != state_component.get_do_not_show_virtual_space_setting_dialog();
        modified |= m_settings_ui_component.get_show_document_information() != state_component.get_show_document_information();
        return modified;
    }

    /**
     * Stores the settings from the Swing form to the configurable component. This method is called on EDT upon user's
     * request.
     *
     */
    @Override
    public void apply()
    {
        State_component state_component = State_component.get_instance();
        try { state_component.set_enabled( m_settings_ui_component.get_enabled() ); }
        catch( Exception exception ) { System.out.println( "brief4ijidea.Application_configurable.apply: " + exception.getLocalizedMessage() ); }
        state_component.set_show_icon_in_status_bar( m_settings_ui_component.get_show_icon_in_status_bar() );
        state_component.set_paste_lines_at_home( m_settings_ui_component.get_paste_lines_at_home() );
        state_component.set_use_brief_home( m_settings_ui_component.get_use_brief_home() );
        state_component.set_check_active_keymap_is_brief( m_settings_ui_component.get_check_active_keymap_is_brief() );
        state_component.set_exit_only_closes_editor( m_settings_ui_component.get_exit_only_closes_editor() );
        state_component.set_write_all_and_exit_closes_IDEA( m_settings_ui_component.get_write_all_and_exit_closes_IDEA() );
        state_component.set_do_not_show_virtual_space_setting_dialog( m_settings_ui_component.get_do_not_show_virtual_space_setting_dialog() );
        state_component.set_show_document_information( m_settings_ui_component.get_show_document_information() );
    }

    /**
     * Loads the settings from the configurable component to the Swing form. This method is called on EDT immediately
     * after the form creation or later upon user's request.
     */
    @Override
    public void reset()
    {
        State_component state_component = State_component.get_instance();
        m_settings_ui_component.set_enabled( state_component.get_enabled() );
        m_settings_ui_component.set_show_icon_in_status_bar( state_component.get_show_icon_in_status_bar() );
        m_settings_ui_component.set_paste_lines_at_home( state_component.get_paste_lines_at_home() );
        m_settings_ui_component.set_use_brief_home( state_component.get_use_brief_home() );
        m_settings_ui_component.set_check_active_keymap_is_brief( state_component.get_check_active_keymap_is_brief() );
        m_settings_ui_component.set_exit_only_closes_editor( state_component.get_exit_only_closes_editor() );
        m_settings_ui_component.set_write_all_and_exit_closes_IDEA( state_component.get_write_all_and_exit_closes_IDEA() );
        m_settings_ui_component.set_do_not_show_virtual_space_setting_dialog( state_component.get_do_not_show_virtual_space_setting_dialog() );
        m_settings_ui_component.set_show_document_information( state_component.get_show_document_information() );
    }

    /**
     * Notifies the configurable component that the Swing form will be closed. This method should dispose all resources
     * associated with the component.
     */
    @Override
    public void disposeUIResources()
    {
        m_settings_ui_component.dispose();
        m_settings_ui_component = null;
    }
}
