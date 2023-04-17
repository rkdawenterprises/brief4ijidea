/*
 * Copyright (c) 2022 RKDAW Enterprises and Ralph Williamson
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

package net.ddns.rkdawenterprises.brief4ijidea.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun RemoteRobot.idea(function: IdeaFrame.() -> Unit) {
    find<IdeaFrame>(timeout = Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Idea frame")
@DefaultXpath("IdeFrameImpl type", "//div[@class='IdeFrameImpl']")
class IdeaFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {

    val projectViewTree
        get() = find<ContainerFixture>(byXpath("ProjectViewTree", "//div[@class='ProjectViewTree']"))

    val projectName
        get() = step("Get project name") { return@step callJs<String>("component.getProject().getName()") }

    val menuBar: JMenuBarFixture
        get() = step("Menu...") {
            return@step remoteRobot.find(JMenuBarFixture::class.java, JMenuBarFixture.byType())
        }

    @JvmOverloads
    fun dumbAware(timeout: Duration = Duration.ofMinutes(5), function: () -> Unit) {
        step("Wait for smart mode") {
            waitFor(duration = timeout, interval = Duration.ofSeconds(5)) {
                runCatching { isDumbMode().not() }.getOrDefault(false)
            }
            function()
            step("..wait for smart mode again") {
                waitFor(duration = timeout, interval = Duration.ofSeconds(5)) {
                    isDumbMode().not()
                }
            }
        }
    }

    fun isDumbMode(): Boolean {
        return callJs("com.intellij.openapi. project.DumbService.isDumb(component.project);", true)
    }
}