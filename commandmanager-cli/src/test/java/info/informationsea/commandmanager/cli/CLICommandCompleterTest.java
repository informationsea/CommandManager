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

import info.informationsea.commandmanager.core.ManagedCommand;
import jline.console.completer.Completer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class CLICommandCompleterTest {

    private CLICommandManager commandManager = null;

    @Before
    public void setup() {
        commandManager = new CLICommandManager();
        commandManager.addCommand("access", Command1.class);
        commandManager.addCommand("acacia", Command2.class);
    }

    @Test
    public void testComplete() throws Exception {
        CLICommandCompleter completer = new CLICommandCompleter(commandManager);
        assertCompleter(Arrays.<CharSequence>asList("acacia", "access"), 0, completer, "aca", 2);
        assertCompleter(Arrays.<CharSequence>asList("acacia", "access", "help", "source"), 0, completer, "", 0);

        assertCompleter(Arrays.<CharSequence>asList("-a", "-method", "-output", "-v"), 7, completer, "acacia -", 8);
        assertCompleter(Arrays.<CharSequence>asList("-a", "-method", "-output"), 10, completer, "acacia -v -", 11);

        assertCompleter(Arrays.asList("TEST1", "TEST2", "TEST3"), 15, completer, "acacia -method ", 15);
        assertCompleter(Arrays.asList("argument1", "argument2"), 7, completer, "acacia a", 8);
    }

    public void assertCompleter(List<CharSequence> expectedList, int expectedPosition, Completer completer, String buffer, int cursor) {
        ArrayList<CharSequence> list = new ArrayList<>();
        Assert.assertEquals(expectedPosition, completer.complete(buffer, cursor, list));
        log.info("completed {}", list);
        Assert.assertArrayEquals(expectedList.toArray(), list.toArray());
    }

    @NoArgsConstructor
    public static class Command1 implements ManagedCommand {

        @Option(name = "-a") @Getter
        int a = 0;

        @Option(name = "-v") @Getter
        boolean v = false;

        @Argument(index = 0) @Getter
        String s = "default";

        @Override
        public void execute() {
            log.info("execute {} {}", a, v);
        }
    }

    public enum TestEnum {
        TEST1,
        TEST2,
        TEST3
    }

    @NoArgsConstructor
    public static class Command2 implements ManagedCommand {

        @Option(name = "-a") @Getter
        int a = 0;

        @Option(name = "-v") @Getter
        boolean v = false;

        @Option(name = "-method") @Getter
        TestEnum method = TestEnum.TEST1;

        @Option(name = "-output") @Getter
        File file = null;

        @Argument(index = 0) @Getter
        String s = "default";

        @Override
        public void execute() {
            log.info("execute {} {}", a, v);
        }

        @Override
        public List<String> getCandidateForOption(String name) {
            switch (name) {
                case "-v":
                    return Arrays.asList("1", "2");
                case "-method":
                    return Arrays.asList("TEST1", "TEST2", "TEST3");
            }
            return null;
        }

        @Override
        public List<String> getCandidateForArgument(int index) {
            switch (index) {
                case 0:
                    return Arrays.asList("argument1", "argument2");
            }
            return null;
        }
    }
}