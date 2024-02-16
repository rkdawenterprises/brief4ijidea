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

import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.VisualPosition
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.EditorFixture
import com.intellij.remoterobot.fixtures.JTreeFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.step
import com.intellij.remoterobot.utils.Keyboard
import com.intellij.remoterobot.utils.waitFor
import net.ddns.rkdawenterprises.brief4ijidea.pages.DialogFixture
import net.ddns.rkdawenterprises.brief4ijidea.pages.DialogFixture.Companion.byTitle
import net.ddns.rkdawenterprises.brief4ijidea.pages.IdeaFrame
import org.apache.commons.lang3.StringUtils
import java.awt.Point
import java.time.Duration

fun String.escape(): String = this.replace("\n",
                                           "\\n")

fun optional_step(step_name: String,
                  code: () -> Unit) = step(step_name)
{
    try
    {
        code()
    }
    catch(ignore: Throwable)
    {
        println("$step_name failure ignored...")
    }
}

enum class Column_target
{ START, END }

fun EditorFixture.scroll_to_line(line_number: Int)
{
    val script =
        """
            const editor = local.get('editor');
            const document = local.get('document');
            const offset = document.getLineStartOffset($line_number);
            editor.getScrollingModel().scrollTo(editor.offsetToLogicalPosition(offset), com.intellij.openapi.editor.ScrollType.CENTER);
        """.trimIndent();
    runJs(script,
          true)
    Thread.sleep(500)
}

fun EditorFixture.move_to_line(line_number: Int,
                               column_target: Column_target = Column_target.START)
{
    if(column_target == Column_target.START)
    {
        move_to_line(line_number,
                     0)
    }
    else if(column_target == Column_target.END)
    {
        move_to_line(line_number,
                     get_end_column(line_number))
    }
}

fun EditorFixture.move_to_line(line_number: Int,
                               column_number: Int)
{
    val script =
        """
            importClass(com.intellij.openapi.editor.ScrollType);
            const editor = component.getEditor();
            const document = editor.getDocument();
            const offset = document.getLineStartOffset($line_number) + $column_number;
            editor.getScrollingModel().scrollTo(editor.offsetToLogicalPosition(offset), ScrollType.CENTER);
            const visual_position = editor.offsetToVisualPosition(offset);
            editor.visualPositionToXY(visual_position);
        """.trimIndent()
    val point = callJs<Point>(script,
                              true)
    Thread.sleep(500)
    click(point)
}

/**
 * Retrieves the text of a given line number.
 * Note: The caret is moved to the requested line and that line is selected.
 *
 * @param line_number The requested line number to get.
 * @return The text of the requested line.
 */
fun EditorFixture.get_line(line_number: Int): String
{
    move_to_line(line_number)

    val script =
        """
            importPackage(com.intellij.openapi.command);
            
            const editor = local.get('editor');
            const project = editor.getProject();
                
            WriteCommandAction.runWriteCommandAction(project, new Runnable({
                run: function () {
                    editor.getSelectionModel().selectLineAtCaret();
                }
            }));
        """.trimIndent();

    runJs(script,
          true)
    Thread.sleep(500)

    return selectedText
}

/**
 * Gets the requested lines.
 * Note: the lines are selected.
 *
 * @param line_number
 * @param number_of_lines
 * @return A '\n' delimited string containing the requested lines.
 */
fun EditorFixture.get_lines(line_number: Int,
                            number_of_lines: Int): String
{
    move_to_line(line_number)

    val script =
        """
            importPackage(com.intellij.openapi.command);
                        
            const editor = component.getEditor();
            const document = editor.getDocument();
            const project = editor.getProject();
            
            const start_offset = document.getLineStartOffset($line_number);
            const end_offset = document.getLineStartOffset($line_number + $number_of_lines);
                
            WriteCommandAction.runWriteCommandAction(project, new Runnable({
                run: function () {
                    editor.getSelectionModel().setSelection(start_offset, end_offset);
                }
            }));
        """.trimIndent();

    runJs(script,
          true)
    Thread.sleep(500)

    return selectedText;
}

/**
 * Uses get_lines() to obtain the associated set of lines, then extracts the block from the set of lines.
 *
 * @param line_start First line of requested block. Zero based.
 * @param column_start First column of requested block. Zero based.
 * @param number_of_lines Vertical size of the block.
 * @param number_of_columns Horizontal size of the block.
 * @return
 */
fun EditorFixture.get_block(line_start: Int,
                            column_start: Int,
                            number_of_lines: Int,
                            number_of_columns: Int): Array<String?>
{
    val lines = get_lines(line_start,
                          number_of_lines).replace("\r",
                                                   "")
        .split('\n');

    var rows: Array<String?> = arrayOfNulls<String>(number_of_lines);

    for(i in lines.indices)
    {
        if(i < number_of_lines)
        {
            if(lines[i].length > column_start)
            {
                val end_index = if((column_start + number_of_columns) > lines[i].length)
                {
                    lines[i].length
                }
                else
                {
                    (column_start + number_of_columns)
                }

                rows[i] = lines[i].substring(column_start,
                                             end_index);
            }
        }
    }

    // Fill empty or smaller rows.
    for(i in rows.indices)
    {
        val length = rows[i]?.length ?: 0

        if(rows[i] == null)
        {
            rows[i] = StringUtils.repeat(" ",
                                         number_of_columns)
        }
        else if(length < number_of_columns)
        {
            rows[i] = rows[i] + StringUtils.repeat(" ",
                                                   number_of_columns - length)
        }
    }

    return rows;
}

/**
 * Obtains the caret offset and line number of the beginning of the first line in the current visible area.
 * Also returns an alternate (the next line) offset/line since scrolling may not allow the caret
 * to be on the first visible line.
 *
 * @return An integer array with [offset, line, alternate_offset, alternate_line].
 */
fun EditorFixture.get_visible_area_top_offset_line(): IntArray
{
    val script =
        """
            importPackage(com.intellij.util);
            importPackage(com.intellij.openapi.editor);
            
            const editor = local.get('editor');
            const document = local.get('document');
            
            const is_true_smooth_scrolling_enabled = EditorCoreUtil.isTrueSmoothScrollingEnabled();
            const model = editor.scrollingModel;
            const visible_area = is_true_smooth_scrolling_enabled ? model.getVisibleAreaOnScrollingFinished() : model.getVisibleArea();
            const visible_area_top_line_number = editor.yToVisualLine( visible_area.y );
            if( ( visible_area.y > editor.visualLineToY( visible_area_top_line_number ) ) &&
                    ( ( visible_area.y + visible_area.height ) > editor.visualLineToY( visible_area_top_line_number + 1 ) ) )
            {
                visible_area_top_line_number++;
            }
            
            const visible_area_top_line_number_alternate = visible_area_top_line_number + 1;
            const offset = document.getLineStartOffset(visible_area_top_line_number);
            const alternate_offset = document.getLineStartOffset(visible_area_top_line_number_alternate);
            
            offset.toString() + ',' + visible_area_top_line_number.toString() + ',' +
            alternate_offset.toString() + ',' + visible_area_top_line_number_alternate.toString();
        """.trimIndent();
    val offset_line = callJs<String>(script,
                                     true);
    val offset_lines = offset_line.split(",")
        .toTypedArray();
    return intArrayOf(offset_lines[0].toInt(),
                      offset_lines[1].toInt(),
                      offset_lines[2].toInt(),
                      offset_lines[3].toInt());
}

/**
 * Obtains the caret offset and line number of the end of the last line in the current visible area.
 * Also returns an alternate (the previous line) offset/line since scrolling may not allow the caret
 * to be on the last visible line.
 *
 * @return An integer array with [offset, line, alternate_offset, alternate_line].
 */
fun EditorFixture.get_visible_area_bottom_offset_line(): IntArray
{
    val script =
        """
            importPackage(com.intellij.util);
            importPackage(com.intellij.openapi.editor.ex.util);
            importPackage(com.intellij.openapi.editor);

            const editor = local.get('editor');
            const document = local.get('document');
            
            const is_true_smooth_scrolling_enabled = EditorCoreUtil.isTrueSmoothScrollingEnabled();
            const model = editor.scrollingModel;
            const visible_area = is_true_smooth_scrolling_enabled ? model.getVisibleAreaOnScrollingFinished() : model.getVisibleArea();
            const max_Y = visible_area.y + visible_area.height - editor.getLineHeight();
            const visible_area_bottom_line_number = editor.yToVisualLine( max_Y );
            if( ( visible_area_bottom_line_number > 0 ) &&
                ( max_Y < editor.visualLineToY( visible_area_bottom_line_number ) ) &&
                ( visible_area.y <= editor.visualLineToY( visible_area_bottom_line_number - 1 ) ) )
            {
                visible_area_bottom_line_number--;
            }
            
            const visible_area_bottom_line_number_alternate = visible_area_bottom_line_number - 1;
            const offset = document.getLineStartOffset(visible_area_bottom_line_number) +
                EditorUtil.getLastVisualLineColumnNumber( editor,
                                                          visible_area_bottom_line_number );
            const offset_alternate = document.getLineStartOffset( visible_area_bottom_line_number_alternate ) +
                EditorUtil.getLastVisualLineColumnNumber( editor,
                                                          visible_area_bottom_line_number_alternate );
            
            offset.toString() + ',' + visible_area_bottom_line_number.toString() + ',' +
            offset_alternate.toString() + ',' + visible_area_bottom_line_number_alternate.toString();
        """.trimIndent();
    val offset_line = callJs<String>(script,
                                     true);
    val offset_lines = offset_line.split(",")
        .toTypedArray();
    return intArrayOf(offset_lines[0].toInt(),
                      offset_lines[1].toInt(),
                      offset_lines[2].toInt(),
                      offset_lines[3].toInt());
}

fun EditorFixture.get_visible_area_left_offset_of_current_line(): Int
{
    val script =
        """
            importPackage(com.intellij.util);
            importPackage(com.intellij.openapi.editor.ex.util);
            importPackage(com.intellij.openapi.editor);
            
            const editor = local.get('editor');
            const document = local.get('document');
            
            const is_true_smooth_scrolling_enabled = EditorCoreUtil.isTrueSmoothScrollingEnabled();
            const model = editor.scrollingModel;
            const visible_area = is_true_smooth_scrolling_enabled ? model.getVisibleAreaOnScrollingFinished() : model.getVisibleArea();
            const cursor_point = editor.visualPositionToXY( editor.getCaretModel().getVisualPosition() );
            const window_left_at_line_point = new Point( visible_area.x, cursor_point.y );
            const window_left_at_line_visual_position = editor.xyToVisualPosition( window_left_at_line_point );
            editor.visualPositionToOffset( window_left_at_line_visual_position );
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_visible_area_right_visual_position_of_current_line(): VisualPosition
{
    val script =
        """
            importPackage(com.intellij.util);
            importPackage(com.intellij.openapi.editor.ex.util);
            importPackage(com.intellij.openapi.editor);
            
            const editor = local.get('editor');
            const document = local.get('document');
            
            const is_true_smooth_scrolling_enabled = EditorCoreUtil.isTrueSmoothScrollingEnabled();
            const model = editor.scrollingModel;
            const visible_area = is_true_smooth_scrolling_enabled ? model.getVisibleAreaOnScrollingFinished() : model.getVisibleArea();
            
            const width = editor.getScrollPane().getVerticalScrollBar().getWidth();
            const max_X = visible_area.x + visible_area.width - width;
            
            const cursor_point = editor.visualPositionToXY( editor.getCaretModel()
                                                              .getVisualPosition() );
            const window_right_at_line_point = new Point( max_X, cursor_point.y );
            const window_right_at_line_visual_position = editor.xyToVisualPosition( window_right_at_line_point );
            
            window_right_at_line_visual_position.line.toString() + ',' + window_right_at_line_visual_position.column.toString();
        """.trimIndent();
    val position = callJs<String>(script,
                                  true);
    val line_column = position.split(',')
        .toTypedArray()
    return VisualPosition(line_column[0].toInt(),
                          line_column[1].toInt());
}

fun EditorFixture.get_current_line_number(): Int
{
    val script =
        """
            const editor = local.get('editor');
            const document = local.get('document');
            editor.offsetToLogicalPosition(editor.getCaretModel().getOffset()).line;
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_line_number(offset: Int): Int
{
    val script =
        """
            const editor = local.get('editor');
            editor.offsetToLogicalPosition($offset).line;
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_start_offset(line_number: Int): Int
{
    val script =
        """
            const editor = local.get('editor');
            const document = local.get('document');
            document.getLineStartOffset($line_number);
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_end_column(line_number: Int): Int
{
    val script =
        """
            importPackage(com.intellij.openapi.editor.ex.util);
                                
            const editor = local.get('editor');
            EditorUtil.getLastVisualLineColumnNumber( editor, $line_number );
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_end_offset(line_number: Int): Int
{
    val script =
        """
            importPackage(com.intellij.openapi.editor.ex.util);
                                
            const editor = local.get('editor');
            const document = local.get('document');
            document.getLineStartOffset($line_number) +
                EditorUtil.getLastVisualLineColumnNumber( editor, $line_number );
        """.trimIndent();
    return callJs(script,
                  true)
}

fun EditorFixture.get_end_offset(): Int
{
    val script =
        """
            const document = local.get('document');
            document.getTextLength();
        """.trimIndent();
    return callJs(script,
                  true)
}

fun close_tip_of_the_day(remote_robot: RemoteRobot) = optional_step("Close Tip of the Day if it appears")
{
    waitFor(Duration.ofSeconds(30)) {
        remote_robot.findAll(DialogFixture::class.java,
                             byXpath("//div[@class='MyDialog'][.//div[@text='Running startup activities...']]"))
            .isEmpty()
    }

    val idea: IdeaFrame = remote_robot.find(IdeaFrame::class.java)
    idea.dumbAware {
        idea.find(DialogFixture::class.java,
                  byTitle("Tip of the Day"))
            .button("Close")
            .click()
    }

}

fun close_all_tabs(remote_robot: RemoteRobot) = step("Close all existing tabs")
{
    remote_robot.findAll<CommonContainerFixture>(byXpath("//div[@class='EditorTabs']//div[@class='SingleHeightLabel']"))
        .forEach {
            it.find<ComponentFixture>(byXpath("//div[@class='InplaceButton']"))
                .click()
        }
}

/**
 * Returns the length of the given line.
 * Note: Does not include the line separator.
 *
 * @param line_number The requested line number to get the length.
 * @return The length of the requested line.
 */
fun EditorFixture.get_line_length(line_number: Int): Int
{
    val script =
        """
            importPackage(com.intellij.openapi.editor.ex.util);
            
            const editor = local.get('editor');
            EditorUtil.getLastVisualLineColumnNumber( editor, $line_number );
        """.trimIndent();
    return callJs(script,
                  true)

}

val CommonContainerFixture.tree_fixtures: List<JTreeFixture>
    get() = findAll(JTreeFixture.byType())

/**
 * Clicks the given path, expanding parent nodes if necessary.
 *
 * @param path Tree path of the form "root, node1, node1.1" using ",space" as separator. Ex. "Editor, General".
 */
fun JTreeFixture.click_path(path: String)
{
    val paths = collectExpandedPaths().toString()
        .replaceFirst("TreePathToRow",
                      "")
        .split(", TreePathToRow")

    for(i in paths.indices)
    {
        val string = paths[i]
        if(string.contains(path))
        {
            println(string)
            val start = "row="
            val start_index = string.indexOf(start) + start.length
            val end = ')'
            val end_index = string.indexOf(end)
            if((start_index != -1) && (end_index != -1))
            {
                val index = string.substring(start_index,
                                             end_index)
                    .toInt()
                clickRow(index)
                break
            }
        }
    }

    Thread.sleep(500)
}

fun EditorFixture.get_caret_logical_position(): LogicalPosition
{
    val script =
        """
            const current_logical_position = local.get('editor').getCaretModel().getLogicalPosition();
            current_logical_position.line.toString() + ',' + current_logical_position.column.toString();
        """.trimIndent()
    val position = callJs<String>(script,
                                  true)
    val line_column = position.split(',')
    return LogicalPosition(line_column[0].toInt(),
                           line_column[1].toInt())
}

fun EditorFixture.get_caret_visual_position(): VisualPosition
{
    val script =
        """
            const editor = local.get('editor');
            const current_visual_position = editor.getCaretModel().getVisualPosition();
            current_visual_position.line.toString() + ',' + current_visual_position.column.toString();
        """.trimIndent()
    val position = callJs<String>(script,
                                  true)
    val line_column = position.split(',')
        .toTypedArray()
    return VisualPosition(line_column[0].toInt(),
                          line_column[1].toInt());
}

fun IdeaFrame.click_on_status_icon_settings()
{
    find<ComponentFixture>(byXpath("//div[@myicon='brief4ijidea.svg']")).click()
    waitFor { heavyWeightWindows().size == 1 }
    val list = heavyWeightWindows()[0].itemsList
    list.clickItem("Settings")
    Thread.sleep(500)
}

fun EditorFixture.get_delete_to_word_boundry_range(end_n_start: Boolean): Array<LogicalPosition>
{
    val script =
        """
            importPackage(com.intellij.openapi.editor.actions);
            
            const editor = local.get('editor');
            const document = local.get('document');
            
            const camel_mode = editor.getSettings().isCamelWords();
            const text_range = $end_n_start ? EditorActionUtil.getRangeToWordEnd(editor, camel_mode, true) :
                                              EditorActionUtil.getRangeToWordStart(editor, camel_mode, true);
            
            const start_position = editor.offsetToLogicalPosition(text_range.getStartOffset());
            const end_position = editor.offsetToLogicalPosition(text_range.getEndOffset());
            
            start_position.line.toString() + ',' + start_position.column.toString() + ',' +
                end_position.line.toString() + ',' + end_position.column.toString();
        """.trimIndent()
    val range = callJs<String>(script,
                               true);
    val start_end = range.split(',')
        .toTypedArray()
    return arrayOf(LogicalPosition(start_end[0].toInt(),
                                   start_end[1].toInt()),
                   LogicalPosition(start_end[2].toInt(),
                                   start_end[3].toInt()));
}

fun EditorFixture.get_clipboard_text(): String
{
    val script =
        """
            importPackage(com.intellij.openapi.application.ex);
            ClipboardUtil.getTextInClipboard();
        """.trimIndent()
    return callJs<String>(script,
                          true);
}
