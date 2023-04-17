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

@file:Suppress("ClassName",
               "FunctionName",
               "LocalVariableName",
               "PropertyName",
               "PrivatePropertyName",
               "RedundantSemicolon",
               "UsePropertyAccessSyntax",
               "KDocUnresolvedReference")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.TextPanel
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import org.jetbrains.annotations.NonNls
import javax.swing.JComponent

class Status_bar_document_information_factory : StatusBarWidgetFactory
{
    private val m_widgets: MutableList<Status_bar_document_information> = mutableListOf()

    init
    {
        (EditorFactory.getInstance()
            .eventMulticaster as EditorEventMulticasterEx)
            .addFocusChangeListener(object : FocusChangeListener
                                    {
                                        override fun focusGained(editor: Editor)
                                        {
                                            if(editor.project != null)
                                            {
                                                get_widget(editor.project!!)?.editor_focus_gained(editor)
                                            }

                                            update_widget();
                                        }

                                        override fun focusLost(editor: Editor)
                                        {
                                            if(editor.project != null)
                                            {
                                                get_widget(editor.project!!)?.editor_focus_lost(editor)
                                            }

                                            update_widget();
                                        }
                                    },
                                    State_component.get_instance())
    }

    companion object
    {
        @NonNls
        const val ID = "Brief4ijidea_status_bar_document_information_factory_ID"
        val DISPLAY_NAME = Localized_messages.message("document.information")

        @JvmStatic
        fun update_widget()
        {
            val project_manager = ProjectManager.getInstanceIfCreated() ?: return

            val status_bar_widget_factory =
                StatusBarWidgetFactory.EP_NAME.findExtension(Status_bar_document_information_factory::class.java)
                    ?: return

            for(project in project_manager.openProjects)
            {
                val status_bar_widgets_manager = project.getService(StatusBarWidgetsManager::class.java)
                status_bar_widgets_manager?.updateWidget(status_bar_widget_factory)

                val status_bar = WindowManager.getInstance()
                    .getStatusBar(project)
                status_bar?.updateWidget(ID)
            }
        }
    }

    fun get_widget(project: Project): Status_bar_document_information?
    {
        synchronized(m_widgets)
        {
            if(m_widgets.isEmpty()) return null;

            for(widget in m_widgets)
            {
                if(widget.m_project == project) return widget;
            }
        }

        return null;
    }

    /**
     * @return `true` if the widget should be created by default. Otherwise, the user must enable it explicitly
     * via status bar context menu or settings.
     */
    override fun isEnabledByDefault(): Boolean = true

    /**
     * @return Returns whether the user should be able to enable or disable the widget.
     *
     *
     * Some widgets are controlled by application-level settings (e.g., Memory indicator) or cannot be disabled (e.g.,
     * Write thread indicator) and thus shouldn't be configurable via status bar context menu or settings.
     */
    override fun isConfigurable(): Boolean = true

    /**
     * @return Widget identifier. Used to store visibility settings.
     */
    override fun getId(): String = ID

    /**
     * @return Widget's display name. Used to refer a widget in UI, e.g. for "Enable/disable &lt;display name>" action
     * names or for checkbox texts in settings.
     */
    override fun getDisplayName(): String = DISPLAY_NAME

    /**
     * Returns availability of widget.
     *
     *
     * `False` means that IDE won't try to create a widget or will dispose it on [ ][StatusBarWidgetsManager.updateWidget] call.
     *
     *
     * E.g. `false` can be returned for
     *
     *  * notifications widget if Event log is shown as a tool window
     *  * memory indicator widget if it is disabled in the appearance settings
     *  * git widget if there are no git repos in a project
     *
     *
     *
     * Whenever availability is changed, you need to call [StatusBarWidgetsManager.updateWidget]
     * explicitly to get status bar updated.
     *
     * @param project
     */
    override fun isAvailable(project: Project): Boolean
    {
        return State_component.get_instance().get_show_document_information();
    }

    /**
     * Creates a widget to be added to the status bar.
     *
     *
     * Once the method is invoked on project initialization, the widget won't be recreated or disposed implicitly.
     *
     *
     * You may need to recreate it if:
     *
     *  * its availability has changed. See [.isAvailable]
     *  * its visibility has changed. See [StatusBarWidgetSettings]
     *
     *
     *
     * To do this, you need to explicitly invoke [StatusBarWidgetsManager.updateWidget]
     * to recreate the widget and re-add it to the status bar.
     *
     * @param project
     */
    override fun createWidget(project: Project): StatusBarWidget
    {
        val status_bar_document_information = Status_bar_document_information(project,
                                               State_component.get_instance())

        synchronized(m_widgets)
        {
            m_widgets.add(status_bar_document_information)
        }

        return status_bar_document_information
    }

    override fun disposeWidget(widget: StatusBarWidget)
    {
        synchronized(m_widgets)
        {
            m_widgets.remove(widget as Status_bar_document_information)
        }
    }

    /**
     * @param statusBar
     *
     * @return Returns whether the widget can be enabled on the given status bar right now. Status bar's context menu
     * with enable/disable action depends on the result of this method.
     *
     *
     * It's better to have this method aligned with [EditorBasedStatusBarPopup.WidgetState.HIDDEN], whenever state
     * is `HIDDEN`, this method should return `false`. Otherwise, enabling widget via context menu will not
     * have any visual effect.
     *
     *
     * E.g. [EditorBasedWidget] are available if editor is opened in a frame that given status bar is attached to
     *
     *
     * For creating editor based widgets see also [StatusBarEditorBasedWidgetFactory]
     */
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    class Status_bar_document_information(val m_project: Project,
                                          disposable: Disposable) : TextPanel(), CustomStatusBarWidget,
                                                                                  Disposable
    {
        init
        {
            Disposer.register(disposable,
                              this)
        }

        companion object
        {
            val s_default_text = Localized_messages.message("l.c.o")
        }

        private val m_caret_listener = object : CaretListener
        {
            /**
             * Called when the caret position has changed.
             *
             * Only explicit caret movements (caused
             * by 'move' methods in [Caret] and [CaretModel]) are reported, 'induced'
             * changes of caret offset due to document modifications are not reported.
             *
             * @param event the event containing information about the caret movement.
             */
            override fun caretPositionChanged(event: CaretEvent)
            {
                val offset = event.editor.logicalPositionToOffset(event.newPosition)
                text = "[${event.newPosition.line}:${event.newPosition.column}:$offset]"
            }
        }

        /**
         * Usually not invoked directly, see class javadoc.
         */
        override fun dispose() {}

        override fun ID(): String = ID

        override fun install(statusBar: StatusBar)
        {
            text = s_default_text
        }

        override fun getComponent(): JComponent = this

        fun editor_focus_lost(editor: Editor)
        {
            (editor as EditorImpl).caretModel.removeCaretListener(m_caret_listener)
            text = s_default_text
        }

        fun editor_focus_gained(editor: Editor)
        {
            (editor as EditorImpl).caretModel.addCaretListener(m_caret_listener)
            val offset = editor.caretModel.offset
            val position = editor.caretModel.logicalPosition
            text = "[${position.line}:${position.column}:$offset]"
        }
    }
}
