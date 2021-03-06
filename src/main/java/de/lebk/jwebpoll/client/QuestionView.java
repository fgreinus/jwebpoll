package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.CellRenderer.QuestionTypeListCell;
import de.lebk.jwebpoll.client.Dialogs.ConfirmDialog;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.QuestionType;
import javafx.application.Platform;
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

    private static final String[] TEMPLATES = {"Ja | Nein", "Bewertung: ++ +   - --", "Bewertung: ++ + 0 - --", "Schulnoten"};

    private boolean enabled;

    private TextField titleTxF, hintTxF;
    private CheckBox requiredCkB;
    private ComboBox<String> typeCbo;
    private Button answerAddBtn, answerRemoveBtn;
    private TableColumn<Answer, String> txtColumn;
    private TableColumn<Answer, Integer> valueColumn;

    private Runnable onAnswersChanged;

    public QuestionView(Question question) {
        try {
            GridPane rootGird = FXMLLoader.load(QuestionView.class.getResource("/client/questionView.fxml"));
            this.titleTxF = (TextField) rootGird.lookup("#titleTxF");
            this.requiredCkB = (CheckBox) rootGird.lookup("#requiredCkB");
            this.hintTxF = (TextField) rootGird.lookup("#hintTxF");
            this.typeCbo = (ComboBox<String>) rootGird.lookup("#typeCbo");
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
            this.typeCbo.getItems().addAll(QuestionType.SINGLE.toString(), QuestionType.MULTIPLE.toString(), QuestionType.FREE.toString());
            if (question.getAnswers().isEmpty())
                this.typeCbo.getItems().addAll(QuestionView.TEMPLATES);
            this.typeCbo.setCellFactory((ListView<String> param) -> new QuestionTypeListCell());
            this.typeCbo.setButtonCell(new QuestionTypeListCell());
            this.typeCbo.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
            {
                if (newValue.equals(oldValue))
                    return;

                if (newValue.equals(QuestionType.SINGLE.toString()))
                    question.setType(QuestionType.SINGLE);
                else if (newValue.equals(QuestionType.MULTIPLE.toString()))
                    question.setType(QuestionType.MULTIPLE);
                else if (newValue.equals(QuestionType.FREE.toString()))
                    question.setType(QuestionType.FREE);
                else {
                    question.setType(QuestionType.SINGLE);
                    if (newValue.equals(QuestionView.TEMPLATES[0])) {
                        question.getAnswers().add(new Answer("Ja", 1, question));
                        question.getAnswers().add(new Answer("Nein", 1, question));
                    } else if (newValue.equals(QuestionView.TEMPLATES[1])) {
                        question.getAnswers().add(new Answer("Sehr gut", 1, question));
                        question.getAnswers().add(new Answer("Gut", 1, question));
                        question.getAnswers().add(new Answer("Schlecht", 1, question));
                        question.getAnswers().add(new Answer("Sehr Schlecht", 1, question));
                    } else if (newValue.equals(QuestionView.TEMPLATES[2])) {
                        question.getAnswers().add(new Answer("Sehr gut", 1, question));
                        question.getAnswers().add(new Answer("Gut", 1, question));
                        question.getAnswers().add(new Answer("Mittelmäßig", 1, question));
                        question.getAnswers().add(new Answer("Schlecht", 1, question));
                        question.getAnswers().add(new Answer("Sehr Schlecht", 1, question));
                    } else if (newValue.equals(QuestionView.TEMPLATES[3])) {
                        question.getAnswers().add(new Answer("Sehr gut", 1, question));
                        question.getAnswers().add(new Answer("Gut", 1, question));
                        question.getAnswers().add(new Answer("Befriedigend", 1, question));
                        question.getAnswers().add(new Answer("Ausreichend", 1, question));
                        question.getAnswers().add(new Answer("Mangelhaft", 1, question));
                        question.getAnswers().add(new Answer("Ungenügend", 1, question));
                    }
                    answerTable.getItems().addAll(question.getAnswers());
                    Platform.runLater(() ->
                    {
                        this.typeCbo.getSelectionModel().select(question.getType().toString());
                        QuestionView.this.typeCbo.getItems().removeAll(QuestionView.TEMPLATES);
                    });
                }
                if (this.onAnswersChanged != null)
                    this.onAnswersChanged.run();
                boolean displayAnswerTable = question.getType() != QuestionType.FREE;
                answerTable.setVisible(displayAnswerTable);
                this.answerAddBtn.setVisible(displayAnswerTable);
                this.answerRemoveBtn.setVisible(displayAnswerTable);
                answerFreetext.setVisible(!displayAnswerTable);
                if (question.getType() == QuestionType.FREE)
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
                        this.txtColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
                    } else if (column.getId().equals("#valueColumn")) {
                        this.valueColumn = (TableColumn<Answer, Integer>) column;
                        this.valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
                    }
                }
                this.setEnabled(this.enabled);
            });
            this.typeCbo.setValue(question.getType().toString());

            answerTable.getItems().addAll(question.getAnswers());
            answerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            this.answerAddBtn.setOnAction((ActionEvent event) ->
            {
                Answer newAnswer = new Answer("<Neue Antwortmöglichkeit>", 1, question);
                question.getAnswers().add(newAnswer);
                answerTable.getItems().add(newAnswer);
                this.typeCbo.getItems().removeAll(QuestionView.TEMPLATES);
                if (this.onAnswersChanged != null)
                    this.onAnswersChanged.run();
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
                                if (this.onAnswersChanged != null)
                                    this.onAnswersChanged.run();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                if (LOGGER.isDebugEnabled())
                                    LOGGER.debug("", ex);
                            }
                            if (answerTable.getItems().isEmpty())
                                this.typeCbo.getItems().addAll(QuestionView.TEMPLATES);
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

    public QuestionView(Question question, Runnable onAnswersChanged) {
        this(question);
        this.setOnAnswersChanged(onAnswersChanged);
    }

    public QuestionView(Question question, Runnable onAnswersChanged, boolean disable) {
        this(question, disable);
        this.setOnAnswersChanged(onAnswersChanged);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        this.titleTxF.setDisable(!enabled);
        this.requiredCkB.setDisable(!enabled);
        this.hintTxF.setDisable(!enabled);
        this.typeCbo.setDisable(!enabled);
        this.answerAddBtn.setDisable(!enabled);
        this.answerRemoveBtn.setDisable(!enabled);

        if (enabled) {
            if (this.txtColumn != null) {
                this.txtColumn.setCellFactory(TextFieldTableCell.forTableColumn());
                this.txtColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));
            }
            if (this.valueColumn != null) {
                this.valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
                this.valueColumn.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
            }
        } else {
            if (this.txtColumn != null) {
                this.txtColumn.setCellFactory(param -> new TableCell<Answer, String>() {
                    @Override
                    protected void updateItem(String text, boolean empty) {
                        setText(text);
                    }
                });
                this.txtColumn.setOnEditCommit(event -> {
                });
            }
            if (this.valueColumn != null) {
                this.valueColumn.setCellFactory(param -> new TableCell<Answer, Integer>() {
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

    public void setOnAnswersChanged(Runnable onAnswersChanged) {
        this.onAnswersChanged = onAnswersChanged;
    }
}
