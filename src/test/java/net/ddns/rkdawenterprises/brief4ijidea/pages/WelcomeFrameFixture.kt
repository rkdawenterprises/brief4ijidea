// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package net.ddns.rkdawenterprises.brief4ijidea.pages

import com.intellij.remoterobot.utils.hasAnyComponent
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture

@DefaultXpath(by = "FlatWelcomeFrame type",
              xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
class WelcomeFrameFixture(remoteRobot: RemoteRobot,
                          remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot,
                                                                                     remoteComponent)
{
    fun createNewProjectLink(): ComponentFixture
    {
        return welcomeFrameLink("New Project")
    }

    fun importProjectLink(): ComponentFixture
    {
        return welcomeFrameLink("Get from VCS")
    }

    private fun welcomeFrameLink(text: String): ComponentFixture
    {
        return if(this.hasAnyComponent(byXpath("//div[@class='NewRecentProjectPanel']")))
        {
            find(ComponentFixture::class.java,
                 byXpath("//div[@class='JBOptionButton' and @text='$text']"))
        }
        else find(
            ComponentFixture::class.java,
            byXpath("//div[@class='NonOpaquePanel'][./div[@text='$text']]//div[@class='JButton']"))
    }
}