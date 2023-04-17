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
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

fun RemoteRobot.actionMenu(text: String): ActionMenuFixture
{
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenu' and @text='$text']")
    waitFor {
        findAll<ActionMenuFixture>(xpath).isNotEmpty()
    }
    return findAll<ActionMenuFixture>(xpath).first()
}

fun RemoteRobot.actionMenuItem(text: String): ActionMenuItemFixture
{
    val xpath = byXpath("text '$text'", "//div[@class='ActionMenuItem' and @text='$text']")
    waitFor {
        findAll<ActionMenuItemFixture>(xpath).isNotEmpty()
    }
    return findAll<ActionMenuItemFixture>(xpath).first()
}

@FixtureName("ActionMenu")
class ActionMenuFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)

@FixtureName("ActionMenuItem")
class ActionMenuItemFixture(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)