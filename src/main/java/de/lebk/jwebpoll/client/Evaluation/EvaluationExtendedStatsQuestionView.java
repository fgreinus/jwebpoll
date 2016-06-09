package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.client.QuestionView;
import de.lebk.jwebpoll.client.Statistics;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

import java.io.IOException;

public class EvaluationExtendedStatsQuestionView {
    private static final Logger LOGGER = Logger.getLogger(EvaluationExtendedStatsQuestionView.class);

    public static void setQuestionView(Accordion accordion, Question question, boolean disabled) {
        if (accordion == null)
            throw new IllegalArgumentException("Accordion cannot be null!");
        if (question == null)
            throw new IllegalArgumentException("Question cannot be null!");

        TitledPane tp = new TitledPane();
        tp.setText(question.getTitle());

        GridPane rootGrid = null;
        try {
            rootGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationExtendedStatsQuestionView.fxml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("", ex);
        }
        if (rootGrid == null)
            return;

        switch (question.getType()) {
            case SINGLE:
            case MULTIPLE:
                TableView<ExtendedStatsTableHelperObject> statTable = (TableView<ExtendedStatsTableHelperObject>) rootGrid.lookup("#statTable");
                for (ExtendedStatsTableHelperObject extendedStatsTableHelperObject : calculateBetterStatistics(question)) {
                    statTable.getItems().add(extendedStatsTableHelperObject);
                }
                fillTable(statTable);
                break;
            case FREE:
                break;
        }

        tp.setContent(rootGrid);
        if (accordion.getPanes().size() == 0)
            accordion.setExpandedPane(tp);
        accordion.getPanes().add(tp);
    }


    private static ObservableList<ExtendedStatsTableHelperObject> calculateBetterStatistics(Question question) {
        ObservableList<ExtendedStatsTableHelperObject> list = FXCollections.observableArrayList();

        int weightedPollTotal = 0;
        int weightedPollCount = 0;
        for (Answer answer : question.getAnswers()) {
            weightedPollTotal += answer.getVotes().size() * answer.getValue();
            if (weightedPollTotal != 0) {
                weightedPollCount += answer.getVotes().size();
            }
        }
        double arithmeticAverage = (double) weightedPollTotal / (double) weightedPollCount;
        list.add(new ExtendedStatsTableHelperObject("Arithmetischer Durchschnitt:", arithmeticAverage));
        double variance = Statistics.getVariance(question.getAnswers(), arithmeticAverage);
        list.add(new ExtendedStatsTableHelperObject("Varianz", variance));
        double standardDeviation = Statistics.getStandardDeviation(question.getAnswers(), arithmeticAverage);
        list.add(new ExtendedStatsTableHelperObject("Standardabweichung", standardDeviation));

        return list;
    }

    private static void fillTable(TableView<ExtendedStatsTableHelperObject> statTable) {
        TableColumn<ExtendedStatsTableHelperObject, String> textcolumn = (TableColumn<ExtendedStatsTableHelperObject, String>) statTable.getColumns().get(0);
        textcolumn.setCellValueFactory(new PropertyValueFactory<ExtendedStatsTableHelperObject, String>("text"));
        textcolumn.prefWidthProperty().bind(statTable.widthProperty().multiply(0.5));

        TableColumn<ExtendedStatsTableHelperObject, String> valueColumn = (TableColumn<ExtendedStatsTableHelperObject, String>) statTable.getColumns().get(1);
        valueColumn.setCellValueFactory(new PropertyValueFactory<ExtendedStatsTableHelperObject, String>("value"));
        valueColumn.prefWidthProperty().bind(statTable.widthProperty().multiply(0.5));
    }
}
