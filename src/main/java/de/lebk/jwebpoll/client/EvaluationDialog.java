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

            for (Question question : poll.questions) {

                if (question.getType() == QuestionType.FREE) {
                    try {
                        Answer freeAnswer = Database.getInstance().getAnswerDao().queryBuilder().where().eq("question_id", question.getId()).queryForFirst();
                        List<Vote> voteList = Database.getInstance().getVoteDao().queryBuilder().where().eq("question_id", question.getId()).and().isNull("answer_id").query();
                        for (Vote vote : voteList) {
                            vote.setAnswer(freeAnswer);
                            Database.getInstance().getVoteDao().update(vote);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            try {
                poll = Database.getInstance().getPollDao().queryForId(poll.getId());
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

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
