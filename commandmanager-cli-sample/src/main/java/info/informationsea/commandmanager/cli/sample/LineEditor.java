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

import info.informationsea.commandmanager.cli.CLICommandManager;
import info.informationsea.commandmanager.core.ManagedCommand;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example of CLICommandManager usage
 */
public class LineEditor {
    public static void main(String ... args) {
        CLICommandManager commandManager = new CLICommandManager();
        commandManager.addCommand("load", Load.class);
        commandManager.addCommand("insert", Insert.class);
        commandManager.addCommand("replace", Replace.class);
        commandManager.addCommand("save", Save.class);
        commandManager.addCommand("print", Print.class);

        try {
            commandManager.startConsole();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> lines = null;

    public static class Load implements ManagedCommand {

        @Argument(required = true, usage = "A file to load")
        File file;

        @Override
        public void execute() throws Exception{
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                lines = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.endsWith("\n"))
                        lines.add(line.substring(0, line.length()-1));
                    else
                        lines.add(line);
                }
            }
        }
    }

    public static class Insert implements ManagedCommand {

        @Argument(required = true, usage = "new line content")
        String newline = "";

        @Option(name = "-l", usage = "Insert line number")
        int position = -1;

        @Override
        public void execute() {
            if (position >= 0) {
                lines.add(position, newline);
            } else {
                lines.add(newline);
            }
        }
    }

    public static class Replace implements ManagedCommand {

        @Option(name = "-target", required = true, usage = "Replacement target")
        String target;

        @Option(name = "-replacement" ,required = true, usage = "new text")
        String replacement;

        @Override
        public void execute() throws Exception {
            lines = lines.stream().map(s -> s.replace(target, replacement)).collect(Collectors.toList());
        }
    }

    public static class Save implements ManagedCommand {

        @Argument(required = true)
        File file;

        @Override
        public void execute() throws Exception {
            try (PrintWriter w = new PrintWriter(new FileWriter(file))) {
                for (String one : lines) {
                    w.println(one);
                }
            }
        }
    }

    public static class Print implements ManagedCommand {

        @Override
        public void execute() throws Exception {

            for (String one : lines) {
                System.out.println(one);
            }
        }
    }
}
