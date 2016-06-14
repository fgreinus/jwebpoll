package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.client.SimpleNumberProperty;
import de.lebk.jwebpoll.client.QuestionView;
import de.lebk.jwebpoll.Statistics;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.QuestionType;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EvaluationQuestionView extends TitledPane {
    private static final Logger LOGGER = Logger.getLogger(EvaluationQuestionView.class);
    private static final String SUM = "Summe";
    private static final String AVG = "Durchschnitt";
    private static final String VAR = "Varianz";
    private static final String DEV = "Standardabweichung";

    private Question question;
    private TableView<Answer> answerTable;
    private TableView<Vote> voteTable;
    private List<Answer> extendedStats = new ArrayList<>();

    public EvaluationQuestionView(Question question) throws IOException {
        this.question = question;
        if (this.question == null)
            throw new IllegalArgumentException("Question cannot be null!");

        this.setText(this.question.getTitle());

        GridPane rootGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationQuestionView.fxml"));
        switch (this.question.getType()) {
            case SINGLE:
            case MULTIPLE:
                this.answerTable = (TableView<Answer>) rootGrid.lookup("#table");
                this.initAnswerTable();
                rootGrid.add(this.getPieGrid(), 1, 0);
                break;
            case FREE:
                this.voteTable = (TableView<Vote>) rootGrid.lookup("#table");
                TableColumn<Vote, String> typeColumn = (TableColumn<Vote, String>) voteTable.getColumns().get(0);
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("userText"));
                typeColumn.prefWidthProperty().bind(this.voteTable.widthProperty());
                for (Vote vote : this.question.getFreetextVotes())
                    if (vote.getUserText() != null && !vote.getUserText().isEmpty())
                        this.voteTable.getItems().add(vote);
                GridPane.setColumnSpan(this.voteTable, 2);
                break;
        }
        this.setContent(rootGrid);
    }

    public void showExtendedStats(boolean show) {
        if (this.question.getType() == QuestionType.FREE)
            return;

        if (show)
            this.answerTable.getItems().addAll(this.extendedStats);
        else
            this.answerTable.getItems().removeAll(this.extendedStats);
    }

    private void initAnswerTable() {
        int sumCount = 0;
        int sumWeight = 0;
        double avgCount = 0;
        double avgWeight = 0;
        if (question.getAnswers() != null) {
            this.answerTable.getItems().addAll(question.getAnswers());
            for (Answer answer : question.getAnswers()) {
                sumCount += answer.getVotes().size();
                sumWeight += answer.getVotes().size() * answer.getValue();
            }
            avgCount = (double) sumCount / question.getAnswers().size();
            avgWeight = (double) sumWeight / question.getAnswers().size();

            this.extendedStats.add(new Answer(SUM, 0, null));
            this.extendedStats.add(new Answer(AVG, 0, null));
            this.extendedStats.add(new Answer(VAR, 0, null));
            this.extendedStats.add(new Answer(DEV, 0, null));
        }
        final int sumCountFinal = sumCount;
        final int sumWeightFinal = sumWeight;
        final double avgCountFinal = avgCount;
        final double avgWeightFinal = avgWeight;
        int[] varCountValues = new int[question.getAnswers().size()];
        int[] varWeightValues = new int[question.getAnswers().size()];
        int i = 0;
        for (Answer answer : question.getAnswers()) {
            varCountValues[i] = answer.getVotes().size();
            varWeightValues[i++] = answer.getVotes().size() * answer.getValue();
        }

        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) this.answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        typeColumn.prefWidthProperty().bind(this.answerTable.widthProperty().multiply(0.5));

        TableColumn<Answer, Number> countColumn = new TableColumn<>("Häufigkeit");
        countColumn.setCellValueFactory(cellData ->
        {
            Answer answer = cellData.getValue();
            if (answer.getQuestion() == null) {
                if (answer.getText().equals(SUM))
                    return new SimpleNumberProperty(new SimpleIntegerProperty(sumCountFinal));
                if (answer.getText().equals(AVG))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(avgCountFinal)));
                if (answer.getText().equals(VAR))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getVariance(varCountValues, avgCountFinal))));
                if (answer.getText().equals(DEV))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getStandardDeviation(varCountValues, avgCountFinal))));
            }
            return new SimpleNumberProperty(new SimpleIntegerProperty(answer.getVotes().size()));
        });
        countColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        countColumn.prefWidthProperty().bind(this.answerTable.widthProperty().multiply(0.25));

        TableColumn<Answer, Number> weightedColumn = new TableColumn<>("Gewichtet");
        weightedColumn.setCellValueFactory(cellData ->
        {
            Answer answer = cellData.getValue();
            if (answer.getQuestion() == null) {
                if (answer.getText().equals(SUM))
                    return new SimpleNumberProperty(new SimpleIntegerProperty(sumWeightFinal));
                if (answer.getText().equals(AVG))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(avgWeightFinal)));
                if (answer.getText().equals(VAR))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getVariance(varWeightValues, avgWeightFinal))));
                if (answer.getText().equals(DEV))
                    return new SimpleNumberProperty(new SimpleDoubleProperty(Statistics.round(Statistics.getStandardDeviation(varWeightValues, avgWeightFinal))));
            }
            return new SimpleNumberProperty(new SimpleIntegerProperty(answer.getVotes().size() * answer.getValue()));
        });
        weightedColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        weightedColumn.prefWidthProperty().bind(this.answerTable.widthProperty().multiply(0.25));

        this.answerTable.setRowFactory(param ->
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
        this.answerTable.setSortPolicy(param ->
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

        this.answerTable.getColumns().addAll(countColumn, weightedColumn);
    }

    private GridPane getPieGrid() {
        int i = 0;
        int[] countValues = new int[this.question.getAnswers().size()];
        int[] weightValues = new int[this.question.getAnswers().size()];
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Answer answer : this.question.getAnswers()) {
            countValues[i] = answer.getVotes().size();
            weightValues[i] = answer.getVotes().size() * answer.getValue();
            pieChartData.add(new PieChart.Data(answer.getText(), countValues[i++]));
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
            return null;

        PieChart pieChart = (PieChart) pieGrid.lookup("#pieChart");
        pieChart.setData(pieChartData);
        ToggleGroup toggleGrp = new ToggleGroup();
        RadioButton rbtCount = (RadioButton) pieGrid.lookup("#rbtCount");
        rbtCount.setToggleGroup(toggleGrp);
        rbtCount.setOnAction(event ->
        {
            int j = 0;
            for (PieChart.Data data : pieChart.getData())
                data.setPieValue(countValues[j++]);
        });
        RadioButton rbtWeight = (RadioButton) pieGrid.lookup("#rbtWeight");
        rbtWeight.setToggleGroup(toggleGrp);
        rbtWeight.setOnAction(event ->
        {
            int j = 0;
            for (PieChart.Data data : pieChart.getData())
                data.setPieValue(weightValues[j++]);
        });
        rbtCount.setSelected(true);
        return pieGrid;
    }
}
