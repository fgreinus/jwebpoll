package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.client.SimpleNumberProperty;
import de.lebk.jwebpoll.client.QuestionView;
import de.lebk.jwebpoll.client.Statistics;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Comparator;

public class EvaluationQuestionView {
    private static final Logger LOGGER = Logger.getLogger(EvaluationQuestionView.class);
    private static final String SUM = "Summe";
    private static final String AVG = "Durchschnitt";
    private static final String VAR = "Varianz";
    private static final String DEV ="Standardabweichung";

    public static void setQuestionView(Accordion accordion, Question question,boolean shoExtendedStats) {
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
                fillForSingleAndMultipleChoice(question, answerTable,shoExtendedStats);
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

    private static void fillForSingleAndMultipleChoice(Question question, TableView<Answer> answerTable,boolean showExtendedStats) {
        int sumCount = 0;
        int sumWeight = 0;
        double avgCount = 0;
        double avgWeight = 0;
        if (question.getAnswers() != null) {
            answerTable.getItems().addAll(question.getAnswers());
            for (Answer answer : question.getAnswers()) {
                sumCount += answer.getVotes().size();
                sumWeight += answer.getVotes().size() * answer.getValue();
            }
            avgCount = (double) sumCount / question.getAnswers().size();
            avgWeight = (double) sumWeight / question.getAnswers().size();
            if(showExtendedStats) {
                answerTable.getItems().add(new Answer(SUM, 0, null));
                answerTable.getItems().add(new Answer(AVG, 0, null));
                answerTable.getItems().add(new Answer(VAR, 0, null));
                answerTable.getItems().add(new Answer(DEV, 0, null));
            }

        }
        final int sumCountFinal = sumCount;
        final int sumWeightFinal = sumWeight;
        final double avgCountFinal = avgCount;
        final double avgWeightFinal = avgWeight;
        int[] varCountValues = new int[question.getAnswers().size()];
        int[] varWeightValues = new int[question.getAnswers().size()];
        int i = 0;
        for(Answer answer : question.getAnswers())
        {
            varCountValues[i] = answer.getVotes().size();
            varWeightValues[i++] = answer.getVotes().size() * answer.getValue();
        }

        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.5));

        TableColumn<Answer, Number> countColumn = new TableColumn<>("Häufigkeit");
        countColumn.setCellValueFactory(cellData ->
        {
            Answer answer = cellData.getValue();
            if (answer.getQuestion() == null) {
                if (answer.getText().equals(SUM))
                    return new SimpleNumberProperty(new SimpleIntegerProperty(sumCountFinal));
                if (answer.getText().equals(AVG))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(avgCountFinal)));
                if(answer.getText().equals(VAR))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getVariance(varCountValues, avgCountFinal))));
                if(answer.getText().equals(DEV))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getStandardDeviation(varCountValues, avgCountFinal))));
            }
            return new SimpleNumberProperty(new SimpleIntegerProperty(answer.getVotes().size()));
        });
        countColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        countColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

        TableColumn<Answer, Number> weightedColumn = new TableColumn<>("Gewichtet");
        weightedColumn.setCellValueFactory(cellData ->
        {
            Answer answer = cellData.getValue();
            if (answer.getQuestion() == null) {
                if (answer.getText().equals(SUM))
                    return new SimpleNumberProperty(new SimpleIntegerProperty(sumWeightFinal));
                if (answer.getText().equals(AVG))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(avgWeightFinal)));
                if(answer.getText().equals(VAR))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getVariance(varWeightValues, avgWeightFinal))));
                if(answer.getText().equals(DEV))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getStandardDeviation(varWeightValues, avgWeightFinal))));
            }
            return new SimpleNumberProperty(new SimpleIntegerProperty(answer.getVotes().size() * answer.getValue()));
        });
        weightedColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        weightedColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

        answerTable.setRowFactory(param ->
                new TableRow<Answer>() {
                    @Override
                    protected void updateItem(Answer answer, boolean empty) {
                        super.updateItem(answer, empty);

                        if (answer != null && answer.getQuestion() == null) {
                            if (answer.getText().equals(SUM))
                                setStyle("-fx-font-weight: bold;");
                            else if (answer.getText().equals(AVG))
                                setStyle("-fx-font-style: italic;");
                        } else
                            setStyle("");
                    }
                });
        answerTable.setSortPolicy(param ->
        {
            Comparator<Answer> comparator = (a1, a2) ->
            {
                if (a1.getQuestion() == null) {
                    if (a2.getQuestion() != null)
                        return 1;
                    if (a1.getText().equals(SUM))
                        return -1;
                    if (a1.getText().equals(AVG) && !a2.getText().equals(SUM))
                        return -1;
                    if (a1.getText().equals(VAR) && a2.getText().equals(DEV))
                        return -1;
                    return 1;
                }
                if (a2.getQuestion() == null) {
                    return -1;
                }
                if (param.getComparator() == null)
                    return 0;
                return param.getComparator().compare(a1, a2);
            };
            FXCollections.sort(param.getItems(), comparator);
            return true;
        });

        answerTable.getColumns().add(countColumn);
        answerTable.getColumns().add(weightedColumn);
    }

    private static void addChart(GridPane rootgrid, Question question) {
        ObservableList<PieChart.Data> pieChartCountData = FXCollections.observableArrayList();
        for (Answer answer : question.getAnswers()) {
            pieChartCountData.add(new PieChart.Data(answer.getText(), answer.getVotes().size()));
        }
        ObservableList<PieChart.Data> pieChartWeightData = FXCollections.observableArrayList();
        for (Answer answer : question.getAnswers()) {
            pieChartWeightData.add(new PieChart.Data(answer.getText(), answer.getVotes().size() * answer.getValue()));
        }

        GridPane pieGrid = null;
        try {
            pieGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationPieChartView.fxml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        if (pieGrid == null)
            return;

        PieChart pieChart = (PieChart) pieGrid.lookup("#pieChart");
        pieChart.setData(pieChartCountData);
        ToggleGroup toggleGrp = new ToggleGroup();
        RadioButton rbtCount = (RadioButton) pieGrid.lookup("#rbtCount");
        rbtCount.setToggleGroup(toggleGrp);
        rbtCount.setOnAction(event -> pieChart.setData(pieChartCountData));
        RadioButton rbtWeight = (RadioButton) pieGrid.lookup("#rbtWeight");
        rbtWeight.setToggleGroup(toggleGrp);
        rbtWeight.setOnAction(event -> pieChart.setData(pieChartWeightData));
        rbtCount.setSelected(true);
        rootgrid.add(pieGrid, 1, 0);
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
