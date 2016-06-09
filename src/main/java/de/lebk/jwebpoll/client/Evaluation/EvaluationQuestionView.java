package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.QuestionView;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;

public class EvaluationQuestionView {
    private static final Logger LOGGER = Logger.getLogger(EvaluationQuestionView.class);

    public static void setQuestionView(Accordion accordion, Question question) {
        if (accordion == null)
            throw new IllegalArgumentException("Accordion cannot be null!");
        if (question == null)
            throw new IllegalArgumentException("Question cannot be null!");

        TitledPane tp = new TitledPane();
        tp.setText(question.getTitle());

        GridPane rootGrid = null;
        try {
            rootGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationQuestionView.fxml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        if (rootGrid == null)
            return;

        switch (question.getType()) {
            case SINGLE:
            case MULTIPLE:
                TableView<Answer> answerTable = (TableView<Answer>) rootGrid.lookup("#voteTable");
                fillForSingleAndMultipleChoice(question, answerTable);
                addChart(rootGrid, question);
                break;
            case FREE:
                TableView<Vote> voteTable = (TableView<Vote>) rootGrid.lookup("#voteTable");
                fillForFree(question, voteTable);
                rootGrid.setColumnSpan(voteTable, 2);
                break;
        }

        tp.setContent(rootGrid);
        if (accordion.getPanes().size() == 0)
            accordion.setExpandedPane(tp);
        accordion.getPanes().add(tp);
    }

    private static void fillForSingleAndMultipleChoice(Question question, TableView<Answer> answerTable) {
        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.5));


        TableColumn<Answer, String> countColumn = new TableColumn<>("Häufigkeit");
        countColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getVotes().size())));
        countColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

        TableColumn<Answer, String> weightedColumn = new TableColumn<>("Gewichtet");
        weightedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getVotes().size() * cellData.getValue().getValue())));
        weightedColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

        if (question.getAnswers() != null) {
            answerTable.getItems().addAll(question.getAnswers());
            int sumCount = 0;
            int sumWeight = 0;
            for (Answer answer : question.getAnswers()) {
                sumCount += answer.getVotes().size();
                sumWeight += answer.getVotes().size() * answer.getValue();
            }
            answerTable.getItems().add(new Answer("Summe " +sumCount + " / " + sumWeight, sumCount, null));
            answerTable.setRowFactory(param ->
                new TableRow<Answer>() {
                    @Override
                    protected void updateItem(Answer item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item != null && item.getQuestion() == null)
                            setStyle("-fx-font-weight: bold;");
                        else
                            setStyle("");
                    }
                });
            answerTable.setSortPolicy(param ->
            {
                Comparator<Answer> comparator = (a1, a2) ->
                {
                    if (a1.getQuestion() == null)
                        return 1;
                    if (a2.getQuestion() == null)
                        return -1;
                    if (param.getComparator() == null)
                        return 0;
                    return param.getComparator().compare(a1, a2);
                };
                FXCollections.sort(param.getItems(), comparator);
                return true;
            });
        }

        answerTable.getColumns().add(countColumn);
        answerTable.getColumns().add(weightedColumn);
    }

    private static void addChart(GridPane rootgrid, Question question) {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        for (Answer answer : question.getAnswers()) {
            pieChartData.add(new PieChart.Data(answer.getText(), answer.getVotes().size()));
        }

        final PieChart chart = new PieChart(pieChartData);
        rootgrid.add(chart, 1, 0);
    }

    private static void fillForFree(Question question, TableView<Vote> voteTable) {
        TableColumn<Vote, String> typeColumn = (TableColumn<Vote, String>) voteTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<Vote, String>("userText"));
        typeColumn.prefWidthProperty().bind(voteTable.widthProperty().multiply(1));

        for (Vote vote : question.getFreetextVotes())
            if (vote.getUserText() != null && !vote.getUserText().isEmpty())
                voteTable.getItems().add(vote);
    }
}
