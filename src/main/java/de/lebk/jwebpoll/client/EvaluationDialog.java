package de.lebk.jwebpoll.client;

import com.j256.ormlite.dao.ForeignCollection;
import de.lebk.jwebpoll.data.Poll;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Created by lostincoding on 18.05.16.
 */
public class EvaluationDialog {
    public static void show(Poll poll) {

        Stage evaluationStage = new Stage(StageStyle.UTILITY);
        evaluationStage.setTitle("Auswertung: "+poll.getTitle());

        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            Accordion questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");
          /*  Label confirmMsg = (Label) confirmGrid.lookup("#confirmMsg");
            confirmMsg.setText(msg);

            Button okBtn = (Button) confirmGrid.lookup("#okBtn");
            okBtn.setOnAction((ActionEvent ev) ->
            {
                callback.confirm();
                confirmStage.close();
            });*/
        } catch (IOException ex) {

            return;
        }



        evaluationStage.setScene(new Scene(evaluationGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }
}
