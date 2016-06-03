package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.data.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EvaluationDialog {
    public static void show(int pollId) {
        Stage evaluationStage = new Stage();
        evaluationStage.getIcons().add(new Image(EvaluationDialog.class.getResource("/icon.png").toString()));

        Poll poll = null;
        try {
            poll = Database.getInstance().getPollDao().queryForId(pollId);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if(poll == null)
            return;

        evaluationStage.setTitle("Auswertung: " + poll.getTitle());
        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            Accordion questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");
            for (Question question : poll.questions) {
                EvaluationQuestionView.setQuestionView(questionsAccordion, question, false);

                int weightedPollTotal = 0;
                int weightedPollCount = 0;
                for (Answer answer : question.getAnswers()) {
                    weightedPollTotal += answer.getVotes().size() * answer.getValue();
                    if (weightedPollTotal != 0) {
                        weightedPollCount += answer.getVotes().size();
                    }
                }
                double arithmeticAverage = (double) weightedPollTotal / (double) weightedPollCount;
                System.out.println("Arithmetic average: " + arithmeticAverage);
                double variance = Statistics.getVariance(question.getAnswers(), arithmeticAverage);
                System.out.println("Variance: " + variance);
                double standardDeviation = Statistics.getStandardDeviation(question.getAnswers(), arithmeticAverage);
                System.out.println("Standard deviation: " + standardDeviation);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        evaluationStage.setScene(new Scene(evaluationGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }
}
