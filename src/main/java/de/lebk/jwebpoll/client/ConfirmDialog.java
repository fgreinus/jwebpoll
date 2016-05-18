package de.lebk.jwebpoll.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ConfirmDialog {
    public static void show(String msg, ConfirmCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback cannot be null.");

        Stage confirmStage = new Stage(StageStyle.UTILITY);
        confirmStage.setTitle("Bestätigen");

        GridPane confirmGrid;
        Label confirmMsg;
        Button yesBtn, noBtn;
        try {
            confirmGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/confirm.fxml"));

            confirmMsg = (Label) confirmGrid.lookup("#confirmMsg");
            confirmMsg.setText(msg);

            yesBtn = (Button) confirmGrid.lookup("#yesBtn");
            yesBtn.setOnAction((ActionEvent ev) ->
            {
                callback.confirm(true);
                confirmStage.close();
            });
            noBtn = (Button) confirmGrid.lookup("#noBtn");
            noBtn.setOnAction((ActionEvent ev) ->
            {
                callback.confirm(false);
                confirmStage.close();
            });

        } catch (IOException ex) {
            callback.confirm(false);
            return;
        }

        confirmStage.setOnCloseRequest((WindowEvent we) ->
        {
            callback.confirm(false);
        });

        confirmStage.setScene(new Scene(confirmGrid));
        confirmStage.sizeToScene();
        confirmStage.show();

        noBtn.requestFocus();
    }
}
