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

package info.informationsea.commandmanager.cli;

import info.informationsea.commandmanager.core.CommandManager;
import info.informationsea.commandmanager.core.ManagedCommand;
import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Command Manager for Command Line Interface.
 *
 * This command manager provides two built-in commands, "help" and "source". "help" command shows usage of commands.
 * "source" command read a script file and run commands.
 * @author Yasunobu OKAMURA
 */
@Slf4j
public class CLICommandManager extends CommandManager {

    /**
     * Default constructor
     */
    public CLICommandManager() {
        addCommand("help", CLIHelpCommand.class);
        addCommand("source", CLISourceCommand.class);
    }

    /**
     * Execute commands in the raw line. The raw line will be parsed with ShellParser.
     * @param line commands. Commands should be separated by ';'
     * @throws Exception Commands may throw Exception.
     */
    public void executeMany(String line) throws Exception {
        List<String> args = ShellParser.parseShellLine(line);
        executeMany(args.toArray(new String[args.size()]));
    }

    /**
     * Execute commands in the parsed arguments.
     * @param args commands. Commands should be separated by ';'
     * @throws Exception Commands may throw Exception.
     */
    public void executeMany(String[] args) throws Exception {
        int startPos = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(";")) {
                String[] newArgs = new String[i-startPos];
                System.arraycopy(args, startPos, newArgs, 0, i-startPos);
                //log.info("run {}", (Object) newArgs);
                execute(newArgs);
                startPos = i+1;
            }
        }
        String[] newArgs = new String[args.length-startPos];
        System.arraycopy(args, startPos, newArgs, 0, args.length-startPos);
        //log.info("run last {}", (Object) newArgs);
        execute(newArgs);
    }

    /**
     * Execute a commands in the raw line. The raw line will be parsed with ShellParser.
     * @param line a command and its arguments.
     * @throws Exception A command may throw Exception.
     */
    public void execute(String line) throws Exception {
        List<String> args = ShellParser.parseShellLine(line);
        execute(args.toArray(new String[args.size()]));
    }

    /**
     * Execute a commands in the string array.
     * @param args a command and its arguments.
     * @throws Exception A command may throw Exception.
     */
    public void execute(String[] args) throws Exception {
        ManagedCommand managedCommand = getConfiguredCommandInstance(args);
        if (managedCommand == null) {
            throw new IllegalArgumentException("Command is not found");
        }
        managedCommand.execute();
    }

    /**
     * Get a configured commands in the raw line.
     * Parameters of a command are configured with arguments.
     * The raw line will be parsed with ShellParser.
     * @param line a command and its arguments.
     * @throws CmdLineException A argument parser may throw Exception.
     */
    public ManagedCommand getConfiguredCommandInstance(String line) throws CmdLineException {
        List<String> args = ShellParser.parseShellLine(line);
        return getConfiguredCommandInstance(args.toArray(new String[args.size()]));
    }

    /**
     * Get a configured commands with a string array.
     * Parameters of a command are configured with arguments.
     * @param args a command and its arguments.
     * @throws CmdLineException A argument parser may throw Exception.
     */
    public ManagedCommand getConfiguredCommandInstance(String[] args) throws CmdLineException {
        ManagedCommand command = getCommandInstance(args[0]);
        String[] commandArgs = new String[args.length-1];
        System.arraycopy(args, 1, commandArgs, 0, args.length - 1);
        CmdLineParser parser = new CmdLineParser(command);
        parser.parseArgument(commandArgs);

        if (command instanceof CLIBuiltinCommand) {
            ((CLIBuiltinCommand) command).setCommandManager(this);
        }

        return command;
    }

    /**
     * Load a script from a reader and execute.
     * @param reader script reader
     * @throws Exception commands may throw Exception
     */
    public void loadScript(Reader reader) throws Exception{
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                executeMany(line);
            }
        }
    }

    /**
     * Start console interface with the default console reader
     * @throws IOException console reade may throw IOException
     */
    public void startConsole() throws IOException {
        startConsole(new ConsoleReader());
    }

    /**
     * Start console interface with consoleReader
     * @param consoleReader terminal console reader
     * @throws IOException console reade may throw IOException
     */
    public void startConsole(ConsoleReader consoleReader) throws IOException {
        consoleReader.addCompleter(new CLICommandCompleter(this));
        String line;
        out : while ((line = consoleReader.readLine("> ")) != null) {
            List<String> args = ShellParser.parseShellLine(line);
            if (args.size() == 0) continue;
            switch (args.get(0)) {
                case "clear":
                    consoleReader.clearScreen();
                    break;
                case "exit":
                    break out;
                default:
                    try {
                        execute(args.toArray(new String[args.size()]));
                    } catch (Exception e) {
                        log.info("Execute Error", e);
                    }
            }
        }
    }

    /**
     * Super class of CLI command manager built-in commands
     */
    public abstract static class CLIBuiltinCommand implements ManagedCommand {
        @Setter @Getter
        protected CLICommandManager commandManager;
    }

    public static class CLIHelpCommand extends CLIBuiltinCommand {

        @Argument
        private String command = null;

        @Override
        public void execute() throws Exception {
            if (command == null) {
                System.err.println("Command List:");
                for (String one : commandManager.getCommands().keySet()) {
                    System.err.printf("   %s\n", one);
                }
                for (String one : new String[]{"exit", "clear", "source"}) {
                    System.err.printf("   %s\n", one);
                }
            } else {
                switch (command) {
                    case "exit":
                        System.err.println("exit : exit this program");
                        break;
                    case "clear":
                        System.err.println("clear : clear screen");
                        break;
                    case "source":
                        System.err.println("source FILE: load script");
                        break;
                    default:
                        CmdLineParser parser = new CmdLineParser(commandManager.getCommandInstance(command));
                        System.err.printf("%s ", command);
                        parser.printSingleLineUsage(System.err);
                        System.err.println();
                        parser.printUsage(System.err);
                }
            }
        }

        @Override
        public List<String> getCandidateForArgument(int index) {
            if (index == 0) {
                ArrayList<String> list = new ArrayList<>();
                list.addAll(commandManager.getCommands().keySet());
                list.add("exit");
                list.add("clear");
                list.add("source");
                return list;
            }
            return null;
        }
    }

    public static class CLISourceCommand extends CLIBuiltinCommand {

        @Argument (required = true)
        private File source;

        @Override
        public void execute() throws Exception {
            commandManager.loadScript(new FileReader(source));
        }
    }
}
