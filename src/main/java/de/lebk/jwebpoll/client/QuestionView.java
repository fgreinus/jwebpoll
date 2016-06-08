package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.QuestionType;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class QuestionView {
    final static Logger logger = Logger.getLogger(QuestionView.class);

    public static TitledPane setQuestionView(Accordion accordion, Question question, boolean disabled) {
        try {
            TitledPane tp = new TitledPane();
            GridPane rootGird = FXMLLoader.load(QuestionView.class.getResource("/client/questionView.fxml"));
            TextField titleTxF = (TextField) rootGird.lookup("#titleTxF");
            CheckBox requiredCkB = (CheckBox) rootGird.lookup("#requiredCkB");
            TextField hintTxF = (TextField) rootGird.lookup("#hintTxF");
            ComboBox<QuestionType> typeCbo = (ComboBox<QuestionType>) rootGird.lookup("#typeCbo");
            Text answerAddTextTxt = (Text) rootGird.lookup("#answerAddTextTxt");
            TextField answerAddTextTxF = (TextField) rootGird.lookup("#answerAddTextTxF");
            Text answerAddValueTxt = (Text) rootGird.lookup("#answerAddValueTxt");
            TextField answerAddValueTxF = (TextField) rootGird.lookup("#answerAddValueTxF");
            Button answerAddBtn = (Button) rootGird.lookup("#answerAddBtn");
            TableView<Answer> answerTable = (TableView<Answer>) rootGird.lookup("#answerTable");
            Button answerRemoveBtn = (Button) rootGird.lookup("#answerRemoveBtn");
            TextArea answerFreetext = (TextArea) rootGird.lookup("#answerFreetext");

            titleTxF.setText("#Initialize");
            titleTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
            {
                question.setTitle(titleTxF.getText());

                if (question.getTitle() == null || question.getTitle().isEmpty())
                    tp.setText("<Neue Frage>");
                else
                    tp.setText(question.getTitle());
            });
            titleTxF.setText(question.getTitle());
            titleTxF.setDisable(disabled);
            requiredCkB.setSelected(question.isRequired());
            requiredCkB.setOnAction((ActionEvent event) ->
            {
                question.setRequired(requiredCkB.isSelected());
            });
            requiredCkB.setDisable(disabled);
            hintTxF.setText(question.getHint());
            hintTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
            {
                question.setHint(hintTxF.getText());
            });
            hintTxF.setDisable(disabled);
            typeCbo.getItems().addAll(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.FREE);
            typeCbo.setCellFactory((ListView<QuestionType> param) ->
            {
                return new QuestionTypeListCell();
            });
            typeCbo.setButtonCell(new QuestionTypeListCell());
            typeCbo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends QuestionType> observable, QuestionType oldValue, QuestionType newValue) ->
            {
                if (newValue != oldValue) {
                    question.setType(newValue);
                    switch (newValue) {
                        case SINGLE:
                        case MULTIPLE:
                            answerAddTextTxt.setVisible(true);
                            answerAddTextTxF.setVisible(true);
                            answerAddValueTxt.setVisible(true);
                            answerAddValueTxF.setVisible(true);
                            answerAddBtn.setVisible(true);
                            answerTable.setVisible(true);
                            answerRemoveBtn.setVisible(true);
                            answerFreetext.setVisible(false);
                            break;
                        case FREE:
                            answerAddTextTxt.setVisible(false);
                            answerAddTextTxF.setVisible(false);
                            answerAddValueTxt.setVisible(false);
                            answerAddValueTxF.setVisible(false);
                            answerAddBtn.setVisible(false);
                            answerTable.setVisible(false);
                            answerRemoveBtn.setVisible(false);
                            answerFreetext.setVisible(true);
                            break;
                    }


                    TableColumn<Answer, QuestionType> typeColumn = (TableColumn<Answer, QuestionType>) answerTable.getColumns().get(0);
                    typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.1));
                    typeColumn.setCellValueFactory(new QuestionTypeTableCell(question.getType()));
                    typeColumn.setCellFactory(new Callback<TableColumn<Answer, QuestionType>, TableCell<Answer, QuestionType>>() {
                        @Override
                        public TableCell<Answer, QuestionType> call(TableColumn<Answer, QuestionType> btnCol) {
                            return new TableCell<Answer, QuestionType>() {
                                @Override
                                public void updateItem(QuestionType type, boolean empty) {
                                    super.updateItem(type, empty);

                                    Node graphic = null;
                                    if (type != null && !empty) {
                                        try {
                                            switch (type) {
                                                case SINGLE:
                                                    graphic = FXMLLoader.load(QuestionView.class.getResource("/client/answer_single.fxml"));
                                                    break;
                                                case MULTIPLE:
                                                    graphic = FXMLLoader.load(QuestionView.class.getResource("/client/answer_multiple.fxml"));
                                                    break;
                                            }
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                            if (logger.isDebugEnabled()) {
                                                logger.debug("", ex);
                                            }
                                        }
                                    } else {
                                        setText(null);
                                    }
                                    setGraphic(graphic);
                                }
                            };
                        }
                    });
                }
            });
            typeCbo.setValue(question.getType());
            typeCbo.setDisable(disabled);
            answerAddTextTxF.setDisable(disabled);
            QuestionView.updateAddValueTxF(question, answerAddValueTxF);
            answerAddValueTxF.setDisable(disabled);
            answerAddBtn.setOnAction((ActionEvent event) ->
            {
                if (!answerAddTextTxF.getText().isEmpty()
                        && !answerAddValueTxF.getText().isEmpty()) {
                    try {
                        int value = Integer.parseInt(answerAddValueTxF.getText());
                        boolean valueUsed = false;
                        for (Answer a : question.getAnswers())
                            if (a.getValue() == value) {
                                valueUsed = true;
                                break;
                            }
                        if (valueUsed)
                            answerAddValueTxF.setText("Wert wird bereits verwendet");
                        else {
                            Answer answer = new Answer(answerAddTextTxF.getText(), value, question);
                            question.getAnswers().add(answer);
                            answerTable.getItems().add(answer);
                            answerAddTextTxF.clear();
                            QuestionView.updateAddValueTxF(question, answerAddValueTxF);
                        }
                    } catch (NumberFormatException e) {
                        answerAddValueTxF.setText("Ungültiger Wert");
                        if (logger.isDebugEnabled()) {
                            logger.debug("", e);
                        }
                    }
                }

            });
            answerAddBtn.defaultButtonProperty().bind(answerAddBtn.focusedProperty());
            answerAddBtn.setDisable(disabled);
            answerRemoveBtn.setOnAction((ActionEvent event) ->
            {
                if (!answerTable.getSelectionModel().isEmpty())
                    ConfirmDialog.show("Antwortmöglichkeit wirklich entfernen?", (boolean confirmed) ->
                    {
                        if (confirmed) {
                            Answer toRemove = answerTable.getSelectionModel().getSelectedItem();
                            try {
                                Database.getInstance().getAnswerDao().delete(toRemove);
                                question.getAnswers().remove(toRemove);
                                answerTable.getItems().remove(toRemove);
                                QuestionView.updateAddValueTxF(question, answerAddValueTxF);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                if (logger.isDebugEnabled()) {
                                    logger.debug("", ex);
                                }
                            }
                        }
                    }, accordion.getScene().getWindow());
            });
            answerRemoveBtn.setDisable(disabled);

            TableColumn<Answer, String> textColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(1);
            textColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
            textColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.75));

            TableColumn<Answer, String> valueColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(2);
            valueColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("value"));
            valueColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.15));
            if (question.getAnswers() != null)
                answerTable.getItems().addAll(question.getAnswers());

            tp.setContent(rootGird);
            accordion.getPanes().add(tp);
            accordion.setExpandedPane(tp);

            return tp;
        } catch (IOException ex) {
            ex.printStackTrace();
            if (logger.isDebugEnabled()) {
                logger.debug("", ex);
            }
        }
        return null;
    }

    private static void updateAddValueTxF(Question item, TextField answerAddValueTxF) {
        if (item.getAnswers() == null || item.getAnswers().isEmpty())
            answerAddValueTxF.setText(String.valueOf(1));
        else {
            int highest = Integer.MIN_VALUE;
            for (Answer answer : item.getAnswers())
                if (answer.getValue() > highest)
                    highest = answer.getValue();
            if (highest == Integer.MIN_VALUE)
                highest = 0;
            highest++;
            answerAddValueTxF.setText(String.valueOf(highest));
        }
    }
}
