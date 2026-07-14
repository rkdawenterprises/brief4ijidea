/*
 * Copyright (c) 2019-2026 RKDAW Enterprises and Ralph Williamson
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
               "RedundantSemicolon",
               "PrivatePropertyName",
               "LocalVariableName",
               "PropertyName",
               "PackageName")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ddns.rkdawenterprises.brief4ijidea.Localized_messages.message
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

const val STATUS_BAR_MESSAGES_ID: String = "Brief4ijidea_status_bar_messages_factory_ID"
val STATUS_BAR_MESSAGES_DISPLAY_NAME: String = message("brief.emulator.document.information")

val MESSAGE_PERSISTENT_DURATION = 5.minutes;
val MESSAGE_TEMPORARY_DURATION = 10.seconds;

interface Status_bar_messages_listener: EventListener
{
    companion object
    {
        val TOPIC: Topic<Status_bar_messages_listener> =
            Topic(Status_bar_messages_listener::class.java)
    }

    fun message_persistent(text: String)
    fun message_temporary(text: String)
    fun message_clear()
}

class Status_bar_messages_factory: StatusBarWidgetFactory
{
    val m_widgets: MutableList<Status_bar_messages> = ArrayList<Status_bar_messages>();

    fun get_widget(project: Project?): Status_bar_messages?
    {
        synchronized(m_widgets) {
            if(m_widgets.isEmpty())
            {
                return null;
            }
            for(widget in m_widgets)
            {
                if(widget.m_project === project)
                {
                    return widget;
                }
            }
        }

        return null;
    }

    /**
     * @return Widget identifier. Used to store visibility settings.
     */
    @NonNls
    override fun getId(): @NonNls String
    {
        return STATUS_BAR_MESSAGES_ID;
    }

    /**
     * @return Widget's display name. Used to refer a widget in UI,
     * e.g. for "Enable/disable &lt;display name>" action names
     * or for checkbox texts in settings.
     */
    @Nls
    override fun getDisplayName(): @Nls String
    {
        return STATUS_BAR_MESSAGES_DISPLAY_NAME
    }

    /**
     * Returns availability of widget.
     *
     *
     * `False` means that IDE won't try to create a widget or will dispose it on [StatusBarWidgetsManager.updateWidget] call.
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
     * @param project The current project.
     */
    override fun isAvailable(project: Project): Boolean
    {
        return State_component.get_instance().get_show_status_bar_messages()
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
     *  * its visibility has changed. See StatusBarWidgetSettings.
     *
     *
     *
     * To do this, you need to explicitly invoke [StatusBarWidgetsManager.updateWidget]
     * to recreate the widget and re-add it to the status bar.
     *
     * @param project The current project.
     */
    override fun createWidget(project: Project): StatusBarWidget
    {
        val status_bar_messages =
            Status_bar_messages(project,
                                State_component.get_instance());

        synchronized(m_widgets) {
            m_widgets.add(status_bar_messages);
        }

        return status_bar_messages;
    }

    /**
     * @param widget The widget to dispose of.
     */
    override fun disposeWidget(widget: StatusBarWidget)
    {
        synchronized(m_widgets) {
            m_widgets.remove(widget as Status_bar_messages);
        }
    }

    /**
     * @param statusBar The given status bar widget.
     *
     * @return Returns whether the widget can be enabled on the given status bar right now.
     * Status bar's context menu with enable/disable action depends on the result of this method.
     *
     *
     * It's better to have this method aligned with [EditorBasedStatusBarPopup.WidgetState.HIDDEN],
     * whenever state is `HIDDEN`, this method should return `false`.
     * Otherwise, enabling widget via context menu will not have any visual effect.
     *
     *
     * E.g. [EditorBasedWidget] are available if editor is opened in a frame that given status bar is attached to
     *
     *
     * For creating editor based widgets see also [StatusBarEditorBasedWidgetFactory]
     */
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean
    {
        return false;
    }

    /**
     * @return `true` if the widget should be created by default.
     * Otherwise, the user must enable it explicitly via status bar context menu or settings.
     */
    override fun isEnabledByDefault(): Boolean
    {
        return true;
    }

    /**
     * @return Returns whether the user should be able to enable or disable the widget.
     *
     *
     * Some widgets are controlled by application-level settings (e.g., Memory indicator)
     * or cannot be disabled (e.g., Write thread indicator) and thus shouldn't be configurable via status bar context menu or settings.
     */
    override fun isConfigurable(): Boolean
    {
        return true;
    }
}

class Status_bar_messages(val m_project: Project,
                          disposable: Disposable): StatusBarWidget, TextPresentation, Disposable
{
    var m_message = message("brief.emulator.messages.will.appear.here");
    val m_connection: MessageBusConnection

    init
    {
        Disposer.register(disposable,
                          this);

        m_connection = m_project.getMessageBus().connect(disposable);
        m_connection.subscribe(Status_bar_messages_listener.TOPIC,
                               object: Status_bar_messages_listener
                                          {
                                              override fun message_persistent(text: String)
                                              {
                                                  m_message = text;
                                                  status_bar_messages_update_widget();

                                                  m_project.service<Status_bar_messages_timer>().schedule_timer(MESSAGE_PERSISTENT_DURATION)
                                                  {
                                                      message_clear();
                                                  }
                                              }

                                              override fun message_temporary(text: String)
                                              {
                                                  m_message = text;
                                                  status_bar_messages_update_widget();

                                                  m_project.service<Status_bar_messages_timer>().schedule_timer()
                                                  {
                                                      message_clear();
                                                  }
                                              }

                                              override fun message_clear()
                                              {
                                                  if(m_message.isNotEmpty())
                                                  {
                                                      m_project.service<Status_bar_messages_timer>().cancel_timer();
                                                      m_message = "";
                                                      status_bar_messages_update_widget();
                                                  }
                                              }
                                          });
    }

    /**
     * @return The ID of this widget.
     */
    override fun ID(): String
    {
        return STATUS_BAR_MESSAGES_ID;
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation
    {
        return this;
    }

    override fun getTooltipText(): String
    {
        return STATUS_BAR_MESSAGES_DISPLAY_NAME;
    }

    override fun getText(): String
    {
        return m_message;
    }

    override fun getAlignment(): Float
    {
        return Component.CENTER_ALIGNMENT;
    }

    /**
     * Usually not invoked directly, see class javadoc.
     */
    override fun dispose()
    {
        m_connection.dispose();
    }
}

@Service(Service.Level.PROJECT)
class Status_bar_messages_timer(private val project: Project,
                    private val coroutine_scope: CoroutineScope)
{
    var running_job: Job? = null;

    fun schedule_timer(duration: Duration = MESSAGE_TEMPORARY_DURATION,
                       action: () -> Unit)
    {
        running_job?.cancel();
        running_job = coroutine_scope.launch()
        {
            delay(duration);
            action();
        }
    }

    fun cancel_timer()
    {
        running_job?.cancel();
    }
}

enum class STATUS_BAR_MESSAGES_MESSAGE_TYPE
{
    PERSISTENT, TEMPORARY, CLEAR;
}

/**
 * Publish a persistent message for the status bar. This will be sent to each project's widget.
 * The message must be cleared later [status_bar_messages_message_clear] otherwise it will stay on
 * the task bar for a long duration [MESSAGE_PERSISTENT_DURATION].
 *
 * @param text The message to be published.
 */
fun status_bar_messages_message_persistent(text: String)
{
    status_bar_messages_message(STATUS_BAR_MESSAGES_MESSAGE_TYPE.PERSISTENT,
                                text)
}

/**
 * Publish a temporary message for the status bar. This will be sent to each project's widget.
 * Some time later [MESSAGE_TEMPORARY_DURATION], the widget will clear the message.
 *
 * @param text The message to be published.
 */
fun status_bar_messages_message_temporary(text: String)
{
    status_bar_messages_message(STATUS_BAR_MESSAGES_MESSAGE_TYPE.TEMPORARY,
                                text)
}

fun status_bar_messages_message_clear()
{
    status_bar_messages_message(STATUS_BAR_MESSAGES_MESSAGE_TYPE.CLEAR,
                                "")
}

fun status_bar_messages_message(type: STATUS_BAR_MESSAGES_MESSAGE_TYPE,
                                text: String)
{
    val project_manager = ProjectManager.getInstanceIfCreated() ?: return

    for(project in project_manager.getOpenProjects())
    {
        val message_bus = project.getMessageBus()
        val publisher =
            message_bus.syncPublisher<Status_bar_messages_listener>(Status_bar_messages_listener.TOPIC)
        if(type == STATUS_BAR_MESSAGES_MESSAGE_TYPE.PERSISTENT)
        {
            publisher.message_persistent(Localized_messages["status.bar.text.brief", text])
        }
        else if(type == STATUS_BAR_MESSAGES_MESSAGE_TYPE.TEMPORARY)
        {
            publisher.message_temporary(Localized_messages["status.bar.text.brief", text])
        }
        else if(type == STATUS_BAR_MESSAGES_MESSAGE_TYPE.CLEAR)
        {
            publisher.message_clear()
        }
    }
}

@Suppress("IncorrectServiceRetrieving")
fun status_bar_messages_update_widget()
{
    val project_manager = ProjectManager.getInstanceIfCreated() ?: return

    val status_bar_widget_factory: Status_bar_messages_factory =
        StatusBarWidgetFactory.EP_NAME.findExtension<Status_bar_messages_factory?>(Status_bar_messages_factory::class.java)
            ?: return

    for(project in project_manager.getOpenProjects())
    {
        val status_bar_widgets_manager =
            project.getService(StatusBarWidgetsManager::class.java)
        if(status_bar_widgets_manager != null)
        {
            status_bar_widgets_manager.updateWidget(status_bar_widget_factory)
        }

        val status_bar = WindowManager.getInstance()
            .getStatusBar(project)
        if(status_bar != null)
        {
            status_bar.updateWidget(STATUS_BAR_MESSAGES_ID)
        }
    }
}
