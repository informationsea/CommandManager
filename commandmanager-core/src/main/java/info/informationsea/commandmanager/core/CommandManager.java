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
 */
public class CommandManager {
    private Map<String, Class> commands = new HashMap<>();

    @Getter @Setter
    private Object context = null;

    public <T extends ManagedCommand> void addCommand(String name, Class<T> command) {
        if (commands.containsKey(name)) {
            throw new IllegalArgumentException("Command name is duplicated");
        }
        commands.put(name, command);
    }

    public Map<String, Class> getCommands() {
        return new HashMap<>(commands);
    }

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

    public Class getCommandForName(String name) {
        return commands.get(name);
    }
}
