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
               "RedundantSemicolon",
               "LocalVariableName",
               "HardCodedStringLiteral",
               "SpellCheckingInspection")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.keyboard
import com.intellij.remoterobot.utils.waitFor
import net.ddns.rkdawenterprises.brief4ijidea.Column_marking_component.Column_mode_block_data
import net.ddns.rkdawenterprises.brief4ijidea.Column_marking_component.Column_mode_block_data.deserialize_from_JSON
import net.ddns.rkdawenterprises.brief4ijidea.pages.IdeaFrame
import net.ddns.rkdawenterprises.brief4ijidea.pages.actionMenu
import net.ddns.rkdawenterprises.brief4ijidea.pages.actionMenuItem
import net.ddns.rkdawenterprises.brief4ijidea.pages.dialog
import net.ddns.rkdawenterprises.brief4ijidea.pages.idea
import net.ddns.rkdawenterprises.brief4ijidea.pages.welcomeFrame
import org.apache.commons.lang.StringUtils
import org.assertj.swing.core.MouseButton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.awt.event.KeyEvent.*
import java.time.Duration

@ExtendWith(Remote_robot_client::class)
class Main_test
{
    @Test
    fun main_test(remote_robot: RemoteRobot) = with(remote_robot)
    {
        // TODO: If developing/debugging tests and IDE is open and project/file are set up, then comment this line out to speed iterations.
        ide_setup()

        idea {
            test_commands()
        }

        println("Finished tests...")
    }

    private fun RemoteRobot.ide_setup()
    {
        project_setup()

        close_tip_of_the_day(this)
        close_all_tabs(this)

        idea {
            file_setup()
        }
    }

    private fun RemoteRobot.project_setup()
    {
        welcomeFrame {
            create_new_project_link.click()

            dialog("New Project") {
                findText("Java").click()
                button("Create").click()
            }
        }
    }

    private fun IdeaFrame.file_setup()
    {
        waitFor(Duration.ofMinutes(5)) { isDumbMode().not() }

        step("Create the test java file") {
            with(projectViewTree) {
                if(hasText("src").not())
                {
                    findText(projectName).doubleClick()
                    waitFor { hasText("src") }
                }
                findText("src").click(MouseButton.RIGHT_BUTTON)
            }
            remoteRobot.actionMenu("New")
                .click()
            remoteRobot.actionMenuItem("Java Class")
                .click()
            keyboard { enterText("Test"); enter() }
        }

        with(textEditor()) {
            step("Populate the test java file") {
                waitFor { editor.hasText("Test") }
                editor.findText("Test")
                    .click()

                keyboard {
                    key(VK_END)
                    enter()
                }

                keyboard {
                    hotKey(VK_CONTROL,
                           VK_A)
                }

                keyboard { key(VK_DELETE) }

                editor.insertTextAtLine(0,
                                        0,
                                        net.ddns.rkdawenterprises.brief4ijidea.Test_data.java_example.trimIndent()
                                            .escape())
            }
        }
    }

    private fun IdeaFrame.test_commands()
    {
        step("Test the commands") {
            // TODO: Prompt user to close browser.
            test_help_menu_command()
            test_quick_java_doc_command()
            test_undo_redo_commands()
            test_change_output_file_command()
            test_beginning_of_line_command()
            test_end_of_line_command()
            test_top_of_buffer_command()
            test_end_of_buffer_command()
            test_top_of_window_command()
            test_end_of_window_command()
            test_left_side_of_window_command()
            test_right_side_of_window_command()
            test_scroll_buffer_down_in_window_command()
            test_go_to_line_command()
            test_delete_line_command()
            test_delete_next_word_command()
            test_delete_previous_word_command()
            test_delete_to_beginning_of_line_command()
            test_delete_to_end_of_line_command()
            test_insert_mode_toggle_command()
            test_open_line_command()
            test_mark_command()
            test_line_mark_command()
            test_column_mark_command()
            test_drop_bookmark_10_command()
            test_paste_from_history_command()
            test_line_to_top_of_window_command()
            test_center_line_in_window_command()
            test_line_to_bottom_of_window_command()
            test_search_forward_command()
            test_translate_forward_command()
        }
    }

    private fun IdeaFrame.test_translate_forward_command()
    {
        step("Command: Search forward. Description: Searches forward from the current position to the end of the current buffer for the given pattern.")
        {
            keyboard {
                hotKey(VK_ALT,
                       VK_T);
            }

            val search_replace_component = find(CommonContainerFixture:: class.java, byXpath("//div[@class='SearchReplaceComponent']"), Duration.ofSeconds(5));
            search_replace_component.button(byXpath("//div[@text='Replace']"), Duration.ofSeconds(5)).click();
            search_replace_component.button(byXpath("//div[@defaulticon='close.svg']"), Duration.ofSeconds(5)).click();
        }
    }

    private fun IdeaFrame.test_search_forward_command()
    {
        step("Command: Search forward. Description: Searches forward from the current position to the end of the current buffer for the given pattern.")
        {
            keyboard {
                hotKey(VK_ALT,
                       VK_S);
            }

            val search_replace_component = find(CommonContainerFixture:: class.java, byXpath("//div[@class='SearchReplaceComponent']"), Duration.ofSeconds(5));
            search_replace_component.button(byXpath("//div[@defaulticon='close.svg']"), Duration.ofSeconds(5)).click();
        }
    }

    private fun IdeaFrame.test_line_to_bottom_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Line to bottom of window. Description: Scrolls the buffer, moving the current line to the bottom of the window.")
        {
            val line_number = 110;
            editor_fixture.move_to_line(line_number);
            var bottom = editor_fixture.get_visible_area_bottom_offset_line();
            assert((bottom[1] != line_number) && (bottom[3] != line_number))

            // Have to issue the command twice. Not sure why. It works fine manually.
            keyboard {
                hotKey(VK_CONTROL,
                       VK_B);
                hotKey(VK_CONTROL,
                       VK_B);
            }

            bottom = editor_fixture.get_visible_area_bottom_offset_line();
            assert((bottom[1] == line_number) || (bottom[3] == line_number))
        }
    }

    private fun IdeaFrame.test_center_line_in_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Center line in window. Description: Scrolls the buffer, moving the current line to the center of the current window.")
        {
            val line_number = 110;
            editor_fixture.move_to_line(line_number);
            var bottom = editor_fixture.get_visible_area_bottom_offset_line()[1];
            var top = editor_fixture.get_visible_area_top_offset_line()[1];
            var center = top + ((bottom - top) / 2);
            assert(center != line_number);

            // Have to issue the command twice. Not sure why. It works fine manually.
            keyboard {
                hotKey(VK_CONTROL,
                       VK_C);
                hotKey(VK_CONTROL,
                       VK_C);
            }

            bottom = editor_fixture.get_visible_area_bottom_offset_line()[1];
            top = editor_fixture.get_visible_area_top_offset_line()[1];
            center = top + ((bottom - top) / 2);
            assert(kotlin.math.abs(line_number - center) <= 1)
        }
    }

    private fun IdeaFrame.test_line_to_top_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Line to top of window. Description: Scrolls the buffer, moving the current line to the top of the window.")
        {
            val line_number = 110;
            editor_fixture.move_to_line(line_number);
            var top = editor_fixture.get_visible_area_top_offset_line();
            assert((top[1] != line_number) && (top[3] != line_number))

            keyboard {
                hotKey(VK_CONTROL,
                       VK_T);
            }

            top = editor_fixture.get_visible_area_top_offset_line();
            assert((top[1] == line_number) || (top[3] == line_number))
        }
    }

    private fun IdeaFrame.test_paste_from_history_command()
    {
        step("Command: Paste from history. Description: Opens a dialog to paste an item from scrap history.")
        {
            keyboard {
                hotKey(VK_SHIFT,
                       VK_INSERT);
            }

            dialog("Choose Content to Paste")
            {
                button("Cancel").click();
            }
        }
    }

    /**
     * Also tests "jump to bookmark" as well as the bookmark dialog command.
     */
    private fun IdeaFrame.test_drop_bookmark_10_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Drop bookmark 10. Description: Drops a numbered bookmark at the current position.")
        {
            val line_number = 110;
            val alternate_line_number = 256;

            editor_fixture.move_to_line(line_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_0);
            }

            editor_fixture.move_to_line(alternate_line_number);

            var current_position = editor_fixture.get_caret_logical_position();

            assert(current_position.line == alternate_line_number)

            keyboard {
                hotKey(VK_ALT,
                       VK_J);
                key(VK_0)
            }

            current_position = editor_fixture.get_caret_logical_position();

            assert(current_position.line == line_number)

            keyboard {
                hotKey(VK_ALT,
                       VK_B);
            }

            waitFor { heavyWeightWindows().size == 1 }
            val heavy_weight_window = heavyWeightWindows()[0];
            heavy_weight_window.findText("Bookmarks");
//            val tree_fixture = heavy_weight_window.find<JTreeFixture>(JTreeFixture::class.java,
//                                                                      byXpath("//div[@class='DnDAwareTree']"),
//                                                                      Duration.ofSeconds(5));
//            val items = tree_fixture.collectExpandedPaths();
//            var found_it = false;
//            for(i in items.indices)
//            {
//                println(items[i].row)
//                for(j in items[i].path) println(j)
//                if(items[i].contains("Test.java:") && items[i].contains((line_number + 1).toString()))
//                {
//                    found_it = true;
//                    tree_fixture.clickRow(i);
//                }
//            }
//            assert(found_it)

//            button(byXpath("//div[@myicon='remove.svg']")).click()

            keyboard { key(VK_ESCAPE) }
        }
    }

    /**
     * Also tests the copy/paste command with column marked text.
     */
    private fun IdeaFrame.test_column_mark_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Column mark. Description: Starts marking a rectangular block.")
        {
            val line_number = 70;
            val column_number = 15;
            val number_of_lines = 5;
            val number_of_columns = 4;
            val text_of_block = editor_fixture.get_block(line_number,
                                                         column_number,
                                                         number_of_lines,
                                                         number_of_columns);

            editor_fixture.move_to_line(line_number,
                                        column_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_C);
            }

            virtual_space_response();

            editor_fixture.move_to_line(line_number,
                                        column_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_C);
                for(i in 1 until number_of_columns) key(VK_RIGHT);
                for(i in 1 until number_of_lines) key(VK_DOWN);
            }

            keyboard {
                key(VK_ADD)
            }

            val clipboard = editor_fixture.get_clipboard_text();
            val block_data: Column_mode_block_data = deserialize_from_JSON(clipboard);

            assert(block_data.rows.size == number_of_lines)
            assert(block_data.width == number_of_columns)
            for(i in block_data.rows.indices)
            {
                assert(block_data.rows[i] == text_of_block[i])
            }

            // Attempt to duplicate column paste behavior.
            val test_text_of_lines_after_paste = StringBuilder(editor_fixture.get_line(line_number - 1));
            val test_text_of_lines = editor_fixture.get_lines(line_number,
                                                              number_of_lines)
                .split('\n');
            for(i in test_text_of_lines.indices)
            {
                if(i < number_of_lines)
                {
                    val modified_line = StringBuilder(test_text_of_lines[i]);

                    // Fill empty or smaller rows.
                    if(test_text_of_lines[i].length < column_number)
                    {
                        modified_line.append(StringUtils.repeat(" ",
                                                                column_number - test_text_of_lines[i].length));
                    }

                    modified_line.insert(column_number,
                                         block_data.rows[i]);
                    test_text_of_lines_after_paste.append(modified_line.append(System.getProperty("line.separator")));
                }
            }
            test_text_of_lines_after_paste.append(editor_fixture.get_line(line_number + number_of_lines));

            editor_fixture.move_to_line(line_number,
                                        column_number);

            keyboard {
                key(VK_INSERT)
            }

            val text_of_lines_after_paste = editor_fixture.get_lines(line_number - 1,
                                                                     number_of_lines + 2);

            assert(test_text_of_lines_after_paste.toString() == text_of_lines_after_paste)

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }

            virtual_space_disable();
        }
    }

    /**
     * Also tests the copy/paste command with line marked text.
     */
    private fun IdeaFrame.test_line_mark_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Line mark. Description: Starts marking a line at a time.")
        {
            val line_number = 70;
            val number_of_lines = 4;
            val text_of_lines = editor_fixture.get_lines(line_number,
                                                         number_of_lines);

            editor_fixture.move_to_line(line_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_L);
                for(i in 1 until number_of_lines) key(VK_DOWN);
            }

            val selected = editor_fixture.selectedText;

            assert(text_of_lines == selected);

            keyboard {
                key(VK_ADD)
            }

            assert(text_of_lines == editor_fixture.get_clipboard_text());

            val test_text_of_lines_after_paste = editor_fixture.get_line(line_number - 1) +
                    text_of_lines + text_of_lines +
                    editor_fixture.get_line(line_number + number_of_lines);

            editor_fixture.move_to_line(line_number);

            keyboard {
                key(VK_INSERT)
            }

            val text_of_lines_after_paste = editor_fixture.get_lines(line_number - 1,
                                                                     (2 * number_of_lines) + 2);

            assert(test_text_of_lines_after_paste == text_of_lines_after_paste)

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    /**
     * Also tests the copy/paste command with marked text.
     */
    private fun IdeaFrame.test_mark_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Mark. Description: Starts normal marking mode.")
        {
            val line_number = 43;
            val number_of_columns = 4;
            val text_of_line = editor_fixture.get_line(line_number);
            val column_number = text_of_line.length / 4;

            editor_fixture.move_to_line(line_number,
                                        column_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_M);
                for(i in 1 until number_of_columns) key(VK_RIGHT);
            }

            val test_text = text_of_line.substring(column_number,
                                                   column_number + number_of_columns);
            val selected = editor_fixture.selectedText;

            assert(test_text == selected);

            keyboard {
                key(VK_ADD)
            }

            assert(test_text == editor_fixture.get_clipboard_text());

            val test_line_after_paste = StringBuilder(text_of_line).insert(column_number + number_of_columns,
                                                                           test_text);

            editor_fixture.move_to_line(line_number,
                                        column_number);
            keyboard {
                key(VK_INSERT)
            }

            val text_of_line_after_paste = editor_fixture.get_line(line_number);

            assert(test_line_after_paste.toString() == text_of_line_after_paste);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_open_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Open line. Description: Inserts a blank line after the current line and places the cursor on the first column of this new line.")
        {
            val line_number = 43;
            val text_of_line = editor_fixture.get_line(line_number);
            val text_of_second_line = editor_fixture.get_line(line_number + 1);
            val column_number = text_of_line.length / 2;
            editor_fixture.move_to_line(line_number,
                                        column_number)

            assert(text_of_second_line.length > 2)

            keyboard {
                hotKey(VK_CONTROL,
                       VK_ENTER);
            }

            val text_of_line_again = editor_fixture.get_line(line_number);
            val text_of_second_line_again = editor_fixture.get_line(line_number + 1);
            val text_of_third_line_again = editor_fixture.get_line(line_number + 2);

            assert(text_of_line == text_of_line_again);
            assert(text_of_second_line != text_of_second_line_again)
            assert(text_of_second_line == text_of_third_line_again)

            val regex = "^\\s+\$".toRegex();
            assert(regex.matches(text_of_second_line_again))

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_insert_mode_toggle_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Insert mode toggle. Description: Switches between insert mode and overstrike mode.")
        {
            val line_number = 44;
            var text_of_line = editor_fixture.get_line(line_number);
            val column_number = text_of_line.length / 2;
            editor_fixture.move_to_line(line_number,
                                        column_number)

            val test_text = "RalphKW"

            keyboard {
                hotKey(VK_ALT,
                       VK_I);
                enterText(test_text)
            }

            val line_modified = StringBuilder(text_of_line.removeRange(column_number,
                                                                       column_number + test_text.length))
                .insert(column_number,
                        test_text);

            text_of_line = editor_fixture.get_line(line_number);

            assert(line_modified.toString() == text_of_line);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_I);
            }
        }
    }

    private fun IdeaFrame.test_delete_to_end_of_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Delete to end of line. Description: Deletes all characters from the current position to the end of the line.")
        {
            val line_number = 44;
            var text_of_line = editor_fixture.get_line(line_number);
            val column_number = text_of_line.length / 2;
            editor_fixture.move_to_line(line_number,
                                        column_number)

            keyboard {
                hotKey(VK_ALT,
                       VK_K);
            }

            val line_modified = text_of_line.removeRange(column_number,
                                                         (text_of_line.length - 1));
            text_of_line = editor_fixture.get_line(line_number);

            assert(line_modified == text_of_line);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_delete_to_beginning_of_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Delete to beginning of line. Description: Deletes all characters before the cursor to the beginning of the line.")
        {
            val line_number = 44;
            var text_of_line = editor_fixture.get_line(line_number);
            val column_number = text_of_line.length / 2;
            editor_fixture.move_to_line(line_number,
                                        column_number)

            keyboard {
                hotKey(VK_CONTROL,
                       VK_K);
            }

            val line_modified = text_of_line.removeRange(0,
                                                         column_number);
            text_of_line = editor_fixture.get_line(line_number);

            assert(line_modified == text_of_line);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_delete_previous_word_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Delete previous word. Description: Deletes from the cursor position to the beginning of the previous word.")
        {
            val line_number = 44;
            var text_of_line = editor_fixture.get_line(line_number);
            editor_fixture.clickOnOffset(editor_fixture.get_end_offset(line_number));
            Thread.sleep(500);

            val range_to_delete = editor_fixture.get_delete_to_word_boundry_range(false);
            val line_modified = text_of_line.removeRange(range_to_delete[0].column,
                                                         range_to_delete[1].column);

            keyboard {
                hotKey(VK_CONTROL,
                       VK_BACK_SPACE);
            }

            text_of_line = editor_fixture.get_line(line_number);
            assert(line_modified == text_of_line);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_delete_next_word_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Delete next word. Description: Deletes from the cursor position to the start of the next word.")
        {
            val line_number = 306;
            var text_of_line = editor_fixture.get_line(line_number);
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val range_to_delete = editor_fixture.get_delete_to_word_boundry_range(true);
            val line_modified = text_of_line.removeRange(range_to_delete[0].column,
                                                         range_to_delete[1].column);

            keyboard {
                hotKey(VK_ALT,
                       VK_BACK_SPACE)
            }

            text_of_line = editor_fixture.get_line(line_number);
            assert(line_modified == text_of_line);

            keyboard {
                hotKey(VK_ALT,
                       VK_U);
                hotKey(VK_ALT,
                       VK_U);
            }
        }
    }

    private fun IdeaFrame.test_delete_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Delete line. Description: Deletes the entire current line.")
        {
            val line_number = 306;
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val text_of_line_previous = editor_fixture.get_line(line_number - 1);
            var text_of_line = editor_fixture.get_line(line_number);
            val text_of_line_next = editor_fixture.get_line(line_number + 1);

            assert((text_of_line != text_of_line_previous) && (text_of_line != text_of_line_next))

            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            keyboard {
                hotKey(VK_ALT,
                       VK_D)
            }

            text_of_line = editor_fixture.get_line(line_number);

            assert((text_of_line != text_of_line_previous) && (text_of_line == text_of_line_next))

            keyboard {
                hotKey(VK_ALT,
                       VK_U)
                hotKey(VK_ALT,
                       VK_U)
            }
        }
    }

    private fun IdeaFrame.test_go_to_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Go to line. Description: Moves the cursor to the specified line number.")
        {
            val line_number = 178;
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val current_line = editor_fixture.get_caret_logical_position().line;
            assert(current_line == line_number);

            keyboard {
                hotKey(VK_ALT,
                       VK_G)
            }

            dialog("Go to Line:Column")
            {
                button("Cancel").click()
            }
        }
    }

    private fun IdeaFrame.test_scroll_buffer_up_in_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Scroll buffer up in window. Description: Moves the buffer, if possible, up one line in the window, keeping the cursor on the same line.")
        {
            val line_number = 178;
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val starting_offset = editor_fixture.caretOffset
            val starting_window_end_line = editor_fixture.get_visible_area_bottom_offset_line()[1]
            val starting_window_beginning_line = editor_fixture.get_visible_area_top_offset_line()[1]

            keyboard {
                hotKey(VK_CONTROL,
                       VK_E)
            }

            val final_offset = editor_fixture.caretOffset
            val final_window_end_line = editor_fixture.get_visible_area_bottom_offset_line()[1]
            val final_window_beginning_line = editor_fixture.get_visible_area_top_offset_line()[1]

            assert(starting_offset == final_offset)
            assert((starting_window_end_line - 1) == final_window_end_line)
            assert((starting_window_beginning_line - 1) == final_window_beginning_line)
        }
    }

    private fun IdeaFrame.test_scroll_buffer_down_in_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Scroll buffer down in window. Description: Moves the buffer, if possible, down one line in the window, keeping the cursor on the same line.")
        {
            val line_number = 178;
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val starting_offset = editor_fixture.caretOffset
            val starting_window_end_line = editor_fixture.get_visible_area_bottom_offset_line()[1]
            val starting_window_beginning_line = editor_fixture.get_visible_area_top_offset_line()[1]

            keyboard {
                hotKey(VK_CONTROL,
                       VK_D)
            }

            val final_offset = editor_fixture.caretOffset
            val final_window_end_line = editor_fixture.get_visible_area_bottom_offset_line()[1]
            val final_window_beginning_line = editor_fixture.get_visible_area_top_offset_line()[1]

            assert(starting_offset == final_offset)
            assert((starting_window_end_line + 1) == final_window_end_line)
            assert((starting_window_beginning_line + 1) == final_window_beginning_line)
        }
    }

    private fun IdeaFrame.virtual_space_response()
    {
        dialog("Change Settings for this Command") {
            findText("Don't ask again").click()
            button("OK").click()
        }

        virtual_space_enable();
    }

    private fun IdeaFrame.virtual_space_enable()
    {
        click_on_status_icon_settings();

        dialog("Settings") {
            checkBox("Do not show virtual space setting dialog again.").unselect()
            tree_fixtures[0].click_path("Editor, General")
            checkBox("After the end of line").select()
            button("OK").click()
        }
    }

    private fun IdeaFrame.virtual_space_disable()
    {
        click_on_status_icon_settings();

        dialog("Settings") {
            tree_fixtures[0].click_path("Editor, General")
            checkBox("After the end of line").unselect()
            button("OK").click()
        }
    }

    private fun IdeaFrame.test_right_side_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Right side of window. Description: Moves the cursor to the right side of the window, regardless of the length of the line.")
        {
            val line_number = 144;
            editor_fixture.clickOnOffset(editor_fixture.get_start_offset(line_number));
            Thread.sleep(500)

            val start_visual_position = editor_fixture.get_caret_visual_position();
            val end_visual_position = editor_fixture.get_visible_area_right_visual_position_of_current_line();
            assert(start_visual_position.column != end_visual_position.column)

            keyboard {
                hotKey(VK_SHIFT,
                       VK_END)
            }

            virtual_space_response();

            keyboard {
                hotKey(VK_SHIFT,
                       VK_END)
            }

            val current_visual_position = editor_fixture.get_caret_visual_position();
            assert(current_visual_position.column == end_visual_position.column)

            virtual_space_disable()
        }
    }

    private fun IdeaFrame.test_left_side_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Left side of window. Description: Moves the cursor to the left side of the window.")
        {
            val line_number = 148;
            editor_fixture.clickOnOffset(editor_fixture.get_end_offset(line_number));
            Thread.sleep(500)
            val left_side_of_window_offset = editor_fixture.get_visible_area_left_offset_of_current_line();

            keyboard {
                hotKey(VK_SHIFT,
                       VK_HOME)
            }

            assert(editor_fixture.caretOffset == left_side_of_window_offset);
        }
    }

    private fun IdeaFrame.test_end_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: End of window. Description: Moves the cursor to the last line of the current window, retaining the column position.")
        {
            val column_number = 14
            editor_fixture.move_to_line(18,
                                        column_number)
            val starting_offset = editor_fixture.caretOffset

            val window_end_offset_line = editor_fixture.get_visible_area_bottom_offset_line()
            val window_end_offset = window_end_offset_line[0]
            val window_end_line = window_end_offset_line[1]
            val window_end_offset_alternate = window_end_offset_line[2]
            val window_end_line_alternate = window_end_offset_line[3]

            assert((starting_offset != window_end_offset) && (starting_offset != window_end_offset_alternate))

            val window_end_line_length = editor_fixture.get_line_length(window_end_line)
            val window_end_line_length_alternate = editor_fixture.get_line_length(window_end_line_alternate)
            val window_end_target_offset = window_end_offset - (window_end_line_length - column_number)
            val window_end_target_offset_alternate = window_end_offset_alternate - (window_end_line_length_alternate - column_number)

            keyboard {
                hotKey(VK_CONTROL,
                       VK_END)
            }
            val current_offset = editor_fixture.caretOffset
            val current_line = editor_fixture.get_current_line_number()

            assert(((current_offset == window_end_target_offset) &&
                    (current_line == window_end_line)) ||
                           ((current_offset == window_end_target_offset_alternate) &&
                                   (current_line == window_end_line_alternate)))
        }
    }

    private fun IdeaFrame.test_top_of_window_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Top of window. Description: Moves the cursor to the top line of the current window, retaining the column position.")
        {
            val column_number = 31
            editor_fixture.move_to_line(34,
                                        column_number)

            val starting_offset = editor_fixture.caretOffset

            val window_beginning_offset_line = editor_fixture.get_visible_area_top_offset_line()
            val window_beginning_offset = window_beginning_offset_line[0]
            val window_beginning_line = window_beginning_offset_line[1]
            val window_beginning_offset_alternate = window_beginning_offset_line[2]
            val window_beginning_line_alternate = window_beginning_offset_line[3]
            assert((starting_offset != window_beginning_offset) &&
                           (starting_offset != window_beginning_offset_alternate))

            keyboard {
                hotKey(VK_CONTROL,
                       VK_HOME)
            }
            val current_offset = editor_fixture.caretOffset
            val current_line = editor_fixture.get_current_line_number()
            assert(((current_offset == (window_beginning_offset + column_number)) &&
                    (current_line == window_beginning_line)) ||
                           ((current_offset == (window_beginning_offset_alternate + column_number)) &&
                                   (current_line == window_beginning_line_alternate)))
        }
    }

    private fun IdeaFrame.test_end_of_buffer_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: End of buffer. Description: Moves the cursor to the last character in the buffer.")
        {
            editor_fixture.move_to_line(159,
                                        68)
            val starting_offset = editor_fixture.caretOffset
            val end_offset = editor_fixture.get_end_offset()
            val end_line = editor_fixture.get_line_number(end_offset)
            assert(starting_offset != end_offset)

            keyboard {
                hotKey(VK_CONTROL,
                       VK_PAGE_DOWN)
            }
            val current_offset = editor_fixture.caretOffset
            val current_line = editor_fixture.get_current_line_number()
            assert((current_offset == end_offset) && (current_line == end_line))
        }
    }

    private fun IdeaFrame.test_top_of_buffer_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Top of buffer. Description: Moves the cursor to the first character of the buffer.")
        {
            editor_fixture.move_to_line(159,
                                        68)
            val starting_offset = editor_fixture.caretOffset
            assert(starting_offset != 0)

            keyboard {
                hotKey(VK_CONTROL,
                       VK_PAGE_UP)
            }
            val current_offset = editor_fixture.caretOffset
            val current_line = editor_fixture.get_current_line_number()
            assert((current_offset == 0) && (current_line == 0))
        }
    }

    private fun IdeaFrame.test_end_of_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: End of line. Description: Places the cursor at the last valid character of the current line, window, or file.")
        {
            val line_number = 159
            val column_number = 68
            editor_fixture.move_to_line(line_number,
                                        Column_target.END)
            val end_of_line_offset = editor_fixture.caretOffset
            editor_fixture.move_to_line(line_number,
                                        column_number)
            val window_end_offset_line = editor_fixture.get_visible_area_bottom_offset_line()
            val window_end_offset = window_end_offset_line[0]
            val window_end_line = window_end_offset_line[1]
            val window_end_offset_alternate = window_end_offset_line[2]
            val window_end_line_alternate = window_end_offset_line[3]

            val starting_offset = editor_fixture.caretOffset
            assert(starting_offset != end_of_line_offset)

            keyboard { key(VK_END) }
            var current_offset = editor_fixture.caretOffset
            var current_line = editor_fixture.get_current_line_number()
            assert((current_offset == end_of_line_offset) && (current_line == line_number))

            keyboard { key(VK_END) }
            current_offset = editor_fixture.caretOffset
            current_line = editor_fixture.get_current_line_number()
            assert(((current_offset == window_end_offset) && (current_line == window_end_line)) ||
                           (current_offset == window_end_offset_alternate) && ((current_line == window_end_line_alternate)))

            keyboard { key(VK_END) }
            current_offset = editor_fixture.caretOffset
            current_line = editor_fixture.get_current_line_number()
            val end_offset = editor_fixture.get_end_offset()
            val end_line = editor_fixture.get_line_number(end_offset)
            assert((current_line == end_line) && (current_offset == end_offset))
        }
    }

    private fun IdeaFrame.test_beginning_of_line_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Beginning of line. Description: Places the cursor at column 1 of the current line, window, or file.")
        {
            val line_number = 159
            val column_number = 68
            editor_fixture.move_to_line(line_number)
            val beginning_of_line_offset = editor_fixture.caretOffset
            editor_fixture.move_to_line(line_number,
                                        column_number);

            val window_beginning_offset_line = editor_fixture.get_visible_area_top_offset_line();
            val window_beginning_offset = window_beginning_offset_line[0]
            val window_beginning_line = window_beginning_offset_line[1]
            val window_beginning_offset_alternate = window_beginning_offset_line[2]
            val window_beginning_line_alternate = window_beginning_offset_line[3]

            val starting_offset = editor_fixture.caretOffset
            assert(starting_offset != beginning_of_line_offset)

            keyboard { key(VK_HOME) }
            var current_offset = editor_fixture.caretOffset
            var current_line = editor_fixture.get_current_line_number()
            assert((current_offset == beginning_of_line_offset) && (current_line == line_number))

            keyboard { key(VK_HOME) }
            current_offset = editor_fixture.caretOffset
            current_line = editor_fixture.get_current_line_number()
            assert(((current_line == window_beginning_line) && (current_offset == window_beginning_offset)) ||
                           ((current_line == window_beginning_line_alternate) && (current_offset == window_beginning_offset_alternate)))

            keyboard { key(VK_HOME) }
            current_offset = editor_fixture.caretOffset
            current_line = editor_fixture.get_current_line_number()
            assert((current_line == 0) && (current_offset == 0))
        }
    }

    private fun IdeaFrame.test_change_output_file_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Change output file. Description: Changes the output file name for the current buffer.")
        {
            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_O)
            }

            dialog("Rename",
                   Duration.ofSeconds(60)) {
                button("Cancel").click()
            }
        }
    }

    private fun IdeaFrame.test_undo_redo_commands()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor
        val line_number = 49
        var string_at_line_modified = ""

        step("Command: Undo. Description: Reverses the effect of any typing or commands that modified an open file.")
        {
            keyboard { key(VK_ESCAPE) }

            val string_at_line = editor_fixture.get_line(line_number)

            val index = 14
            val caret_offset = editor_fixture.caretOffset
            editor_fixture.clickOnOffset(caret_offset + index)
            waitFor { editor_fixture.caretOffset == (caret_offset + index) }
            keyboard { key(VK_DELETE) }
            keyboard { key(VK_DELETE) }
            string_at_line_modified = editor_fixture.get_line(line_number)
            assert(string_at_line_modified == string_at_line.removeRange(index,
                                                                         index + 2))

            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_U)
            }

            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_U)
            }

            val string_at_line_modified_undo = editor_fixture.get_line(line_number)
            assert(string_at_line == string_at_line_modified_undo)
        }

        step("Command: Redo. Description: Reverses the effect of commands that have been undone.")
        {
            editor_fixture.keyboard {
                hotKey(VK_CONTROL,
                       VK_U)
            }

            editor_fixture.keyboard {
                hotKey(VK_CONTROL,
                       VK_U)
            }

            val string_at_line_modified_redo = editor_fixture.get_line(line_number)
            assert(string_at_line_modified_redo == string_at_line_modified)

            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_U)
            }

            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_U)
            }
        }
    }

    private fun IdeaFrame.test_help_menu_command()
    {
        val text_editor_fixture = textEditor()
        val editor_fixture = text_editor_fixture.editor

        step("Command: Help. Description: Open general help menu.")
        {
            editor_fixture.keyboard {
                hotKey(VK_ALT,
                       VK_H)
            }

            Thread.sleep(5000)

            waitFor(Duration.ofSeconds(60))
            {
                editor_fixture.hasFocus
            }
        }
    }

    // TODO: If developing/debugging tests, then comment this line out to speed iterations.
    @AfterEach
    fun closeProject(remoteRobot: RemoteRobot) = with(remoteRobot) {
        idea {
            if(remoteRobot.isMac())
            {
                keyboard {
                    hotKey(VK_SHIFT,
                           VK_META,
                           VK_A);
                    enterText("Close Project");
                    enter();
                }
            }
            else
            {
                menuBar.select("File",
                               "Close Project");
            }
        }
    }
}
