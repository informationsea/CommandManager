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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CLICommandConsoleTest {

    private CommandManager commandManager = null;
    private CLICommandConsole commandConsole = null;
    private Context context = null;

    @Before
    public void setup() {
        commandManager = new CommandManager();
        commandConsole = new CLICommandConsole(commandManager);
        commandManager.addCommand("1", Command1.class);
        commandManager.addCommand("2", Command2.class);
        context = new Context();
        commandManager.setContext(context);
    }

    @Test
    public void testExecute() throws Exception {
        Command1 command1 = (Command1) commandConsole.getConfiguredCommandInstance("1 -a 4 -v hello");
        Assert.assertEquals(4, command1.getA());
        Assert.assertTrue(command1.isV());
        Assert.assertEquals("hello", command1.getS());

        commandConsole.execute("1 -a 4 -v hello");
        Assert.assertEquals("4", context.map.get("a"));
        Assert.assertEquals("true", context.map.get("v"));
        Assert.assertEquals("hello", context.map.get("s"));

        commandConsole.execute(new String[]{"1", "hello!"});
        Assert.assertEquals("hello!", context.map.get("s"));
    }

    @Test
    public void testExecuteMany() throws Exception {
        commandConsole.executeMany("1 test ; 2");
        Assert.assertEquals("test", context.map.get("s"));
        Assert.assertEquals("true", context.map.get("2"));
    }

    @Test
    public void testLoadScript() throws Exception {
        commandConsole.loadScript(new InputStreamReader(getClass().getResourceAsStream("samplerun.txt")));
        Assert.assertEquals("23", context.map.get("a"));
        Assert.assertEquals("hello", context.map.get("s"));
        Assert.assertEquals("true", context.map.get("2"));
    }

    @Test
    public void testHelp() throws Exception {
        commandConsole.execute("help");
        commandConsole.execute("help 1");
    }

    @NoArgsConstructor
    public static class Command1 implements ManagedCommand {

        @Option(name = "-a", help = true, usage = "A NUMBER") @Getter
        int a = 0;

        @Option(name = "-v", usage = "boolean") @Getter
        boolean v = false;

        @Argument(index = 0, usage = "sample arg") @Getter
        String s = "default";

        private Context context = null;

        @Override
        public CommandResult execute() {
            //log.info("execute {} {}", a, v);
            context.map.put("a", String.valueOf(a));
            context.map.put("v", String.valueOf(v));
            context.map.put("s", s);
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }

        @Override
        public void setContext(Object context) {
            this.context = (Context) context;
        }
    }

    public static class Command2 implements ManagedCommand {

        private Context context;

        @Override
        public CommandResult execute() throws Exception {
            context.map.put("2", "true");
            return new CommandResult(null, CommandResult.ResultState.SUCCESS);
        }

        @Override
        public void setContext(Object context) {
            this.context = (Context) context;
        }
    }

    private class Context {
        public Map<String, String> map = new HashMap<>();
    }
}