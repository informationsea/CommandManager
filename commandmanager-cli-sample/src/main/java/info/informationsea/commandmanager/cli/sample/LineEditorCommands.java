package info.informationsea.commandmanager.cli.sample;

import info.informationsea.commandmanager.core.CommandManager;
import info.informationsea.commandmanager.core.CommandResult;
import info.informationsea.commandmanager.core.ManagedCommand;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CommandManager
 * Copyright (C) 2015 OKAMURA Yasunobu
 * Created on 2015/09/10.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LineEditorCommands {
    public static void registerCommands(CommandManager commandManager) {
        commandManager.addCommand("load", Load.class);
        commandManager.addCommand("convert", Convert.class);
        commandManager.addCommand("insert", Insert.class);
        commandManager.addCommand("replace", Replace.class);
        commandManager.addCommand("save", Save.class);
        commandManager.addCommand("print", Print.class);
        commandManager.setContext(new LineEditorContext());
    }

    public static class LineEditorContext {
        public List<String> lines = new ArrayList<>();
    }

    public abstract static class AbstractLineEditorCommand implements ManagedCommand {
        protected List<String> lines = null;

        @Override
        public void setContext(Object context) {
            this.lines = ((LineEditorContext) context).lines;
        }
    }

    @Slf4j
    public static class Load extends AbstractLineEditorCommand {

        @Argument(required = true, usage = "A file to load")
        File file;

        @Override
        public CommandResult execute() throws Exception{
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                lines.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("loading {}", line);
                    if (line.endsWith("\n"))
                        lines.add(line.substring(0, line.length()-1));
                    else
                        lines.add(line);
                }
            }
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Insert extends AbstractLineEditorCommand {
        @Argument(required = true, usage = "new line content")
        String newline = "";

        @Option(name = "-l", usage = "Insert line number")
        int position = -1;

        @Override
        public CommandResult execute() {
            if (position >= 0) {
                lines.add(position, newline);
            } else {
                lines.add(newline);
            }
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }


    }

    public static class Replace extends AbstractLineEditorCommand {

        @Option(name = "-target", required = true, usage = "Replacement target")
        String target;

        @Option(name = "-replacement" ,required = true, usage = "new text")
        String replacement;

        @Option(name = "-regexp", usage = "Use regular expression")
        boolean regexp;

        @Override
        public CommandResult execute() throws Exception {
            List<String> newlines = lines.stream().map(s -> s.replace(target, replacement)).collect(Collectors.toList());
            lines.clear();
            lines.addAll(newlines);
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Save extends AbstractLineEditorCommand {


        @Argument(required = true, usage = "File to save")
        File file;

        @Override
        public CommandResult execute() throws Exception {
            try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
                for (String one : lines) {
                    w.println(one);
                }
            }
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Print extends AbstractLineEditorCommand {

        @Override
        public CommandResult execute() throws Exception {
            StringBuilder buffer = new StringBuilder();
            lines.forEach(x -> buffer.append(x).append("\n"));
            return new CommandResult(buffer.toString(), CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Convert extends AbstractLineEditorCommand {

        enum ConvertType {LOWER, UPPER, CAPITAL}

        @Argument(required = true, usage = "Convert type")
        private ConvertType convertType;

        @Override
        public CommandResult execute() throws Exception {
            List<String> newList = new ArrayList<>();
            switch (convertType) {
                case LOWER:
                    newList = lines.stream().map(String::toLowerCase).collect(Collectors.toList());
                    break;
                case UPPER:
                    newList = lines.stream().map(String::toUpperCase).collect(Collectors.toList());
                    break;
                case CAPITAL:
                    newList = lines; // dummy
                    break;
            }
            lines.clear();
            lines.addAll(newList);
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }
}
