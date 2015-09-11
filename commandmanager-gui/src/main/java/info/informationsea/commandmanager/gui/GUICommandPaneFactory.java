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

package info.informationsea.commandmanager.gui;

import info.informationsea.commandmanager.core.CommandManager;
import info.informationsea.commandmanager.core.CommandResult;
import info.informationsea.commandmanager.core.ManagedCommand;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command Configuration Pane Factory
 * @author Yasunobu OKAMURA
 */
@AllArgsConstructor @Slf4j
public class GUICommandPaneFactory {

    @Getter
    private CommandManager commandManager;

    /**
     * Get a configuration pane and its properties.
     * An empty {@code Map<String, ObservableValue>}
     * @param commandName a name of command
     * @param option2property an empty Map to store properties
     * @return a configuration pane.
     */
    public Parent getConfigurationDialog(String commandName, Map<String, ObservableValue> option2property) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        CommandManager.OptionInfo optionInfo = commandManager.getOptionInfoForName(commandName);

        int numberOfArguments = optionInfo.getArguments().size();
        for (int i = 0; i < numberOfArguments; i++) {
            OptionHandler optionHandler = optionInfo.getArguments().get(i);
            //log.info("add {} {} {}", i, gridPane, optionHandler);
            option2property.put("arg" + i, addOption(gridPane, optionHandler, i));
        }

        int position = numberOfArguments;
        for (Map.Entry<String, OptionHandler> entry : optionInfo.getOptions().entrySet()) {
            //log.info("{} {}", commandName, entry.getValue().getClass());
            option2property.put(entry.getKey(), addOption(gridPane, entry.getValue(), position++));
        }

        return gridPane;
    }

    /**
     * Get a configuration pane with the run button.
     * @param commandName a name of command
     * @return a TitledPane to configure and run a command.
     */
    public Parent getCommandPane(String commandName) {
        HBox buttonBox = new HBox();
        Button runButton = new Button("Run");
        buttonBox.getChildren().add(runButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        final HashMap<String, ObservableValue> option2property = new HashMap<>();

        VBox vBox = new VBox(
                getConfigurationDialog(commandName, option2property),
                buttonBox
        );

        runButton.setOnMouseClicked(e -> {
            ManagedCommand command = commandManager.getCommandInstance(commandName);
            CmdLineParser parser = new CmdLineParser(command);
            List<OptionHandler> arguments = parser.getArguments();
            try {
                for (int i = 0; i < arguments.size(); i++) {
                    Object optionValue = option2property.get("arg"+i).getValue();
                    arguments.get(i).setter.addValue(convertProperObject(arguments.get(i), optionValue));
                }
                for (OptionHandler handler : parser.getOptions()) {
                    Object optionValue = option2property.get(handler.option.toString()).getValue();
                    handler.setter.addValue(convertProperObject(handler, optionValue));
                }

                CommandResult result = command.execute();

                Alert.AlertType type = Alert.AlertType.ERROR;
                String headerText = "";
                switch (result.getState()) {
                    case SUCCESS:
                        type = Alert.AlertType.INFORMATION;
                        headerText = "Command executed successfully";
                        break;
                    case WARN:
                        type = Alert.AlertType.WARNING;
                        headerText = "Command executed with warning";
                        break;
                    case ERROR:
                    default:
                        type = Alert.AlertType.ERROR;
                        headerText = "Command executed with error";
                        break;
                }

                Alert alert = new Alert(type);
                alert.setHeaderText(headerText);

                if (result.getResult() == null || result.getResult().length() < 300) {
                    alert.setContentText(result.getResult());

                } else {
                    alert.setContentText(result.getResult().substring(0, 300)+"...");
                    TextArea textArea = new TextArea(result.getResult());
                    alert.getDialogPane().setExpandableContent(textArea);
                }

                alert.show();
            } catch (Exception e1) {
                e1.printStackTrace();
                showExceptionAlert("Failed to run", e1);
            }
        });

        return vBox;
    }

    private Object convertProperObject(OptionHandler handler, Object optionValue) {
        if (handler instanceof FileOptionHandler)
            optionValue = new File(optionValue.toString());
        else if (handler instanceof EnumOptionHandler)
            optionValue = Enum.valueOf(handler.setter.getType(), optionValue.toString());
        else if (handler instanceof DoubleOptionHandler)
            optionValue = Double.parseDouble(optionValue.toString());
        else if (handler instanceof IntOptionHandler)
            optionValue = Integer.parseInt(optionValue.toString());
        return optionValue;
    }


    private ObservableValue addOption(GridPane gridPane, OptionHandler optionHandler, int position) {
        gridPane.add(new Label(optionHandler.option.usage()), 0, position);

        if (optionHandler instanceof FileOptionHandler) {
            TextField textField = new TextField(optionHandler.printDefaultValue());
            Button selectButton = new Button("Select...");
            selectButton.setOnMouseClicked(e -> {
                FileChooser fileChooser = new FileChooser();
                String usage = optionHandler.option.usage();
                fileChooser.setTitle(usage);
                File file = null;

                if (usage.toLowerCase().contains("save") || usage.toLowerCase().contains("store")) {
                    file = fileChooser.showSaveDialog(null);
                } else {
                    file = fileChooser.showOpenDialog(null);
                }

                if (file != null)
                    textField.setText(file.getAbsolutePath());
            });

            gridPane.add(textField, 1, position);
            gridPane.add(selectButton, 2, position);
            return textField.textProperty();
        }

        if (optionHandler instanceof BooleanOptionHandler) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(optionHandler.printDefaultValue().equals("true"));
            gridPane.add(checkBox, 1, position);
            return checkBox.selectedProperty();
        }

        // disable following code due to unexpected behavior of Spinner
        /*
        if (optionHandler instanceof IntOptionHandler) {
            Spinner<Integer> spinner = new Spinner<>();
            SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.parseInt(optionHandler.printDefaultValue()));
            spinner.setValueFactory(factory);
            spinner.setEditable(true);
            gridPane.add(spinner, 1, position);
            return spinner.valueProperty();
        }

        if (optionHandler instanceof DoubleOptionHandler) {
            Spinner<Double> spinner = new Spinner<>();
            SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(Double.MIN_VALUE, Double.MAX_VALUE, Double.parseDouble(optionHandler.printDefaultValue()));
            spinner.setValueFactory(factory);
            spinner.setEditable(true);
            gridPane.add(spinner, 1, position);
            return spinner.valueProperty();
        }
        */

        if (optionHandler instanceof EnumOptionHandler) {
            ChoiceBox<String> choiceBox = new ChoiceBox<>();
            List<String> candidates = CommandManager.OptionInfo.candidateOptions(optionHandler);
            if (candidates != null) {
                choiceBox.getItems().addAll(candidates);
                choiceBox.getSelectionModel().select(optionHandler.printDefaultValue());
                gridPane.add(choiceBox, 1, position);
                return choiceBox.getSelectionModel().selectedItemProperty();
            }
        }

        TextField textField = new TextField(optionHandler.printDefaultValue());
        gridPane.add(textField, 1, position);
        return textField.textProperty();
    }

    public static void showExceptionAlert(String headerText, Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(headerText);

        alert.setContentText(th.getMessage());

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }
}
