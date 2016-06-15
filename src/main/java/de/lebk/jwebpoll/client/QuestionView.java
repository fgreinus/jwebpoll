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
import javafx.util.converter.IntegerStringConverter;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class QuestionView extends TitledPane {
    private static final Logger LOGGER = Logger.getLogger(QuestionView.class);

    private TextField titleTxF, hintTxF;
    private CheckBox requiredCkB;
    private ComboBox<QuestionType> typeCbo;
    private Button answerAddBtn, answerRemoveBtn;
    private TableColumn<Answer, String> txtColumn;
    private TableColumn<Answer, Integer> valueColumn;

    public QuestionView(Question question) {
        try {
            GridPane rootGird = FXMLLoader.load(QuestionView.class.getResource("/client/questionView.fxml"));
            this.titleTxF = (TextField) rootGird.lookup("#titleTxF");
            this.requiredCkB = (CheckBox) rootGird.lookup("#requiredCkB");
            this.hintTxF = (TextField) rootGird.lookup("#hintTxF");
            this.typeCbo = (ComboBox<QuestionType>) rootGird.lookup("#typeCbo");
            TableView<Answer> answerTable = (TableView<Answer>) rootGird.lookup("#answerTable");
            this.answerAddBtn = (Button) rootGird.lookup("#answerAddBtn");
            this.answerRemoveBtn = (Button) rootGird.lookup("#answerRemoveBtn");
            TextArea answerFreetext = (TextArea) rootGird.lookup("#answerFreetext");

            this.titleTxF.setText("#Initialize");
            this.titleTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
            {
                question.setTitle(this.titleTxF.getText());
                if (question.getTitle() == null || question.getTitle().isEmpty())
                    this.setText("<Neue Frage>");
                else
                    this.setText(question.getTitle());
            });
            this.titleTxF.setText(question.getTitle());
            this.requiredCkB.setSelected(question.isRequired());
            this.requiredCkB.setOnAction((ActionEvent event) -> question.setRequired(this.requiredCkB.isSelected()));
            this.hintTxF.setText(question.getHint());
            this.hintTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> question.setHint(this.hintTxF.getText()));
            this.typeCbo.getItems().addAll(QuestionType.SINGLE, QuestionType.MULTIPLE, QuestionType.FREE);
            this.typeCbo.setCellFactory((ListView<QuestionType> param) -> new QuestionTypeListCell());
            this.typeCbo.setButtonCell(new QuestionTypeListCell());
            this.typeCbo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends QuestionType> observable, QuestionType oldValue, QuestionType newValue) ->
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
                        this.txtColumn = (TableColumn<Answer, String>) column;
                        this.txtColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
                    } else if (column.getId().equals("#valueColumn")) {
                        this.valueColumn = (TableColumn<Answer, Integer>) column;
                        this.valueColumn.setCellValueFactory(new PropertyValueFactory<Answer, Integer>("value"));
                    }
                }
            });
            this.typeCbo.setValue(question.getType());

            if (question.getAnswers() != null)
                answerTable.getItems().addAll(question.getAnswers());
            answerTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

            this.answerAddBtn.setOnAction((ActionEvent event) ->
            {
                Answer newAnswer = new Answer("<Neue Antwortmöglichkeit>", 1, question);
                question.getAnswers().add(newAnswer);
                answerTable.getItems().add(newAnswer);
            });
            this.answerRemoveBtn.setOnAction((ActionEvent event) ->
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
                    }, this.getScene().getWindow());
            });

            this.setEnabled(true);
            this.setContent(rootGird);
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
    }

    public QuestionView(Question question, boolean disable) {
        this(question);
        this.setEnabled(!disable);
    }

    public void setEnabled(boolean enabled) {
        this.titleTxF.setDisable(!enabled);
        this.requiredCkB.setDisable(!enabled);
        this.hintTxF.setDisable(!enabled);
        this.typeCbo.setDisable(!enabled);
        this.answerAddBtn.setDisable(!enabled);
        this.answerRemoveBtn.setDisable(!enabled);
        if (enabled) {
            this.txtColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            this.txtColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));
            this.valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            this.valueColumn.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
        } else {
            this.txtColumn.setCellFactory(param -> new TableCell<Answer, String>()
            {
                @Override
                protected void updateItem(String text, boolean empty) {
                    setText(text);
                }
            });
            this.txtColumn.setOnEditCommit(event -> {
            });
            this.valueColumn.setCellFactory(param -> new TableCell<Answer, Integer>()
            {
                @Override
                protected void updateItem(Integer value, boolean empty) {
                    setText(value == null ? "" : String.valueOf(value));
                }
            });
            this.valueColumn.setOnEditCommit(event -> {
            });
        }
    }
}
