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
               "unused",
               "RedundantSemicolon")

package net.ddns.rkdawenterprises.brief4ijidea

import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.impl.KeymapImpl
import org.jetbrains.annotations.NonNls
import java.awt.Toolkit
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

data class Key_action(val action_ID: String, val second: String?)

class Key_event_to_string
{
    companion object
    {
        @NonNls
        private const val key_pressed_tag_1 = "\"<KEY_PRESSED>\"";
        @NonNls
        private const val key_pressed_tag_2 = "<KEY_PRESSED>,";
        @NonNls
        private const val key_released_tag_1 = "\"<KEY_RELEASED>\""
        @NonNls
        private const val key_released_tag_2 = "<KEY_RELEASED>,";
        @NonNls
        private const val key_typed_tag = "\"<KEY_TYPED>\"";
        @NonNls
        private const val unsupported_tag_1 = "\"<UNSUPPORTED>\"";
        @NonNls
        private const val unsupported_tag_2 = ",\"<UNSUPPORTED>\"";
        @NonNls
        private const val unmodified_tag = ",\"<UNMODIFIED>\"";
        @NonNls
        private const val k1_tag = "{\"K1\":";
        @NonNls
        private const val k2_tag = ",\"K2\":";
        @NonNls
        private const val alt_tag = "Alt";
        @NonNls
        private const val control_tag = "Control";
        @NonNls
        private const val meta_tag = "Meta";
        @NonNls
        private const val shift_tag = "Shift";


        /**
         * Creates a JSON string with keyboard key information. Does not support mouse buttons.
         *
         * @return A JSON string array with key information.
         *         For "KEY_PRESSED" or "KEY_RELEASED" it will be in the form of [ID, MODIFIERS, VK_x(without the "VK_" prefix)].
         *         For "KEY_TYPED" it will be in the form of [ID, MODIFIERS, character].
         */
        @NonNls
        @JvmStatic
        fun KeyEvent.to_string(): String
        {
            val e: KeyEvent = this;

            val builder = StringBuilder("[");

            when(e.id)
            {
                KeyEvent.KEY_PRESSED -> builder.append(key_pressed_tag_1);
                KeyEvent.KEY_TYPED -> builder.append(key_typed_tag);
                KeyEvent.KEY_RELEASED -> builder.append(key_released_tag_1);
                else ->
                {
                    builder.append(unsupported_tag_1);
                }
            }

            when(e.id)
            {
                KeyEvent.KEY_PRESSED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED ->
                {
                    val modifiers = get_modifiers_as_string(e);
                    if(modifiers == null) builder.append(unmodified_tag) else builder.append(",\"$modifiers\"");
                }
                else ->
                {
                    builder.append(unsupported_tag_1);
                }
            }

            when(e.id)
            {
                KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED ->
                {
                    val name = get_key_name_as_string(e);
                    builder.append(",\"<$name>\"");
                }

                KeyEvent.KEY_TYPED ->
                {
                    val character = e.keyChar;
                    builder.append(",\"<$character>\"");
                }
            }

            builder.append(']');
            return builder.toString();
        }

        @NonNls
        @JvmStatic
        fun KeymapImpl.to_map(): Map<String, Key_action>?
        {
            val k: KeymapImpl = this;

            val map = HashMap<String, Key_action>()

            val action_IDs = k.actionIds;
            for(action_ID in action_IDs)
            {
                val shortcuts = k.getShortcuts(action_ID);
                for(shortcut in shortcuts)
                {
                    if(shortcut.isKeyboard)
                    {
                        val shortcut_string = (shortcut as KeyboardShortcut).to_string();
                        @NonNls
                        val search_string1 = "{\"K1\":[";
                        @NonNls
                        val search_string2 = "],\"K2\":[";
                        val start_index1 = shortcut_string.indexOf(search_string1) + search_string1.length;
                        val start_index2 = shortcut_string.indexOf(search_string2);
                        val end_index1 = if(start_index2 != -1) start_index2 else (shortcut_string.length - 2);
                        val end_index2 = shortcut_string.length - 2;

                        val key1 = shortcut_string.substring(start_index1, end_index1).
                            replace("\"","").
                        replace(key_pressed_tag_2, "").
                        replace(key_released_tag_2, "");

                        var key2: String? = null;
                        if(start_index2 != -1)
                        {
                            key2 = shortcut_string.substring(start_index2 + search_string2.length, end_index2).
                            replace("\"","").
                            replace(key_pressed_tag_2, "").
                            replace(key_released_tag_2, "");
                        }

                        map[key1] = Key_action(action_ID,
                                               key2);

                    }
                }
            }

            return if( map.isEmpty() ) null else map;
        }

        /**
         * Creates a JSON string with keyboard shortcut key information.
         *
         * @return A JSON string with key information.
         *         It will be in the form of Kx:[ID, MODIFIERS, VK_y],
         *         where x is 1 or 2 for each of the 2 possible keystrokes, and
         *         y is the key name without the "VK_" prefix.
         *
         */
        @NonNls
        @JvmStatic
        fun KeyboardShortcut.to_string(): String
        {
            val k: KeyboardShortcut = this;

            val builder = StringBuilder(k1_tag);

            builder.append(get_keystroke_as_string(k.firstKeyStroke));

            if(k.secondKeyStroke != null)
            {
                builder.append(k2_tag);
                builder.append(get_keystroke_as_string(k.secondKeyStroke!!));
            }

            builder.append("}");
            return builder.toString();
        }

        /**
         * Creates a JSON string with keystroke key information.
         *
         * @return A JSON string array with key information.
         *         It will be in the form of [ID, MODIFIERS, VK_x(without the "VK_" prefix)].
         *
         */
        @NonNls
        @JvmStatic
        private fun get_keystroke_as_string( k: KeyStroke ): String
        {
            val builder = StringBuilder("[");

            val ID = k.keyEventType;

            when(ID)
            {
                KeyEvent.KEY_PRESSED -> builder.append(key_pressed_tag_1);
                KeyEvent.KEY_TYPED -> builder.append(key_typed_tag);
                KeyEvent.KEY_RELEASED -> builder.append(key_released_tag_1);
                else ->
                {
                    builder.append(unsupported_tag_1);
                }
            }

            when(ID)
            {
                KeyEvent.KEY_PRESSED, KeyEvent.KEY_TYPED, KeyEvent.KEY_RELEASED ->
                {
                    val modifiers = get_modifiers_as_string(k.modifiers and (KeyEvent.ALT_DOWN_MASK or KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK));
                    if(modifiers == null) builder.append(unmodified_tag) else builder.append(",\"$modifiers\"");
                }
                else ->
                {
                    builder.append(unsupported_tag_2);
                }
            }

            when(ID)
            {
                KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED ->
                {
                    val name = get_key_name_as_string(k.keyChar,
                                                      KeyEvent.getKeyText(k.keyCode));
                    builder.append(",\"<$name>\"");
                }

                KeyEvent.KEY_TYPED ->
                {
                    val character = k.keyChar;
                    builder.append(",\"<$character>\"");
                }
            }

            builder.append("]");
            return builder.toString();
        }

        @JvmStatic
        private fun get_modifiers_as_string(e: KeyEvent): String?
        {
            val modifiers = e.modifiersEx and (KeyEvent.ALT_DOWN_MASK or KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK);
            return get_modifiers_as_string(modifiers);
        }

        @NonNls
        @JvmStatic
        private fun get_modifiers_as_string(modifiers: Int): String?
        {
            if(modifiers == 0) return null;

            val result = StringBuilder();

            if(modifiers and KeyEvent.ALT_DOWN_MASK != 0) result.append("<${
                Toolkit.getProperty("AWT.alt",
                                    alt_tag)
            }>");
            if(modifiers and KeyEvent.CTRL_DOWN_MASK != 0) result.append("<${
                Toolkit.getProperty("AWT.control",
                                    control_tag)
            }>");
            if(modifiers and KeyEvent.META_DOWN_MASK != 0) result.append("<${
                Toolkit.getProperty("AWT.meta",
                                    meta_tag)
            }>");
            if(modifiers and KeyEvent.SHIFT_DOWN_MASK != 0) result.append("<${
                Toolkit.getProperty("AWT.shift",
                                    shift_tag)
            }>");

            return if(result.isNotEmpty()) result.toString() else null;
        }

        @JvmStatic
        private fun get_key_name_as_string(e: KeyEvent): String
        {
            val key_text = KeyEvent.getKeyText(e.keyCode);
            val modifiers = e.modifiersEx and (KeyEvent.ALT_DOWN_MASK or KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK);
            val key_char = if( modifiers == 0 ) e.keyChar else KeyEvent.CHAR_UNDEFINED;
            return get_key_name_as_string(key_char,key_text);
        }

        @JvmStatic
        private fun get_key_name_as_string(key_char: Char, key_text: String): String
        {
            return if((key_char == KeyEvent.CHAR_UNDEFINED) || (key_text.length > 1))
            {
                key_text;
            }
            else
            {
                String(charArrayOf(key_char));
            }
        }
    }
}