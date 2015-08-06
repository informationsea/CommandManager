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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parse shell line.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) @Slf4j
public class ShellParser {

    private enum ParserState {
        NORMAL, SPACE, IN_QUOTE, ESCAPE, IN_QUOTE_ESCAPE
    }

    /**
     * Split string and its original position
     */
    @Value
    public static class ArgumentAndPosition {
        private int position;
        private String arg;
    }

    /**
     * Split line to components with shell line manner.
     * @param line a shell line
     * @return split string array of the line
     */
    public static List<String> parseShellLine(@NonNull String line) {
        return parseShellLineWithPosition(line).stream().map(ArgumentAndPosition::getArg).collect(Collectors.toList());
    }

    /**
     * Split line to components with shell line manner.
     * @param line  a shell line.
     * @return split array of string and its positions.
     */
    public static List<ArgumentAndPosition> parseShellLineWithPosition(@NonNull String line) {
        List<ArgumentAndPosition> result = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        boolean shouldAdd = false;
        ParserState state = ParserState.NORMAL;
        int position = 0;
        int start = -1;
        for (char ch : line.toCharArray()) {
            switch (state) {
                case NORMAL:
                    if (Character.isWhitespace(ch)) {
                        state = ParserState.SPACE;
                        if (shouldAdd) {
                            result.add(new ArgumentAndPosition(start, current.toString()));
                            start = -1;
                        }
                        current = new StringBuilder();
                        shouldAdd = false;
                    } else if (ch == '"') {
                        state = ParserState.IN_QUOTE;
                        shouldAdd = true;
                        if (start == -1) start = position;
                    } else if (ch == '\\') {
                        state = ParserState.ESCAPE;
                        if (start == -1) start = position;
                        shouldAdd = true;
                    } else {
                        current.append(ch);
                        if (start == -1) start = position;
                        shouldAdd = true;
                    }
                    break;
                case SPACE:
                    if (ch == '"') {
                        state = ParserState.IN_QUOTE;
                        start = position;
                        shouldAdd = true;
                    } else if (ch == '\\') {
                        state = ParserState.ESCAPE;
                        start = position;
                        shouldAdd = true;
                    } else if (Character.isWhitespace(ch)) {
                        // do nothing
                    } else {
                        current.append(ch);
                        state = ParserState.NORMAL;
                        start = position;
                        shouldAdd = true;
                    }
                    break;
                case IN_QUOTE:
                    if (ch == '"') {
                        state = ParserState.NORMAL;
                    } else if (ch == '\\') {
                        state = ParserState.IN_QUOTE_ESCAPE;
                    } else {
                        current.append(ch);
                    }

                    break;
                case ESCAPE:
                case IN_QUOTE_ESCAPE:
                    current.append(ch);
                    if (state.equals(ParserState.ESCAPE)) {
                        state = ParserState.NORMAL;
                    } else {
                        state = ParserState.IN_QUOTE;
                    }
                    break;
            }
            position += 1;
        }

        if (shouldAdd)
            result.add(new ArgumentAndPosition(start, current.toString()));

        return result;
    }
}
