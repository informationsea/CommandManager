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

import org.junit.Assert;
import org.junit.Test;

public class CommandResultTest {

    @Test
    public void test1() throws Exception{
        CommandResult commandResult = new CommandResult("Hello, world\nTake two", CommandResult.ResultState.SUCCESS);
        Assert.assertEquals("Hello, world\nTake two", commandResult.getResult());
        Assert.assertEquals(CommandResult.ResultState.SUCCESS, commandResult.getState());

        commandResult = new CommandResult("Warn", CommandResult.ResultState.WARN);
        Assert.assertEquals("Warn", commandResult.getResult());
        Assert.assertEquals(CommandResult.ResultState.WARN, commandResult.getState());

        commandResult = new CommandResult("Warn", CommandResult.ResultState.ERROR);
        Assert.assertEquals("Warn", commandResult.getResult());
        Assert.assertEquals(CommandResult.ResultState.ERROR, commandResult.getState());
    }

    @Test
    public void test2() throws Exception {
        CommandResult commandResult = new CommandResult("Result 1", CommandResult.ResultState.SUCCESS);
        Assert.assertEquals("Result 1", commandResult.getResult());
        Assert.assertEquals(CommandResult.ResultState.SUCCESS, commandResult.getState());

        commandResult.appendCommandResult(new CommandResult("Result 2", CommandResult.ResultState.WARN));
        Assert.assertEquals("Result 1\n-----\nResult 2", commandResult.getResult());
        Assert.assertEquals(CommandResult.ResultState.WARN, commandResult.getState());
    }
}