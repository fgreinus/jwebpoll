package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.client.ConfirmDialog;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class EvaluationExtendedStats {

    public static void show(Poll poll) {
        Stage statsStage = new Stage();
        //statsStage.setTitle("Auswertung: " + poll.getTitle());
        GridPane statsGrid = null;
        Accordion statsAccordion = null;
        try {
            statsGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationExtendedStats.fxml"));
            statsAccordion = (Accordion) statsGrid.lookup("#statAccordion");
        } catch (IOException io) {
            io.printStackTrace();
        }

        for(Question question:poll.getQuestions()) {
            EvaluationExtendedStatsQuestionView.setQuestionView(statsAccordion,question,false);
        }

        statsStage.setScene(new Scene(statsGrid));
        statsStage.sizeToScene();
        statsStage.show();
    }


}
