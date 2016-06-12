package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.CellRenderer.QuestionTypeListCell;
import de.lebk.jwebpoll.client.Dialogs.ConfirmDialog;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.QuestionType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class QuestionView {
    private static final Logger LOGGER = Logger.getLogger(QuestionView.class);

    public static TitledPane setQuestionView(Accordion accordion, Question question, boolean disabled) {
        try {
            TitledPane tp = new TitledPane();
            GridPane rootGird = FXMLLoader.load(QuestionView.class.getResource("/client/questionView.fxml"));
            TextField titleTxF = (TextField) rootGird.lookup("#titleTxF");
            CheckBox requiredCkB = (CheckBox) rootGird.lookup("#requiredCkB");
            TextField hintTxF = (TextField) rootGird.lookup("#hintTxF");
            ComboBox<QuestionType> typeCbo = (ComboBox<QuestionType>) rootGird.lookup("#typeCbo");
            TableView<Answer> answerTable = (TableView<Answer>) rootGird.lookup("#answerTable");
            Button answerAddBtn = (Button) rootGird.lookup("#answerAddBtn");
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
            requiredCkB.setOnAction((ActionEvent event) -> question.setRequired(requiredCkB.isSelected()));
            requiredCkB.setDisable(disabled);
            hintTxF.setText(question.getHint());
            hintTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> question.setHint(hintTxF.getText()));
            hintTxF.setDisable(disabled);
            typeCbo.getItems().addAll(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.FREE);
            typeCbo.setCellFactory((ListView<QuestionType> param) -> new QuestionTypeListCell());
            typeCbo.setButtonCell(new QuestionTypeListCell());
            typeCbo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends QuestionType> observable, QuestionType oldValue, QuestionType newValue) ->
            {
                if (newValue == oldValue)
                    return;
                question.setType(newValue);
                boolean displayAnswerTable = newValue != QuestionType.FREE;
                answerTable.setVisible(displayAnswerTable);
                answerAddBtn.setVisible(displayAnswerTable);
                answerRemoveBtn.setVisible(displayAnswerTable);
                answerFreetext.setVisible(!displayAnswerTable);
                if (newValue == QuestionType.FREE)
                    return;
                for (TableColumn<Answer, ?> column : answerTable.getColumns()) {
                    if (column.getId().equals("#controlColumn")) {
                        final TableColumn<Answer, QuestionType> typeColumn = (TableColumn<Answer, QuestionType>) column;
                        typeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(question.getType()));
                        typeColumn.setCellFactory(new Callback<TableColumn<Answer, QuestionType>, TableCell<Answer, QuestionType>>() {
                            @Override
                            public TableCell<Answer, QuestionType> call(TableColumn<Answer, QuestionType> btnCol) {
                                return new TableCell<Answer, QuestionType>() {
                                    @Override
                                    public void updateItem(QuestionType type, boolean empty) {
                                        super.updateItem(type, empty);
                                        Node graphic = null;
                                        if (type != null && !empty) {
                                            switch (type) {
                                                case SINGLE:
                                                    graphic = new RadioButton();
                                                    break;
                                                case MULTIPLE:
                                                    graphic = new CheckBox();
                                                    break;
                                            }
                                            if (graphic != null)
                                                graphic.setDisable(true);
                                        } else
                                            setText(null);
                                        setGraphic(graphic);
                                    }
                                };
                            }
                        });
                    } else if (column.getId().equals("#textColumn")) {
                        TableColumn<Answer, String> txtColumn = (TableColumn<Answer, String>) column;
                        if(!disabled)
                        {
                            txtColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                            txtColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));
                        }
                        txtColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
                    } else if (column.getId().equals("#valueColumn")) {
                        TableColumn<Answer, Integer> valueColumn = (TableColumn<Answer, Integer>) column;
                        if(!disabled)
                        {
                            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
                            valueColumn.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
                        }
                        valueColumn.setCellValueFactory(new PropertyValueFactory<Answer, Integer>("value"));
                    }
                }
            });
            typeCbo.setValue(question.getType());
            typeCbo.setDisable(disabled);

            if (question.getAnswers() != null)
                answerTable.getItems().addAll(question.getAnswers());
            answerTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            answerAddBtn.setOnAction((ActionEvent event) ->
            {
                Answer newAnswer = new Answer("<Neue Antwortmöglichkeit>", nextAnswerWeight(question), question);
                question.getAnswers().add(newAnswer);
                answerTable.getItems().add(newAnswer);
            });
            answerAddBtn.setDisable(disabled);
            answerRemoveBtn.setOnAction((ActionEvent event) ->
            {
                if (!answerTable.getSelectionModel().isEmpty())
                    ConfirmDialog.show("Antwortmöglichkeit wirklich entfernen?", confirmed ->
                    {
                        if (confirmed) {
                            Answer toRemove = answerTable.getSelectionModel().getSelectedItem();
                            try {
                                Database.DB.getAnswerDao().delete(toRemove);
                                question.getAnswers().remove(toRemove);
                                answerTable.getItems().remove(toRemove);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("", ex);
                            }
                        }
                    }, accordion.getScene().getWindow());
            });
            answerRemoveBtn.setDisable(disabled);

            tp.setContent(rootGird);
            accordion.getPanes().add(tp);

            return tp;
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        return null;
    }

    private static int nextAnswerWeight(Question item) {
        if (item.getAnswers() == null || item.getAnswers().isEmpty())
            return 1;
        int highest = Integer.MIN_VALUE;
        for (Answer answer : item.getAnswers())
            if (answer.getValue() > highest)
                highest = answer.getValue();
        if (highest == Integer.MIN_VALUE)
            highest = 0;
        return ++highest;
    }
}
