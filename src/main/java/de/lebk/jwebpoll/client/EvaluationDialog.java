package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class EvaluationDialog {
    public static void show(Poll poll) {

        Stage evaluationStage = new Stage();
        evaluationStage.getIcons().add(new Image(EvaluationDialog.class.getResource("/icon.png").toString()));
        evaluationStage.setTitle("Auswertung: " + poll.getTitle());
//        ScrollPane scroller = new ScrollPane();
//        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scroller.setFitToWidth(true);
//        scroller.setFitToHeight(true);
        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            Accordion questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");

            int voteCountTotal = 0;
            for (Question question : poll.questions) {
                EvaluationQuestionView.setQuestionView(questionsAccordion, question, false);

                for (Answer answer : question.getAnswers()) {
                    voteCountTotal += answer.getVotes().size();
                }

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
            evaluationGrid.setVisible(true);
            questionsAccordion.setVisible(true);
//            scroller.setContent(questionsAccordion);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        evaluationStage.setScene(new Scene(evaluationGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }
}
