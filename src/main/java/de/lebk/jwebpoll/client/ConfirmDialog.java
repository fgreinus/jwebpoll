package de.lebk.jwebpoll.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.*;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ConfirmDialog {
    final static Logger logger = Logger.getLogger(ConfirmDialog.class);

    public static void show(String msg, ConfirmCallback callback, Window owner) {
        if (callback == null)
            throw new IllegalArgumentException("Callback cannot be null.");

        Stage confirmStage = new Stage(StageStyle.UTILITY);
        confirmStage.initModality(Modality.WINDOW_MODAL);
        confirmStage.initOwner(owner);
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
            if (logger.isDebugEnabled()) {
                logger.debug("", ex);
            }
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
