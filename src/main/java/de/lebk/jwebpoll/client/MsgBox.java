package de.lebk.jwebpoll.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.*;

import java.io.IOException;

public class MsgBox {
    public static void show(String title, String msg, MsgBoxCallback callback, Window owner) {
        Stage msgStage = new Stage(StageStyle.UTILITY);
        msgStage.initModality(Modality.WINDOW_MODAL);
        msgStage.initOwner(owner);
        msgStage.setTitle(title);

        GridPane confirmGrid;
        try {
            confirmGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/msgbox.fxml"));

            Label confirmMsg = (Label) confirmGrid.lookup("#confirmMsg");
            confirmMsg.setText(msg);

            Button okBtn = (Button) confirmGrid.lookup("#okBtn");
            okBtn.setOnAction((ActionEvent ev) ->
            {
                if (callback != null)
                    callback.confirm();
                msgStage.close();
            });
        } catch (IOException ex) {
            if (callback != null)
                callback.confirm();
            return;
        }

        msgStage.setOnCloseRequest((WindowEvent we) ->
        {
            if (callback != null)
                callback.confirm();
        });

        msgStage.setScene(new Scene(confirmGrid));
        msgStage.sizeToScene();
        msgStage.show();
    }
}
