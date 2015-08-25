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

import java.util.List;

/**
 * A managed command.
 * Classes implemented this interface should annotate parameters with args4j.
 * CommandManager recognize the annotation, and prepare CLI or GUI for commands.
 * @implNote CommandManager create an instance while creating complete database and completing.
 * Please minimize constructor and setContext method to fast completing.
 * @author Yasunobu OKAMURA
 */
public interface ManagedCommand {
    /**
     * Execute this command.
     * @throws Exception this command may throw Exception
     */
    void execute() throws Exception;

    /**
     * Set a context object by CommandManager
     * @see CommandManager
     * @param context a context object.
     */
    default void setContext(Object context) {}

    default List<String> getCandidateForOption(String name) {return null;}

    default List<String> getCandidateForArgument(int index) {return null;}
}
