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

package info.informationsea.commandmanager.core;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage commands.
 * @author Yasunobu OKAMURA
 */
public class CommandManager {
    private Map<String, Class> commands = new HashMap<>();

    @Getter @Setter
    private Object context = null;

    /**
     * Register a new command to command manager.
     * @param name a command name
     * @param command a class of ManagedCommand
     * @param <T> a ManagedCommand
     */
    public <T extends ManagedCommand> void addCommand(String name, Class<T> command) {
        if (commands.containsKey(name)) {
            throw new IllegalArgumentException("Command name is duplicated");
        }
        commands.put(name, command);
    }

    /**
     * Get a list of commands.
     * @return a list fo commands.
     */
    public Map<String, Class> getCommands() {
        return new HashMap<>(commands);
    }

    /**
     * Get a instance of ManagedCommand that corresponding to the name
     * @param name a command name
     * @return a instance of ManagedCommand
     */
    public ManagedCommand getCommandInstance(String name) {
        Class clazz = getCommandForName(name);
        try {
            ManagedCommand instance = (ManagedCommand) clazz.newInstance();
            instance.setContext(context);
            return instance;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get a Class of ManagedCommand corresponding to the name
     * @param name a command name
     * @return a Class of ManagedCommand
     */
    public Class getCommandForName(String name) {
        return commands.get(name);
    }
}
