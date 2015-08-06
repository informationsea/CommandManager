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
import org.junit.Assert;
import org.junit.Test;

public class CommandManagerTest {

    @Test
    public void testCommandManager() throws Exception {
        CommandManager commandManager = new CommandManager();
        commandManager.addCommand("1", TestCommand1.class);
        commandManager.addCommand("2", TestCommand2.class);

        Assert.assertEquals(TestCommand1.class, commandManager.getCommandForName("1"));
        Assert.assertEquals(TestCommand2.class, commandManager.getCommandForName("2"));
        Assert.assertEquals(2, commandManager.getCommands().size());

        Assert.assertEquals(TestCommand1.class, commandManager.getCommandInstance("1").getClass());
        Assert.assertEquals(TestCommand2.class, commandManager.getCommandInstance("2").getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommandManager2() throws Exception {
        CommandManager commandManager = new CommandManager();
        commandManager.addCommand("1", TestCommand1.class);
        commandManager.addCommand("1", TestCommand3.class);
    }

    public static class TestCommand1 implements ManagedCommand {
        @Getter
        String commandName = "hello";

        @Override
        public void execute() {}
    }

    public static class TestCommand2 implements ManagedCommand {
        @Getter
        String commandName = "hello2";

        @Override
        public void execute() {}
    }

    public static class TestCommand3 implements ManagedCommand {
        @Getter
        String commandName = "hello";

        @Override
        public void execute() {}
    }
}