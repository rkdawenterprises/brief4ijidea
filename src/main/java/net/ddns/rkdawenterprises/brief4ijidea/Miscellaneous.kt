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

@file:Suppress("FunctionName",
               "LocalVariableName",
               "PrivatePropertyName",
               "HardCodedStringLiteral",
               "unused",
               "RedundantSemicolon",
               "UsePropertyAccessSyntax",
               "KDocUnresolvedReference")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCoreUtil
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DoNotAskOption
import com.intellij.openapi.ui.MessageDialogBuilder.Companion.okCancel
import java.awt.Rectangle
import java.util.*

fun to_nearest_visual_line_base(editor: Editor,
                                y: Int): Int
{
    val transformed = editor.visualLineToY(editor.yToVisualLine(y))
    return if(y > transformed && y < transformed + editor.lineHeight) transformed else y
}

/**
 * Scrolls the given editor the given number of lines up or down.
 *
 * @param editor The editor to scroll.
 * @param lines Negative value scrolls to a decreasing line number. Positive value scrolls to an increasing line number.
 */
fun scroll_lines(editor: Editor,
                 lines: Int)
{
    val lineHeight: Int = editor.lineHeight
    val visibleArea = get_editor_content_visible_area(editor)
    val y = visibleArea.y - (lineHeight * lines);
    val scroll_offset = to_nearest_visual_line_base(editor,
                                                    y);
    editor.scrollingModel
        .scrollVertically(scroll_offset);
}

fun has_selection(editor: Editor): Boolean
{
    return editor.selectionModel
        .hasSelection();
}

fun capitalize_character_at_index(string: String,
                                  index: Int): String
{
    return string.substring(0,
                            index) +
            string.substring(index,
                             index + 1)
                .uppercase(Locale.getDefault()) +
            string.substring(index + 1)
}

fun get_undo_manager(project: Project?): UndoManager
{
    return if((project != null) && !project.isDefault) UndoManager.getInstance(project) else UndoManager.getGlobalInstance();
}

fun stop_all_marking_modes(editor: Editor,
                           remove_selection: Boolean)
{
    Marking_component.stop_marking_mode(editor,
                                        remove_selection)
    Line_marking_component.stop_line_marking_mode(editor,
                                                  remove_selection)
    Column_marking_component.stop_column_marking_mode(editor,
                                                      remove_selection)
    State_component.status_bar_message(null)
    if(remove_selection)
    {
        if(has_selection(editor))
        {
            editor.caretModel
                .removeSecondaryCarets()
            editor.selectionModel
                .removeSelection()
        }
    }
}

fun stop_all_marking_modes(editor: Editor)
{
    stop_all_marking_modes(editor,
                           true)
}

fun validate_position(editor: Editor,
                      position: LogicalPosition): LogicalPosition
{
    return editor.offsetToLogicalPosition(editor.logicalPositionToOffset(position))
}

fun editor_gained_focus(editor: Editor)
{
    stop_all_marking_modes(editor,
                           false)
}

fun editor_lost_focus(editor: Editor)
{
    stop_all_marking_modes(editor,
                           false)
}

fun toggle_marking_mode(editor: Editor)
{
    Line_marking_component.stop_line_marking_mode(editor,
                                                  true)
    Column_marking_component.stop_column_marking_mode(editor,
                                                      true)
    Marking_component.toggle_marking_mode(editor)
}

fun toggle_line_marking_mode(editor: Editor)
{
    Marking_component.stop_marking_mode(editor,
                                        true)
    Column_marking_component.stop_column_marking_mode(editor,
                                                      true)
    Line_marking_component.toggle_line_marking_mode(editor)
}

fun toggle_column_marking_mode(editor: Editor)
{
    Marking_component.stop_marking_mode(editor,
                                        true)
    Line_marking_component.stop_line_marking_mode(editor,
                                                  true)
    Column_marking_component.toggle_column_marking_mode(editor)
}

fun get_bottom_of_window_line_number(editor: Editor): Int
{
    val visible_area = get_editor_content_visible_area(editor);
    val max_Y: Int = visible_area.y + visible_area.height - editor.lineHeight;
    var visible_area_bottom_line_number = editor.yToVisualLine(max_Y);
    if(visible_area_bottom_line_number > 0 &&
        max_Y < editor.visualLineToY(visible_area_bottom_line_number) &&
        visible_area.y <= editor.visualLineToY(visible_area_bottom_line_number - 1))
    {
        visible_area_bottom_line_number--;
    }

    return visible_area_bottom_line_number;
}

fun get_top_of_window_line_number(editor: Editor): Int
{
    val visible_area = get_editor_content_visible_area(editor);
    var visible_area_top_line_number = editor.yToVisualLine(visible_area.y);
    if(visible_area.y > editor.visualLineToY(visible_area_top_line_number) &&
        visible_area.y + visible_area.height > editor.visualLineToY(visible_area_top_line_number + 1)
    )
    {
        visible_area_top_line_number++;
    }

    return visible_area_top_line_number;
}

fun get_editor_content_visible_area(editor: Editor): Rectangle
{
    val model = editor.scrollingModel
    return if(EditorCoreUtil.isTrueSmoothScrollingEnabled()) model.visibleAreaOnScrollingFinished else model.visibleArea;
}

fun virtual_space_setting_warning(editor: Editor)
{
    val do_not_show_virtual_space_setting_dialog = State_component.get_instance()
        .get_do_not_show_virtual_space_setting_dialog();
    if(!do_not_show_virtual_space_setting_dialog)
    {
        val editor_settings = editor.settings;
        if(!editor_settings.isVirtualSpace)
        {
            ApplicationManager.getApplication()
                .invokeLater {
                    warning_message("Change Settings for this Command",
                        Localized_messages.message("you.must.enable.settings.editor.general.virtual.space.after.the.end.of.line.for.some.commands.right.side.of.window.and.column.marking.mode.to.work.properly"),
                        object : DoNotAskOption.Adapter()
                        {
                            /**
                             * Save the state of the checkbox in the settings, or perform some other related action.
                             * This method is called right after the dialog is [closed][.close].
                             * <br></br>
                             * Note that this method won't be called in the case when the dialog is closed by [Cancel][.CANCEL_EXIT_CODE]
                             * if [saving the choice on cancel is disabled][.shouldSaveOptionsOnCancel] (which is by default).
                             *
                             * @param isSelected true if user selected "don't show again".
                             * @param exitCode   the [exit code][.getExitCode] of the dialog.
                             * @see .shouldSaveOptionsOnCancel
                             */
                            /**
                             * Save the state of the checkbox in the settings, or perform some other related action.
                             * This method is called right after the dialog is [closed][.close].
                             * <br></br>
                             * Note that this method won't be called in the case when the dialog is closed by [Cancel][.CANCEL_EXIT_CODE]
                             * if [saving the choice on cancel is disabled][.shouldSaveOptionsOnCancel] (which is by default).
                             *
                             * @param isSelected true if user selected "don't show again".
                             * @param exitCode   the [exit code][.getExitCode] of the dialog.
                             * @see .shouldSaveOptionsOnCancel
                             */
                            override fun rememberChoice(isSelected: Boolean,
                                                        exitCode: Int)
                            {
                                State_component.get_instance()._do_not_show_virtual_space_setting_dialog = isSelected;
                            }
                        });
                }
        }
    }
}

fun warning_message(title: String,
                    message: String,
                    option: DoNotAskOption.Adapter? = null): Boolean
{
    return okCancel(title, message)
        .doNotAsk(option)
        .asWarning()
        .guessWindowAndAsk();
}
