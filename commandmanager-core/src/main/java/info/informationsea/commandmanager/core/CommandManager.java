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
import lombok.Value;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage commands.
 * @author Yasunobu OKAMURA
 */
public class CommandManager {
    private Map<String, Class> commands = new HashMap<>();

    @Getter @Setter
    private Object context = null;

    private Map<String, OptionInfo> optionInfoMap = new HashMap<>();

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

        try {
            Object bean = command.newInstance();
            CmdLineParser parser = new CmdLineParser(bean);
            optionInfoMap.put(name, new OptionInfo(parser.getOptions(), parser.getArguments()));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
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

    /**
     * Get command options and arguments for the name
     * @param name a command name
     * @return a list of command options and arguments
     */
    public OptionInfo getOptionInfoForName(String name) {
        return optionInfoMap.get(name);
    }

    /**
     * Command Option Information.
     * The list of command options and arguments.
     */
    @Value
    public static class OptionInfo {
        private Map<String, OptionHandler> options;
        private List<OptionHandler> arguments;

        public OptionInfo(List<OptionHandler> options, List<OptionHandler> arguments) {
            this.options = new HashMap<>();
            this.arguments = arguments;

            for (OptionHandler one : options) {
                this.options.put(one.option.toString(), one);
                //log.info("option {} {}", one, one.option.toString());
            }
        }

        public static List<String> candidateOptions(OptionHandler optionHandler) {
            Class clazz = optionHandler.setter.getType();
            if (clazz.isEnum()) {
                List<String> candidate = new ArrayList<>();
                for (Object obj : clazz.getEnumConstants()) {
                    candidate.add(obj.toString());
                }
                return candidate;
            }

            return null;
        }
    }
}
