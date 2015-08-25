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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Properties;

/**
 * Resolve the version of CommandManager
 * @author Yasunobu OKAMURA
 */
@Slf4j
public class CommandManagerVersionResolver {
    @Getter
    private String gitCommit = "UNKNOWN";
    @Getter
    private String mavenVersion = "UNKNOWN";

    /**
     * Resolve a version of CommandManager
     */
    public CommandManagerVersionResolver() {
        this(CommandManager.class);
    }

    /**
     * Resolve a version for clazz
     * @param clazz a class to resolve version.
     */
    public CommandManagerVersionResolver(Class<?> clazz) {
        Properties properties = new Properties();
        try {
            properties.load(clazz.getResourceAsStream("/META-INF/commandmanager/version.properties"));
        } catch (IOException e) {
            log.warn("Cannot load version information for {} ({})", clazz.getCanonicalName(), e.toString());
        }

        gitCommit = properties.getProperty("git.commit");
        mavenVersion = properties.getProperty("version");
    }
}
