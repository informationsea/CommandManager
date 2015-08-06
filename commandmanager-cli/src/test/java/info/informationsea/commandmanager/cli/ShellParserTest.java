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

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class ShellParserTest {

    @Test
    public void testParseShellLine() throws Exception {
        Assert.assertArrayEquals(new String[]{"hello", "world"}, ShellParser.parseShellLine("hello world").toArray());
        Assert.assertArrayEquals(new String[]{"hello", "world"}, ShellParser.parseShellLine("hello  world").toArray());
        Assert.assertArrayEquals(new String[]{"hello", "world"}, ShellParser.parseShellLine("hello \n world").toArray());
        Assert.assertArrayEquals(new String[]{"hello world"}, ShellParser.parseShellLine("\"hello world\"").toArray());
        Assert.assertArrayEquals(new String[]{"--hello world"}, ShellParser.parseShellLine("--\"hello world\"").toArray());
        Assert.assertArrayEquals(new String[]{"hello \"world"}, ShellParser.parseShellLine("\"hello \\\"world\"").toArray());
        Assert.assertArrayEquals(new String[]{"hello world"}, ShellParser.parseShellLine("hello\\ world").toArray());
        Assert.assertArrayEquals(new String[]{"--hello=world !!!"}, ShellParser.parseShellLine("--hello=\"world !!!\"").toArray());
        Assert.assertArrayEquals(new String[]{"hello", "world"}, ShellParser.parseShellLine("hello world ").toArray());
        Assert.assertArrayEquals(new String[]{"hello", "world"}, ShellParser.parseShellLine(" hello world").toArray());


        // with position
        Assert.assertArrayEquals(new ShellParser.ArgumentAndPosition[]{
                new ShellParser.ArgumentAndPosition(1, "hello"),
                new ShellParser.ArgumentAndPosition(8, "world")
        }, ShellParser.parseShellLineWithPosition(" hello  world").toArray());
    }
}