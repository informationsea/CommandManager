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

/**
 * Result of a command execution.
 * @author Yasunobu OKAMURA
 */
public class CommandResult {

    public enum ResultState {
        SUCCESS,
        WARN,
        ERROR
    }

    @Getter
    private String result = null;

    @Getter
    private ResultState state;

    public CommandResult(String result, ResultState state) {
        this.result = result;
        this.state = state;
    }

    public void appendCommandResult(CommandResult commandResult) {
        switch (state) {
            case SUCCESS:
                state = commandResult.getState();
                break;
            case WARN:
                if (commandResult.getState() != ResultState.SUCCESS)
                    state = commandResult.getState();
                break;
        }

        if (commandResult.getResult() != null) {
            if (result != null)
                result += "\n-----\n" + commandResult.getResult();
            else
                result = commandResult.getResult();
        }
    }
}
