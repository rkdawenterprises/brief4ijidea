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
               "LocalVariableName",
               "HardCodedStringLiteral",
               "RedundantSemicolon",
               "unused",
               "SpellCheckingInspection")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import net.ddns.rkdawenterprises.brief4ijidea.pages.IdeaFrame
import java.awt.event.KeyEvent
import java.time.Duration

fun IdeaFrame.test_quick_java_doc_command()
{
    val text_editor_fixture = textEditor()
    val editor_fixture = text_editor_fixture.editor

    step("Command: Quick Java Doc. Description: Show contextual documentation popup.")
    {
        editor_fixture.scroll_to_line(42);
        waitFor { editor_fixture.hasText("SuppressWarnings") }
        editor_fixture.findText("SuppressWarnings")
            .click()
        editor_fixture.keyboard {
            hotKey(KeyEvent.VK_CONTROL,
                   KeyEvent.VK_H)
        }

        waitFor { heavyWeightWindows().size == 1 }
        val all_text = heavyWeightWindows()[0].find(CommonContainerFixture::class.java,
                                                    byXpath("//div[@class='JEditorPane']"),
                                                    Duration.ofSeconds(5))
            .findAllText()
        var found_it = 0
        for(i in all_text.indices)
        {
            if((found_it == 0) && all_text[i].text.contains("public interface")) found_it++;
            if((found_it == 1) && all_text[i].text.contains("SuppressWarnings")) found_it++;
        }

        assert(found_it == 2)
    }
}
