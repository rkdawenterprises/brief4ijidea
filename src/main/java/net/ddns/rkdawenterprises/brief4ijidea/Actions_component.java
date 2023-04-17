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

package net.ddns.rkdawenterprises.brief4ijidea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.impl.ActionManagerImpl;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.intellij.openapi.keymap.impl.DefaultKeymap;
import com.intellij.openapi.keymap.impl.KeymapImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static net.ddns.rkdawenterprises.brief4ijidea.MiscellaneousKt.capitalize_character_at_index;

@SuppressWarnings({ "unused", "CommentedOutCode" })
public class Actions_component
{
    private Actions_component() { }

    private static Map<String, Keymap_action_data> s_keymap_file_parsed = null;

    public static @NotNull String setup_actions()
            throws IOException, ParserConfigurationException, SAXException
    {
        s_keymap_file_parsed =
                parse_keymap_file( "/keymaps/Brief.xml" );

        if( s_keymap_file_parsed.isEmpty() ) throw new RuntimeException( "Keymap file error" );

        add_actions( s_keymap_file_parsed );

        final KeymapManagerEx keymap_manager = KeymapManagerEx.getInstanceEx();
        String current_keymap = ( (KeymapImpl)keymap_manager.getActiveKeymap() ).getName();

        if( State_component.get_instance()
                           .get_check_active_keymap_is_brief() )
        {
            final KeymapImpl brief_keymap = (KeymapImpl)keymap_manager.getKeymap( Localized_messages.message( "brief" ) );
            if( brief_keymap != null )
            {
                brief_keymap.setCanModify( true );
                keymap_manager.setActiveKeymap( brief_keymap );
//                fix_keymap_conflicts( s_keymap_file_parsed,
//                                      brief_keymap );

                State_component.status_bar_message( Localized_messages.message( "keymap.set.to.brief" ) );
            }
            else
            {
                throw new RuntimeException( "Could not load Brief keymap" );
            }
        }

        return current_keymap;
    }

    public static void disable_actions( @Nullable String previous_keymap_name )
    {
        if( State_component.get_instance()
                           .get_check_active_keymap_is_brief() )
        {
            final KeymapManagerEx keymap_manager = KeymapManagerEx.getInstanceEx();

            KeymapImpl default_keymap;

            if( ( previous_keymap_name == null ) || previous_keymap_name.equals( Localized_messages.message( "brief" ) ) )
            {
                default_keymap = (KeymapImpl)keymap_manager.getKeymap( DefaultKeymap.getInstance()
                                                                                    .getDefaultKeymapName() );
            }
            else
            {
                default_keymap = (KeymapImpl)keymap_manager.getKeymap( previous_keymap_name );
            }

            if( default_keymap != null )
            {
                keymap_manager.setActiveKeymap( default_keymap );
                State_component.status_bar_message( Localized_messages.message( "keymap.restored.to.0", default_keymap.getName() ) );
            }
        }

        remove_actions( s_keymap_file_parsed );
    }

    public static Iterable<Node> iterable( @NotNull final NodeList node_list )
    {
        return () -> new Iterator<>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return index < node_list.getLength();
            }

            @Override
            public Node next()
            {
                if( !hasNext() )
                    throw new NoSuchElementException();
                return node_list.item( index++ );
            }
        };
    }

    public static @NotNull @NonNls Map<String, Keymap_action_data> parse_keymap_file( @NotNull String path )
            throws IOException, ParserConfigurationException, SAXException
    {
        try( InputStream input_stream =
                     State_component.get_instance()
                                    .getClass()
                                    .getResourceAsStream( path ) )
        {
            final HashMap<String, Keymap_action_data> result = new HashMap<>();

            final Document document = DocumentBuilderFactory.newInstance()
                                                            .newDocumentBuilder()
                                                            .parse( input_stream );

            final Node keymap_node = document.getElementsByTagName( "keymap" )
                                             .item( 0 );
            @NonNls String last_comment_text = "N/A";
            for( Node node : iterable( keymap_node.getChildNodes() ) )
            {
                if( node != null )
                {
                    if( node.getNodeType() == Node.COMMENT_NODE )
                    {
                        Comment comment = (Comment)node;
                        last_comment_text = comment.getTextContent();
                    }
                    else if( node.getNodeType() == Node.ELEMENT_NODE )
                    {
                        String[] action_text = parse_action_text( last_comment_text );

                        @NonNls Element action_element = (Element)node;
                        if( !action_element.getNodeName()
                                           .equals( "action" ) ) continue;
                        String action_ID = action_element.getAttribute( "id" );
                        if( action_ID.length() <= 0 ) continue;
                        NodeList keyboard_shortcut_node_list = action_element.getElementsByTagName( "keyboard-shortcut" );
                        if( keyboard_shortcut_node_list.getLength() <= 0 ) continue;

                        Keymap_action_data keymap_action = new Keymap_action_data();
                        keymap_action.setAction_ID( action_ID );
                        if( action_text != null )
                        {
                            keymap_action.setText( action_text[0] );
                            keymap_action.setDescription( action_text[1] );
                        }

                        for( Node keyboard_shortcut_node : iterable( keyboard_shortcut_node_list ) )
                        {
                            if( keyboard_shortcut_node.getNodeType() != Node.ELEMENT_NODE ) continue;
                            Element keyboard_shortcut_element = (Element)keyboard_shortcut_node;
                            String first_keystroke_string = keyboard_shortcut_element.getAttribute( "first-keystroke" );
                            String second_keystroke_string = keyboard_shortcut_element.getAttribute( "second-keystroke" );
                            KeyStroke first_keystroke = KeyStroke.getKeyStroke( first_keystroke_string );
                            if( first_keystroke == null ) continue;
                            KeyStroke second_keystroke = KeyStroke.getKeyStroke( second_keystroke_string );
                            keymap_action.getShortcuts()
                                         .add( new KeyboardShortcut( first_keystroke,
                                                                     second_keystroke ) );
                        }

                        result.put( action_ID,
                                    keymap_action );
                    }
                }
            }

            return result;
        }
    }

    public static final @NonNls String s_command_key_string = "Command:";
    public static final @NonNls String s_description_key_string = "Description:";

    private static @NonNls String[] parse_action_text( @Nullable String text )
    {
        if( text == null ) return null;

        String command_string = null;
        String description_string = null;

        int command_start = text.indexOf( s_command_key_string );
        if( command_start != -1 )
        {
            command_start += s_command_key_string.length();
            int command_end = text.indexOf( s_description_key_string );
            if( ( command_end == -1 ) || ( command_end <= command_start ) ) command_end = text.length();
            {
                command_string = text.substring( command_start,
                                                 command_end )
                                     .replaceAll( "\\p{Punct}",
                                                  "" )
                                     .trim();
            }
        }

        int description_start = text.indexOf( s_description_key_string );
        if( description_start != -1 )
        {
            description_start += s_description_key_string.length();
            int description_end = text.indexOf( s_description_key_string );
            if( ( description_end == -1 ) || ( description_end <= description_start ) ) description_end = text.length();
            {
                description_string = text.substring( description_start,
                                                     description_end )
                                         .replaceAll( "\\p{Punct}",
                                                      "" )
                                         .trim();
            }
        }

        return new String[] { command_string, description_string };
    }

    private static void add_actions( @NotNull Map<String, Keymap_action_data> keymap_file )
    {
        remove_actions( keymap_file );

        ActionManagerEx action_manager_ex = ActionManagerImpl.getInstanceEx();

        for( Map.Entry<String, Keymap_action_data> keymap_file_entry : keymap_file.entrySet() )
        {
            @NonNls String action_ID = keymap_file_entry.getKey();
            if( action_ID.startsWith( "net.ddns.rkdawenterprises.brief4ijidea.actions." ) )
            {
                String text = keymap_file_entry.getValue()
                                               .getText();
                String description = keymap_file_entry.getValue()
                                                      .getDescription();
                String action_class_string =
                        capitalize_character_at_index( action_ID,
                                                       action_ID.lastIndexOf( '.' ) + 1 ) + "_action";
                try
                {
                    AnAction action = (AnAction)Class.forName( action_class_string )
                                                     .getConstructor( String.class,
                                                                      String.class )
                                                     .newInstance( text,
                                                                   description );

                    action_manager_ex.registerAction( action_ID,
                                                      action,
                                                      PluginId.getId( "net.ddns.rkdawenterprises.brief4ijidea" ) );
                }
                catch( ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception )
                {
                    @NonNls String message = "brief4ijidea.Actions_component.add_actions: Error, ";
                    System.out.println( message + exception );
                }
            }
        }
    }

    private static void remove_actions( @NotNull Map<String, Keymap_action_data> keymap_file )
    {
        ActionManagerEx action_manager_ex = ActionManagerImpl.getInstanceEx();

        for( Map.Entry<String, Keymap_action_data> keymap_file_entry : keymap_file.entrySet() )
        {
            @NonNls String action_ID = keymap_file_entry.getKey();
            if( action_ID.startsWith( "net.ddns.rkdawenterprises.brief4ijidea.actions." ) )
            {
                action_manager_ex.unregisterAction( action_ID );
            }
        }
    }
    
    private static void fix_keymap_conflicts( @NotNull Map<String, Keymap_action_data> keymap_file,
                                              @NotNull KeymapImpl brief_keymap )
    {
        for( Map.Entry<String, Keymap_action_data> keymap_file_entry : keymap_file.entrySet() )
        {
            String action_ID = keymap_file_entry.getKey();
            Keymap_action_data keymap_action = keymap_file_entry.getValue();
            for( KeyboardShortcut keyboard_shortcut : keymap_action.getShortcuts() )
            {
                final Map<String, ? extends List<KeyboardShortcut>> conflicts =
                        brief_keymap.getConflicts( "",
                                                   keyboard_shortcut );

                for( Map.Entry<String, ? extends List<KeyboardShortcut>> keymap_file_conflict : conflicts.entrySet() )
                {
                    if( keymap_file_conflict.getKey()
                                            .equals( action_ID ) ) continue;

                    List<KeyboardShortcut> keyboard_shortcut_conflicts =
                            keymap_file_conflict.getValue();
                    for( KeyboardShortcut keyboard_shortcut_conflict : keyboard_shortcut_conflicts )
                    {
                        brief_keymap.removeShortcut( keymap_file_conflict.getKey(),
                                                     keyboard_shortcut_conflict );
                    }
                }
            }
        }
    }
}
