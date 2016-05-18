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

public class MsgBox
{
    public static void show(String title, String msg, MsgBoxCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("Callback cannot be null.");

        Stage confirmStage = new Stage(StageStyle.UTILITY);
        confirmStage.setTitle(title);

        GridPane confirmGrid;
        try {
            confirmGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/msgbox.fxml"));

            Label confirmMsg = (Label) confirmGrid.lookup("#confirmMsg");
            confirmMsg.setText(msg);

            Button okBtn = (Button) confirmGrid.lookup("#okBtn");
            okBtn.setOnAction((ActionEvent ev) ->
            {
                callback.confirm();
                confirmStage.close();
            });
        } catch (IOException ex) {
            callback.confirm();
            return;
        }

        confirmStage.setOnCloseRequest((WindowEvent we) ->
        {
            callback.confirm();
        });

        confirmStage.setScene(new Scene(confirmGrid));
        confirmStage.sizeToScene();
        confirmStage.show();
    }
}
