package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Created by lostincoding on 18.05.16.
 */
public class EvaluationQuestionView {


    public static void setQuestionView(Accordion accordion, Question item, boolean disabled) {
        TitledPane tp = new TitledPane();
        GridPane rootGrid = null;
        TableView<Answer> answerTable = null;
        try {
            rootGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationQuestionView.fxml"));
            answerTable = (TableView<Answer>) rootGrid.lookup("#answerTable");
            TextField titleTxF = (TextField) rootGrid.lookup("#titleTxF");
            tp.setText(item.getTitle());

            switch (item.getType()) {
                case SINGLE:
                    fillForSingleAndMultipleChoice(item,answerTable);
                    break;
                case MULTIPLE:
                    fillForSingleAndMultipleChoice(item,answerTable);
                    break;
                case FREE:
                    fillForFree();
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        rootGrid.setVisible(true);
        tp.setVisible(true);
        answerTable.setVisible(true);
        tp.setContent(rootGrid);
        accordion.getPanes().add(tp);

    }

    private static void fillForSingleAndMultipleChoice(Question item,TableView<Answer> answerTable) {
        for (Answer answer : item.getAnswers()) {
            answerTable.getItems().add(answer);
            //
        }
    }

    private static void fillForFree() {

    }


}
