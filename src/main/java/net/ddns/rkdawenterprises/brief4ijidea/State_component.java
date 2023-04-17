
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

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.KeymapImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.editor_gained_focus;
import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.editor_lost_focus;

/**
 * Supports storing the application settings in a persistent way. The {@link State} and {@link Storage} annotations
 * define the name of the data and the file name where these persistent application settings are stored.
 */
@State(name = "brief4ijidea_settings",
        storages = { @Storage("$APP_CONFIG$/brief4ijidea_settings.xml") })
public class State_component
        implements PersistentStateComponent<Persisted_state>, Disposable
{
    /**
     * Usually not invoked directly, see class javadoc.
     */
    @Override
    public void dispose()
    {
        try { set_enabled( false ); }
        catch( Exception exception )
        {
            System.out.println( "brief4ijidea.State_component.dispose" + exception.getLocalizedMessage() );
        }
    }

    private Persisted_state m_persisted_state = new Persisted_state();

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. {@code null} value indicates that
     * the returned state won't be stored, as a result previously stored state will be used.
     *
     * @see XmlSerializer
     */
    @Override
    public Persisted_state getState()
    {
        m_persisted_state.setVersion( 3 );
        return m_persisted_state;
    }

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDE was running.
     * <p>
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     *
     * @see XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState( @NotNull Persisted_state state )
    {
        m_persisted_state = state;
    }

    public static boolean enabled()
    {
        return get_instance().get_enabled();
    }

    public static void enable( boolean enable )
            throws IOException, ParserConfigurationException, SAXException
    {
        if( enabled() == enable ) return;
        get_instance().set_enabled( enable );
    }

    public static State_component get_instance()
    {
        return ApplicationManager.getApplication()
                                 .getService( State_component.class );
    }

    private boolean m_initialized = false;

    public void initialize()
    {
        if( m_persisted_state.getEnabled() )
        {
            ApplicationManager.getApplication()
                              .invokeLater( () ->
                                            {
                                                try
                                                {
                                                    set_enabled( true );
                                                }
                                                catch( Exception exception )
                                                {
                                                    throw new RuntimeException( "Could not initialize plugin: " + exception );
                                                }
                                            } );
        }

        m_initialized = true;
    }

    private KeymapManagerListener m_keymap_manager_listener = null;

    private void do_enable()
            throws IOException, ParserConfigurationException, SAXException
    {
        String previous_keymap_name = Actions_component.setup_actions();
        m_persisted_state.setPrevious_keymap_name( previous_keymap_name );
        System.out.println( "brief4ijidea.do_enable: Previous keymap name:" + previous_keymap_name );
        update_active_keystroke_map();

        m_keymap_manager_listener = new KeymapManagerListener()
        {
            @Override
            public void activeKeymapChanged( @Nullable Keymap keymap )
            {
                keymap_modified( keymap );
            }

            @Override
            public void shortcutChanged( @NotNull Keymap keymap,
                                         @NotNull String actionId )
            {
                keymap_modified( null );
            }

            private void keymap_modified( @Nullable Keymap keymap )
            {
                if( keymap != null )
                {
                    m_persisted_state.setPrevious_keymap_name( keymap.getName() );
                }

                if( get_check_active_keymap_is_brief() )
                {
                    set_check_active_keymap_is_brief( false );
                    status_bar_message( "Startup check for Brief keymap disabled" );
                }

                update_active_keystroke_map();
            }
        };

        KeymapManagerEx.getInstanceEx()
                       .addWeakListener( m_keymap_manager_listener );

        ( (EditorEventMulticasterEx)( EditorFactory.getInstance()
                                                   .getEventMulticaster() ) ).addFocusChangeListener( new FocusChangeListener()
                                                                                                      {
                                                                                                          @Override
                                                                                                          public void focusGained( @NotNull Editor editor )
                                                                                                          {
                                                                                                              editor_gained_focus( editor );
                                                                                                          }

                                                                                                          @Override
                                                                                                          public void focusLost( @NotNull Editor editor )
                                                                                                          {
                                                                                                              editor_lost_focus( editor );
                                                                                                          }
                                                                                                      },
                                                                                                      this );

        status_bar_message( "Plugin enabled" );
    }

    private void do_disable()
    {
        if( m_keymap_manager_listener != null )
        {
            KeymapManagerEx.getInstanceEx()
                           .removeWeakListener( m_keymap_manager_listener );
        }

        Actions_component.disable_actions( m_persisted_state.getPrevious_keymap_name() );

        if( m_active_keystroke_map != null )
        {
            m_active_keystroke_map.clear();
            m_active_keystroke_map = null;
        }

        status_bar_message( "Plugin disabled" );
    }

    public boolean get_enabled() { return m_persisted_state.getEnabled(); }
    public void set_enabled( boolean enabled )
            throws IOException, ParserConfigurationException, SAXException
    {
        m_persisted_state.setEnabled( enabled );

        if( m_persisted_state.getEnabled() )
        {
            do_enable();
        }
        else
        {
            do_disable();
        }

        Status_bar_icon_factory.update_widget();
    }

    public boolean get_show_icon_in_status_bar() { return m_persisted_state.getShow_icon_in_status_bar(); }

    public void set_show_icon_in_status_bar( boolean show_icon_in_status_bar )
    {
        m_persisted_state.setShow_icon_in_status_bar( show_icon_in_status_bar );
        Status_bar_icon_factory.update_widget();
    }

    public boolean get_paste_lines_at_home()
    {
        return m_persisted_state.getPaste_lines_at_home();
    }
    public void set_paste_lines_at_home( boolean paste_lines_at_home ) { m_persisted_state.setPaste_lines_at_home( paste_lines_at_home ); }

    public boolean get_use_brief_home()
    {
        return m_persisted_state.getUse_brief_home();
    }
    public void set_use_brief_home( boolean use_brief_home )
    {
        m_persisted_state.setUse_brief_home( use_brief_home );
    }

    public boolean get_check_active_keymap_is_brief()
    {
        return m_persisted_state.getCheck_active_keymap_is_brief();
    }
    public void set_check_active_keymap_is_brief( boolean check_active_keymap_is_brief ) { m_persisted_state.setCheck_active_keymap_is_brief( check_active_keymap_is_brief ); }

    public boolean get_exit_only_closes_editor()
    {
        return m_persisted_state.getExit_only_closes_editor();
    }
    public void set_exit_only_closes_editor( boolean exit_only_closes_editor ) { m_persisted_state.setExit_only_closes_editor( exit_only_closes_editor ); }

    public boolean get_write_all_and_exit_closes_IDEA() { return m_persisted_state.getWrite_all_and_exit_closes_IDEA(); }
    public void set_write_all_and_exit_closes_IDEA( boolean write_all_and_exit_closes_IDEA ) { m_persisted_state.setWrite_all_and_exit_closes_IDEA( write_all_and_exit_closes_IDEA ); }

    public boolean get_do_not_show_virtual_space_setting_dialog() { return m_persisted_state.getDo_not_show_virtual_space_setting_dialog(); }
    public void set_do_not_show_virtual_space_setting_dialog( boolean do_not_show_virtual_space_setting_dialog ) { m_persisted_state.setDo_not_show_virtual_space_setting_dialog( do_not_show_virtual_space_setting_dialog ); }

    public boolean get_show_document_information() { return m_persisted_state.getShow_document_information(); }
    public void set_show_document_information( boolean show_document_information )
    {
        m_persisted_state.setShow_document_information( show_document_information );
        Status_bar_document_information_factory.update_widget();
    }

    public static void status_bar_message( final String message )
    {
        if( message != null )
        {
            System.out.println( "brief4ijidea.State_component.status_bar_message: " + message );
        }

        Project[] projects = ProjectManager.getInstance()
                                           .getOpenProjects();
        for( Project project : projects )
        {
            StatusBar bar = WindowManager.getInstance()
                                         .getStatusBar( project );
            if( bar != null )
            {
                if( message == null || message.length() == 0 )
                {
                    bar.setInfo( "" );
                }
                else
                {
                    bar.setInfo( Localized_messages.message( "status.bar.text.brief", message ) );
                }
            }
        }
    }

    public static PluginId get_plugin_id()
    {
        return PluginId.getId( "net.ddns.rkdawenterprises.brief4ijidea" );
    }

    public static String get_version()
    {
        String version = "N/A";
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin( get_plugin_id() );
        if( ( plugin != null ) && ( plugin.getVersion() != null ) )
        {
            version = plugin.getVersion();
        }

        return version;
    }

    public boolean get_initialized()
    {
        return m_initialized;
    }

    public Map<String, Key_action> get_active_keystroke_map()
    {
        return m_active_keystroke_map;
    }

    private Map<String, Key_action> m_active_keystroke_map = null;

    private void update_active_keystroke_map()
    {
        KeymapManagerEx keymap_manager = KeymapManagerEx.getInstanceEx();
        m_active_keystroke_map =
                Key_event_to_string.Companion.to_map( (KeymapImpl)keymap_manager.getActiveKeymap() );
    }
}
