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

@file:Suppress("ClassName",
               "PropertyName")

package net.ddns.rkdawenterprises.brief4ijidea.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.welcomeFrame(function: Welcome_frame.() -> Unit)
{
    find(Welcome_frame::class.java,
         Duration.ofSeconds(10)).apply(function)
}

@FixtureName("Welcome Frame")
@DefaultXpath("type",
              "//div[@class='FlatWelcomeFrame']")
class Welcome_frame(remote_robot: RemoteRobot,
                    remote_component: RemoteComponent) : CommonContainerFixture(remote_robot,
                                                                                remote_component)
{
    val create_new_project_link
        get() = actionLink(byXpath("New Project",
                                   "//div[(@class='MainButton' and @text='New Project') or (@accessiblename='New Project' and @class='JButton')]"))
}