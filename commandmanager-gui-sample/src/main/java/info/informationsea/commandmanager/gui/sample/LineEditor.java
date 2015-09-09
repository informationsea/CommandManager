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

package info.informationsea.commandmanager.gui.sample;

import info.informationsea.commandmanager.core.CommandManager;
import info.informationsea.commandmanager.core.CommandResult;
import info.informationsea.commandmanager.core.ManagedCommand;
import info.informationsea.commandmanager.gui.GUICommandPaneFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sample GUI Line Editor.
 * @author OKAMURA Yasunobu
 */
public class LineEditor extends Application {

    private CommandManager commandManager = new CommandManager();
    private GUICommandPaneFactory factory = new GUICommandPaneFactory(commandManager);

    public static void main(String ...args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        commandManager.addCommand("load", Load.class);
        commandManager.addCommand("insert", Insert.class);
        commandManager.addCommand("replace", Replace.class);
        commandManager.addCommand("save", Save.class);
        commandManager.addCommand("print", Print.class);
        commandManager.setContext(new LineEditorContext());

        Accordion accordion = new Accordion();
        for (Map.Entry<String, Class> entry : commandManager.getCommands().entrySet()) {
            accordion.getPanes().add(new TitledPane(entry.getKey(), factory.getCommandPane(entry.getKey())));
        }

        accordion.setPrefSize(800, 600);
        Scene scene = new Scene(accordion);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Line Editor Sample");
        primaryStage.show();
    }

    private static class LineEditorContext {
        public List<String> lines = new ArrayList<>();
    }

    public abstract static class LineEditorCommand implements ManagedCommand {
        protected List<String> lines = null;

        @Override
        public void setContext(Object context) {
            this.lines = ((LineEditorContext) context).lines;
        }
    }

    @Slf4j
    public static class Load extends LineEditorCommand {

        @Argument(required = true, usage = "A file to load")
        File file;

        @Override
        public CommandResult execute() throws Exception{
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                lines.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.endsWith("\n"))
                        lines.add(line.substring(0, line.length()-1));
                    else
                        lines.add(line);
                }
            }
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Insert extends LineEditorCommand {
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

    public static class Replace extends LineEditorCommand {

        @Option(name = "-target", required = true, usage = "Replacement target")
        String target;

        @Option(name = "-replacement" ,required = true, usage = "new text")
        String replacement;

        @Option(name = "-regexp", usage = "Use regular expression (dummy)")
        boolean regexp;

        @Override
        public CommandResult execute() throws Exception {
            List<String> newlines = lines.stream().map(s -> s.replace(target, replacement)).collect(Collectors.toList());
            lines.clear();
            lines.addAll(newlines);
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }
    }

    public static class Save extends LineEditorCommand {


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

    public static class Print extends LineEditorCommand {

        @Override
        public CommandResult execute() throws Exception {
            StringBuilder buffer = new StringBuilder();
            lines.forEach(s -> {
                buffer.append(s);
                buffer.append('\n');
            });
            return new CommandResult(buffer.toString(), CommandResult.ResultState.SUCCESS);
        }
    }
}
