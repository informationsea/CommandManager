/*
    CommandManager : manage commands
    Copyright (C) 2015 Yasunobu OKAMURA

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.informationsea.commandmanager.cli.sample;

import info.informationsea.commandmanager.cli.CLICommandConsole;
import info.informationsea.commandmanager.core.CommandManager;

import java.io.IOException;

/**
 * Example of CLICommandManager usage
 */
public class LineEditor {
    public static void main(String ... args) {
        CommandManager commandManager = new CommandManager();
        CLICommandConsole commandConsole = new CLICommandConsole(commandManager);
        LineEditorCommands.registerCommands(commandManager);

        try {
            commandConsole.startConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
