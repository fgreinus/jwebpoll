package de.lebk.jwebpoll.client;

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
                answerTable.setVisible(newValue != QuestionType.FREE);
                answerFreetext.setVisible(newValue == QuestionType.FREE);
                if (newValue == QuestionType.FREE)
                    return;
                for (TableColumn<Answer, ?> column : answerTable.getColumns()) {
                    if (column.getText().isEmpty()) {
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
                                            if (graphic != null) {
                                                typeColumn.prefWidthProperty().bind(((ButtonBase) graphic).widthProperty());
                                                graphic.setDisable(true);
                                            }
                                        } else
                                            setText(null);
                                        setGraphic(graphic);
                                    }
                                };
                            }
                        });
                    } else if (column.getText().equals("Text")) {
                        TableColumn<Answer, String> txtColumn = (TableColumn<Answer, String>) column;
                        txtColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                        txtColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));
                    } else if (column.getText().equals("Wert")) {
                        TableColumn<Answer, Integer> valueColumn = (TableColumn<Answer, Integer>) column;
                        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
                        valueColumn.setOnEditCommit(event ->
                        {
                            event.getRowValue().setValue(event.getNewValue());
                        });
                    }
                }
            });
            typeCbo.setValue(question.getType());
            typeCbo.setDisable(disabled);

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

            return tp;
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
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
