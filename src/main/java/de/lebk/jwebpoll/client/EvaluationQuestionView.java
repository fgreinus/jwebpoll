package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.ArrayList;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

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
                    fillForSingleAndMultipleChoice(item, answerTable);
                    break;
                case MULTIPLE:
                    fillForSingleAndMultipleChoice(item, answerTable);
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

    private static void fillForSingleAndMultipleChoice(Question item, TableView<Answer> answerTable) {
        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
        typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.75));

        TableColumn<Answer, String> countColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(1);
        countColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("value"));
        countColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

    }

    private static void fillForFree() {

    }


}
