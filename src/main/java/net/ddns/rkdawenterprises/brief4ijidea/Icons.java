/*
 * Copyright (c) 2023 RKDAW Enterprises and Ralph Williamson
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

import com.intellij.ui.IconManager;

import javax.swing.*;

public final class Icons
{
    public static final Icon BRIEF4IJIDEA_ICON =
            IconManager.getInstance().getIcon("/icons/brief4ijidea.svg", Icons.class.getClassLoader());
    public static final Icon BRIEF4IJIDEA_DISABLED_ICON =
            IconManager.getInstance().getIcon("/icons/brief4ijidea_disabled.svg", Icons.class.getClassLoader());
}
