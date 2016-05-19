package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Question;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Created by lostincoding on 18.05.16.
 */
public class EvaluationQuestionView {


    public static void setQuestionView(Accordion accordion, Question item, boolean disabled) {
        try {
            TitledPane tp = new TitledPane();
            GridPane rootGird = null;

            rootGird = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationQuestionView.fxml"));

            TextField titleTxF = (TextField) rootGird.lookup("#titleTxF");
            tp.setText(item.getTitle());

            rootGird.setVisible(true);
            tp.setVisible(true);
            tp.setContent(rootGird);
            accordion.getPanes().add(tp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
