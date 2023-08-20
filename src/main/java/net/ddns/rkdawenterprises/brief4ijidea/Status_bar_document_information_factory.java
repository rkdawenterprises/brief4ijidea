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
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.*;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavadocReference")
public class Status_bar_document_information_factory
        implements StatusBarWidgetFactory {
    public static final @NonNls String ID = "Brief4ijidea_status_bar_document_information_factory_ID";
    public static final String DISPLAY_NAME = Localized_messages.message("document.information");

    private final List<Status_bar_document_information> m_widgets = new ArrayList<>();

    Status_bar_document_information_factory() {
        ((EditorEventMulticasterEx) (EditorFactory.getInstance().getEventMulticaster())).addFocusChangeListener(
                new FocusChangeListener() {
                    /**
                     * @param editor The editor with focus.
                     */
                    @Override
                    public void focusGained(@NotNull Editor editor) {
                        Project project = editor.getProject();
                        if (project != null) {
                            Status_bar_document_information widget = get_widget(project);
                            if(widget != null) {
                                widget.editor_focus_gained(editor);
                            }
                        }

                        update_widget();
                    }

                    /**
                     * @param editor The editor with focus.
                     */
                    @Override
                    public void focusLost(@NotNull Editor editor) {
                        Project project = editor.getProject();
                        if (project != null) {
                            Status_bar_document_information widget = get_widget(project);
                            if(widget != null) {
                                widget.editor_focus_lost(editor);
                            }
                        }

                        update_widget();
                    }
                },
                State_component.get_instance());
    }

    @Nullable
    private Status_bar_document_information get_widget(Project project) {
        synchronized (m_widgets) {
            if (m_widgets.isEmpty()) return null;

            for (Status_bar_document_information widget : m_widgets) {
                if (widget.m_project == project) return widget;
            }
        }

        return null;
    }

    public static void update_widget() {
        ProjectManager project_manager = ProjectManager.getInstanceIfCreated();
        if (project_manager == null) return;

        StatusBarWidgetFactory status_bar_widget_factory = StatusBarWidgetFactory.EP_NAME.findExtension(Status_bar_document_information_factory.class);
        if (status_bar_widget_factory == null) return;

        for (Project project : project_manager.getOpenProjects()) {
            StatusBarWidgetsManager status_bar_widgets_manager = project.getService(StatusBarWidgetsManager.class);
            if (status_bar_widgets_manager != null) {
                status_bar_widgets_manager.updateWidget(status_bar_widget_factory);
            }

            StatusBar status_bar = WindowManager.getInstance()
                    .getStatusBar(project);
            if (status_bar != null) {
                status_bar.updateWidget(ID);
            }
        }
    }

    /**
     * @return Widget identifier. Used to store visibility settings.
     */
    @Override
    public @NonNls @NotNull String getId() {
        return ID;
    }

    /**
     * @return Widget's display name. Used to refer a widget in UI,
     * e.g. for "Enable/disable &lt;display name>" action names
     * or for checkbox texts in settings.
     */
    @Override
    public @Nls @NotNull String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * Returns availability of widget.
     * <p>
     * `False` means that IDE won't try to create a widget or will dispose it on {@link StatusBarWidgetsManager#updateWidget} call.
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
     * @param project The current project.
     */
    @Override
    public boolean isAvailable(@NotNull Project project) {
        return State_component.get_instance()
                .get_show_document_information();
    }

    /**
     * Creates a widget to be added to the status bar.
     * <p>
     * Once the method is invoked on project initialization, the widget won't be recreated or disposed implicitly.
     * <p>
     * You may need to recreate it if:
     * <ul>
     * <li>its availability has changed. See {@link #isAvailable(Project)}</li>
     * <li>its visibility has changed. See StatusBarWidgetSettings.</li>
     * </ul>
     * <p>
     * To do this, you need to explicitly invoke {@link StatusBarWidgetsManager#updateWidget(StatusBarWidgetFactory)}
     * to recreate the widget and re-add it to the status bar.
     *
     * @param project The current project.
     */
    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project)
    {
        Status_bar_document_information status_bar_document_information =
                new Status_bar_document_information(project, State_component.get_instance());

        synchronized (m_widgets) {
            m_widgets.add(status_bar_document_information);
        }

        return status_bar_document_information;
    }

    /**
     * @param widget The widget to dispose of.
     */
    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget)
    {
        synchronized(m_widgets)
        {
            m_widgets.remove((Status_bar_document_information)widget);
        }
    }

    /**
     * @param statusBar The given status bar widget.
     * @return Returns whether the widget can be enabled on the given status bar right now.
     * Status bar's context menu with enable/disable action depends on the result of this method.
     * <p>
     * It's better to have this method aligned with {@link EditorBasedStatusBarPopup.WidgetState#HIDDEN},
     * whenever state is {@code HIDDEN}, this method should return {@code false}.
     * Otherwise, enabling widget via context menu will not have any visual effect.
     * <p>
     * E.g. {@link EditorBasedWidget} are available if editor is opened in a frame that given status bar is attached to
     * <p>
     * For creating editor based widgets see also {@link StatusBarEditorBasedWidgetFactory}
     */
    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return false;
    }

    /**
     * @return {@code true} if the widget should be created by default.
     * Otherwise, the user must enable it explicitly via status bar context menu or settings.
     */
    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    /**
     * @return Returns whether the user should be able to enable or disable the widget.
     * <p>
     * Some widgets are controlled by application-level settings (e.g., Memory indicator)
     * or cannot be disabled (e.g., Write thread indicator) and thus shouldn't be configurable via status bar context menu or settings.
     */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    private static class Status_bar_document_information
            extends TextPanel
            implements CustomStatusBarWidget, Disposable {
        private static final String s_default_text = Localized_messages.message("l.c.o");

        private final Project m_project;

        private final CaretListener m_caret_listener = new CaretListener() {
            /**
             * Called when the caret position has changed.<p>
             * Only explicit caret movements (caused by {@code move*()} methods in {@link Caret} and {@link CaretModel}) are reported, 'induced' changes of
             * caret offset due to document modifications are not reported.
             *
             * @param event the event containing information about the caret movement.
             */
            @Override
            public void caretPositionChanged(@NotNull CaretEvent event) {
                int line = event.getNewPosition().line;
                int column = event.getNewPosition().column;
                int offset = event.getEditor().logicalPositionToOffset(event.getNewPosition());
                setText(String.format("[%d:%d:%d]", line, column, offset));
            }
        };

        Status_bar_document_information(Project project,
                                        Disposable disposable) {
            m_project = project;
            Disposer.register(disposable, this);
        }

        /**
         * @return This factory.
         */
        @Override
        public JComponent getComponent() {
            return this;
        }

        /**
         * @return The ID of this factory.
         */
        @Override
        public @NonNls @NotNull String ID() {
            return ID;
        }

        /**
         * @param statusBar The widget being installed.
         */
        @Override
        public void install(@NotNull StatusBar statusBar) {
            setText(s_default_text);
        }

        /**
         * Usually not invoked directly, see class javadoc.
         */
        @Override
        public void dispose() {
        }

        void editor_focus_lost(Editor editor) {
            editor.getCaretModel().removeCaretListener(m_caret_listener);
            setText(s_default_text);
        }

        void editor_focus_gained(Editor editor) {
            editor.getCaretModel().addCaretListener(m_caret_listener);
            LogicalPosition position = editor.getCaretModel().getLogicalPosition();
            int line = position.line;
            int column = position.column;
            int offset = editor.logicalPositionToOffset(position);
            setText(String.format("[%d:%d:%d]", line, column, offset));
        }
    }
}
