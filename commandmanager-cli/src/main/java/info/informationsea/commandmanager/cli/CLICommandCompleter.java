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
import jline.console.completer.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.FileOptionHandler;
import org.kohsuke.args4j.spi.OptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Command Completer for JLine
 * @author Yasunobu OKAMURA
 */
@Slf4j
public class CLICommandCompleter implements Completer {

    @Getter
    private CLICommandManager commandManager;

    private Map<String, StringsCompleter> optionCompleter = new HashMap<>();
    private StringsCompleter firstCommandCompleter;

    public CLICommandCompleter(CLICommandManager manager) {
        commandManager = manager;

        Map<String, Class> map = commandManager.getCommands();
        for (Map.Entry<String, Class> entry : map.entrySet()) {

            CommandManager.OptionInfo optionInfo = commandManager.getOptionInfoForName(entry.getKey());
            optionCompleter.put(entry.getKey(), new StringsCompleter(optionInfo.getOptions().values().stream().map(o -> o.option.toString()).toArray(String[]::new)));
        }

        //log.info("keys {}", (Object)map.keySet().stream().toArray(String[]::new));
        firstCommandCompleter = new StringsCompleter(map.keySet().stream().toArray(String[]::new));
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        List<ShellParser.ArgumentAndPosition> shellParsed = ShellParser.parseShellLineWithPosition(buffer.substring(0, cursor));
        if (buffer.length() > 0 && Character.isWhitespace(buffer.charAt(buffer.length() - 1)))
            shellParsed.add(new ShellParser.ArgumentAndPosition(buffer.length(), ""));

        if (shellParsed.size() == 0) { // buffer is empty
            return firstCommandCompleter.complete("", 0, candidates);
        } else  if (shellParsed.size() == 1) { // complete command name
            return firstCommandCompleter.complete(shellParsed.get(0).getArg(), cursor, candidates);
        }

        // complete command options
        CommandManager.OptionInfo info = commandManager.getOptionInfoForName(shellParsed.get(0).getArg());
        ShellParser.ArgumentAndPosition lastComponent = shellParsed.get(shellParsed.size()-1);

        if (shellParsed.size() > 2) {
            ShellParser.ArgumentAndPosition oneBeforeLast = shellParsed.get(shellParsed.size() - 2);
            OptionHandler oh = info.getOptions().get(oneBeforeLast.getArg());
            if (oh != null && ! (oh instanceof BooleanOptionHandler)) {

                Completer c = new NullCompleter();

                if (oh instanceof FileOptionHandler) {
                    c = new FileNameCompleter();
                } else {
                    ManagedCommand command = commandManager.getCommandInstance(shellParsed.get(0).getArg());
                    List<String> optionCandidates = command.getCandidateForOption(oneBeforeLast.getArg());
                    //log.info("optionCandidates {} for {}", optionCandidates, oneBeforeLast.getArg());
                    if (optionCandidates != null) {
                        c = new StringsCompleter(optionCandidates);
                    }
                }

                return c.complete(lastComponent.getArg(), cursor - lastComponent.getPosition(), candidates) + lastComponent.getPosition();
            }
        }

        int argIndex = -1;
        for (int i = 1; i < shellParsed.size(); i++) {
            ShellParser.ArgumentAndPosition one = shellParsed.get(i);
            OptionHandler oh = info.getOptions().get(one.getArg());
            if (oh != null && ! (oh instanceof BooleanOptionHandler)) {
                argIndex -= 1;
            } else if (oh == null) {
                argIndex += 1;
            }
        }
        //log.info("arg index {}", argIndex);

        Completer c = new NullCompleter();
        try {
            if (info.getArguments().get(argIndex) instanceof FileOptionHandler) {
                c = new FileNameCompleter();
            } else {
                ManagedCommand command = commandManager.getCommandInstance(shellParsed.get(0).getArg());

                if (command instanceof CLICommandManager.CLIBuiltinCommand) {
                    ((CLICommandManager.CLIBuiltinCommand) command).setCommandManager(commandManager);
                }

                List<String> argumentCandidates = command.getCandidateForArgument(argIndex);
                if (argumentCandidates != null) {
                    c = new StringsCompleter(argumentCandidates);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // ignore
        }

        int pos = new AggregateCompleter(optionCompleter.get(shellParsed.get(0).getArg()), c).
                complete(lastComponent.getArg(), cursor - lastComponent.getPosition(), candidates) + lastComponent.getPosition();

        for (ShellParser.ArgumentAndPosition one : shellParsed.subList(1, shellParsed.size())) {
            //log.info("remove ! {} {} {}", candidates, one, candidates.contains(one.getArg()));
            candidates.remove(one.getArg());
            candidates.remove(one.getArg() + " ");
        }
        //log.info("removed {}", candidates);

        return pos;
    }

}
