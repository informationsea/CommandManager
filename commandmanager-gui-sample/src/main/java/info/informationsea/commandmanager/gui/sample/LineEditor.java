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

package info.informationsea.commandmanager.gui.sample;

import info.informationsea.commandmanager.cli.sample.LineEditorCommands;
import info.informationsea.commandmanager.core.CommandManager;
import info.informationsea.commandmanager.gui.GUICommandPaneFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Sample GUI Line Editor.
 * @author OKAMURA Yasunobu
 */
public class LineEditor extends Application {

    private CommandManager commandManager = new CommandManager();
    private GUICommandPaneFactory factory = new GUICommandPaneFactory(commandManager);

    public static void main(String ...args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LineEditorCommands.registerCommands(commandManager);

        Accordion accordion = new Accordion();
        for (Map.Entry<String, Class> entry : commandManager.getCommands().entrySet()) {
            accordion.getPanes().add(new TitledPane(entry.getKey(), factory.getCommandPane(entry.getKey())));
        }

        accordion.setPrefSize(800, 600);
        Scene scene = new Scene(accordion);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Line Editor Sample");
        primaryStage.show();
    }
}
