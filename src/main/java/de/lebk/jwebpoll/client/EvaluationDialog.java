package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Created by lostincoding on 18.05.16.
 */
public class EvaluationDialog {
    public static void show(Poll poll) {

        Stage evaluationStage = new Stage(StageStyle.UTILITY);
        evaluationStage.setTitle("Auswertung: " + poll.getTitle());

        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            Accordion questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");
            for (Question question : poll.questions) {
                EvaluationQuestionView.setQuestionView(questionsAccordion, question, false);
                for (Answer answer : question.getAnswers()) {
                    System.out.println("  " + answer.getText() + ": " + answer.getVotes().size());
                }
            }
            evaluationGrid.setVisible(true);
            questionsAccordion.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }


        evaluationStage.setScene(new Scene(evaluationGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }
}
