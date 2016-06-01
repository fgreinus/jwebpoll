package de.lebk.jwebpoll.client;

import com.j256.ormlite.dao.ForeignCollection;
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
import java.util.ArrayList;

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
                    //double average = getAverage(weightedPollVal, voteCountTotal);

                    //System.out.println("  " + answer.getText() + ": " + weightedPollVal + " (weighted: " + weightedPollVal * answer.getValue() + ")");

                    //" | " + " arithmetic avg.: " + average + " | " + " standard deviation: " + getStandardDeviation(question.getAnswers(), average) +
                }
                double arithmeticAverage = (double)weightedPollTotal/(double)weightedPollCount;
                System.out.println("Arithmetic average: " + arithmeticAverage);
                double variance = getVarianz(question.getAnswers(), arithmeticAverage);
                System.out.println("Variance: " + variance);
                double standardDeviation = getStandardDeviation(question.getAnswers(), arithmeticAverage);
                System.out.println("Standard deviation: " + standardDeviation);
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

    public static double getAverage(int voteCount, int voteCountTotal) {
        return (double)voteCount/(double)voteCountTotal;
    }

    public static double getStandardDeviation(ForeignCollection<Answer> answers, double average) {
        return Math.sqrt(getVarianz(answers, average));
    }

    public static double getVarianz(ForeignCollection<Answer> answers, double average) {
        double voteCountTotal = 0;
        for (Answer answer : answers) {
            voteCountTotal += (double)answer.getVotes().size();
        }
        double sum = 0.0;
        for (Answer answer : answers) {
            if (answer.getVotes().size() > 0) {
                double answerValue = (double)answer.getValue();
                double termA = answerValue - average;
                double varianceElement = Math.pow(termA, 2);
                sum += varianceElement * (double)answer.getVotes().size();
            }
        }
        return (double)sum / (double)voteCountTotal;
    }
}
