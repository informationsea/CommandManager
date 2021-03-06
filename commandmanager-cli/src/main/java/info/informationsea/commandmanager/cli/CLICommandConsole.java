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
import info.informationsea.commandmanager.core.CommandResult;
import info.informationsea.commandmanager.core.ManagedCommand;
import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.Argument;
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
public class CLICommandConsole {

    @Getter
    private CommandManager commandManager;

    /**
     * Create CLICommandConsole to start console prompt.
     * @param commandManager A command manager object.
     */
    public CLICommandConsole(CommandManager commandManager) {
        this.commandManager = commandManager;
        commandManager.addCommand("help", CLIHelpCommand.class);
        commandManager.addCommand("source", CLISourceCommand.class);
    }

    /**
     * Execute commands in the raw line. The raw line will be parsed with {@code ShellParser}.
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
                if (newArgs.length > 0)
                    execute(newArgs);
                startPos = i+1;
            }
        }
        String[] newArgs = new String[args.length-startPos];
        System.arraycopy(args, startPos, newArgs, 0, args.length-startPos);
        //log.info("run last {}", (Object) newArgs);
        if (newArgs.length > 0)
            execute(newArgs);
    }

    /**
     * Execute a commands in the raw line. The raw line will be parsed with {@code ShellParser}.
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
        CommandResult result = managedCommand.execute();
        if (result.getResult() != null) {
            System.out.println(result.getResult());
        }
    }

    /**
     * Get a configured commands in the raw line.
     * Parameters of a command are configured with arguments.
     * The raw line will be parsed with {@code ShellParser}.
     * @param line a command and its arguments.
     * @throws Exception A argument parser may throw Exception.
     * @return A configured command instance
     */
    public ManagedCommand getConfiguredCommandInstance(String line) throws Exception {
        List<String> args = ShellParser.parseShellLine(line);
        return getConfiguredCommandInstance(args.toArray(new String[args.size()]));
    }

    /**
     * Get a configured commands with a string array.
     * Parameters of a command are configured with arguments.
     * @param args a command and its arguments.
     * @throws Exception A argument parser may throw Exception.
     * @return A configured command instance
     */
    public ManagedCommand getConfiguredCommandInstance(String[] args) throws Exception {
        ManagedCommand command = commandManager.getCommandInstance(args[0]);
        String[] commandArgs = new String[args.length-1];
        System.arraycopy(args, 1, commandArgs, 0, args.length - 1);
        CmdLineParser parser = new CmdLineParser(command);
        parser.parseArgument(commandArgs);

        if (command instanceof CLIBuiltinCommand) {
            ((CLIBuiltinCommand) command).setCommandConsole(this);
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
        protected CLICommandConsole commandConsole;
    }

    public static class CLIHelpCommand extends CLIBuiltinCommand {

        @Argument
        private String command = null;

        @Override
        public CommandResult execute() throws Exception {
            StringBuilder builder = new StringBuilder();
            if (command == null) {
                System.err.println("Command List:");
                for (String one : commandConsole.getCommandManager().getCommands().keySet()) {
                    builder.append(String.format("   %s\n", one));
                }
                for (String one : new String[]{"exit", "clear", "source"}) {
                    builder.append(String.format("   %s\n", one));
                }
            } else {
                switch (command) {
                    case "exit":
                        builder.append("exit : exit this program\n");
                        break;
                    case "clear":
                        builder.append("clear : clear screen\n");
                        break;
                    case "source":
                        builder.append("source FILE: load script\n");
                        break;
                    default:
                        CmdLineParser parser = new CmdLineParser(commandConsole.getCommandManager().getCommandInstance(command));
                        builder.append(command).append(" ");

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        PrintStream ps = new PrintStream(outputStream);
                        parser.printSingleLineUsage(ps);
                        ps.println();
                        parser.printUsage(ps);
                        builder.append(outputStream.toString("utf-8"));
                }
            }
            return new CommandResult(builder.toString(), CommandResult.ResultState.SUCCESS);
        }

        @Override
        public List<String> getCandidateForArgument(int index) {
            if (index == 0) {
                ArrayList<String> list = new ArrayList<>();
                list.addAll(commandConsole.getCommandManager().getCommands().keySet());
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
        public CommandResult execute() throws Exception {
            commandConsole.loadScript(new FileReader(source));
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }
}
