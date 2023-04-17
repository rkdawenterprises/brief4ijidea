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

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetSettings;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager;
import com.intellij.ui.IconManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

@SuppressWarnings("JavadocReference")
public class Status_bar_icon_factory
        implements StatusBarWidgetFactory
{
    public static final @NonNls String ID = "Brief4ijidea_status_bar_brief_for_ij_idea_factory_ID";
    public static final String DISPLAY_NAME = Localized_messages.message( "action.brief.editor.emulator.text" );

    /**
     * @return Widget identifier. Used to store visibility settings.
     */
    @Override
    public @NonNls @NotNull String getId()
    {
        return ID;
    }

    /**
     * @return Widget's display name. Used to refer a widget in UI, e.g. for "Enable/disable &lt;display name>" action
     * names or for checkbox texts in settings.
     */
    @Override
    public @Nls @NotNull String getDisplayName()
    {
        return DISPLAY_NAME;
    }

    /**
     * Returns availability of widget.
     * <p>
     * `False` means that IDE won't try to create a widget or will dispose it on {@link
     * StatusBarWidgetsManager#updateWidget} call.
     * <p>
     * E.g. `false` can be returned for
     * <ul>
     * <li>notifications widget if Event log is shown as a tool window</li>
     * <li>memory indicator widget if it is disabled in the appearance settings</li>
     * <li>git widget if there are no git repos in a project</li>
     * </ul>
     * <p>
     * Whenever availability is changed, you need to call {@link StatusBarWidgetsManager#updateWidget(StatusBarWidgetFactory)}
     * explicitly to get status bar updated.
     *
     * @param project Project
     */
    @Override
    public boolean isAvailable( @NotNull Project project )
    {
        return State_component.get_instance()
                              .get_show_icon_in_status_bar();
    }

    /**
     * Creates a widget to be added to the status bar.
     * <p>
     * Once the method is invoked on project initialization, the widget won't be recreated or disposed implicitly.
     * <p>
     * You may need to recreate it if:
     * <ul>
     * <li>its availability has changed. See {@link #isAvailable(Project)}</li>
     * <li>its visibility has changed. See {@link StatusBarWidgetSettings}</li>
     * </ul>
     * <p>
     * To do this, you need to explicitly invoke {@link StatusBarWidgetsManager#updateWidget(StatusBarWidgetFactory)}
     * to recreate the widget and re-add it to the status bar.
     *
     * @param project Project
     */
    @Override
    public @NotNull StatusBarWidget createWidget( @NotNull Project project )
    {
        return new Status_bar_widget( project );
    }

    @Override
    public void disposeWidget( @NotNull StatusBarWidget widget ) {}

    /**
     * @param statusBar StatusBar
     *
     * @return Returns whether the widget can be enabled on the given status bar right now. Status bar's context menu
     * with enable/disable action depends on the result of this method.
     * <p>
     * It's better to have this method aligned with {@link EditorBasedStatusBarPopup.WidgetState#HIDDEN}, whenever state
     * is {@code HIDDEN}, this method should return {@code false}. Otherwise, enabling widget via context menu will not
     * have any visual effect.
     * <p>
     * E.g. {@link EditorBasedWidget} are available if editor is opened in a frame that given status bar is attached to
     * <p>
     * For creating editor based widgets see also {@link StatusBarEditorBasedWidgetFactory}
     */
    @Override
    public boolean canBeEnabledOn( @NotNull StatusBar statusBar )
    {
        return true;
    }

    /**
     * @return {@code true} if the widget should be created by default. Otherwise, the user must enable it explicitly
     * via status bar context menu or settings.
     */
    @Override
    public boolean isEnabledByDefault()
    {
        return true;
    }

    /**
     * @return Returns whether the user should be able to enable or disable the widget.
     * <p>
     * Some widgets are controlled by application-level settings (e.g., Memory indicator) or cannot be disabled (e.g.,
     * Write thread indicator) and thus shouldn't be configurable via status bar context menu or settings.
     */
    @Override
    public boolean isConfigurable()
    {
        return true;
    }

    public static void update_widget()
    {
        ProjectManager project_manager = ProjectManager.getInstanceIfCreated();
        if( project_manager == null ) return;

        StatusBarWidgetFactory status_bar_widget_factory = StatusBarWidgetFactory.EP_NAME.findExtension( Status_bar_icon_factory.class );
        if( status_bar_widget_factory == null ) return;

        for( Project project : project_manager.getOpenProjects() )
        {
            StatusBarWidgetsManager status_bar_widgets_manager = project.getService( StatusBarWidgetsManager.class );
            if( status_bar_widgets_manager != null )
            {
                status_bar_widgets_manager.updateWidget( status_bar_widget_factory );
            }

            StatusBar status_bar = WindowManager.getInstance()
                                                .getStatusBar( project );
            if( status_bar != null )
            {
                status_bar.updateWidget( ID );
            }
        }
    }

    private static class Open_settings_action
            extends DumbAwareAction
    {
        public Open_settings_action( String text )
        {
            super( text );
        }

        @Override
        public void actionPerformed( @NotNull AnActionEvent e )
        {
            ShowSettingsUtil.getInstance()
                            .showSettingsDialog( e.getProject(),
                                                 Application_configurable.class );
        }
    }

    private static class Browse_link_action
            extends DumbAwareAction
    {
        private final String m_URI;

        public Browse_link_action( String text,
                                   String a_URI,
                                   @NotNull Icon icon )
        {
            super( text,
                   null,
                   icon );
            m_URI = a_URI;
        }

        @Override
        public void actionPerformed( @NotNull AnActionEvent e )
        {
            BrowserUtil.browse( m_URI );
        }
    }

    private static class Status_bar_widget
            implements StatusBarWidget, StatusBarWidget.IconPresentation
    {
        private final Project m_project;

        public Status_bar_widget( @NotNull Project project )
        {
            m_project = project;
        }

        @Override
        public @NonNls @NotNull String ID()
        {
            return Status_bar_icon_factory.ID;
        }

        @Override
        public @Nullable WidgetPresentation getPresentation()
        {
            return this;
        }

        @Override
        public void install( @NotNull StatusBar statusBar ) { }

        /**
         * Usually not invoked directly, see class javadoc.
         */
        @Override
        public void dispose() { }

        @Override
        public @Nullable Icon getIcon()
        {
            if( State_component.enabled() && State_component.get_instance()
                                                            .get_initialized() )
            {
                return IconManager.getInstance()
                                  .getIcon( Localized_messages.message( "icons.brief4ijidea.svg" ),
                                            Status_bar_widget.class );
            }
            else
            {
                return IconManager.getInstance()
                                  .getIcon( Localized_messages.message( "icons.brief4ijidea.disabled.svg" ),
                                            Status_bar_widget.class );
            }
        }

        @Override
        public @Nullable String getTooltipText()
        {
            return Localized_messages.message( "tooltip.in", DISPLAY_NAME, m_project.getName() );
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer()
        {
            return event ->
            {
                Component component = event.getComponent();
                ListPopup list_popup = get_popup( DataManager.getInstance()
                                                             .getDataContext( component ) );
                Dimension dimension = list_popup.getContent()
                                                .getPreferredSize();
                list_popup.show( new RelativePoint( component,
                                                    new Point( 0,
                                                               -dimension.height ) ) );
            };
        }

        private ListPopup get_popup( DataContext data_context )
        {
            DefaultActionGroup actions = get_action_group();
            ListPopup popup = JBPopupFactory.getInstance()
                                            .createActionGroupPopup(
                                                    DISPLAY_NAME,
                                                    actions,
                                                    data_context,
                                                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                    false,
                                                    ActionPlaces.POPUP );
            String ad_text = Localized_messages.message( "popup.advertisement.version", State_component.get_version() );
            popup.setAdText( ad_text,
                             SwingConstants.CENTER );

            return popup;
        }

        private DefaultActionGroup get_action_group()
        {
            DefaultActionGroup action_group_top = new DefaultActionGroup();
            action_group_top.setPopup( true );

            action_group_top.add( ActionManager.getInstance()
                                               .getAction( "net.ddns.rkdawenterprises.brief4ijidea.actions.Enabled_toggle_action" ) );
            action_group_top.add( new Open_settings_action( Localized_messages.message( "settings" ) ) );
            action_group_top.addSeparator();
            // URI taken from keymap file.
            action_group_top.add( new Browse_link_action( Localized_messages.message( "submit.issue" ),
                                                          Localized_messages.message( "status.bar.issues.URI" ),
                                                          AllIcons.Vcs.Vendors.Github ) );
            // URI taken from keymap file.
            action_group_top.add( new Browse_link_action( Localized_messages.message( "repository" ),
                                                          Localized_messages.message( "status.bar.repository.URI" ),
                                                          AllIcons.Vcs.Vendors.Github ) );
            return action_group_top;
        }
    }
}
