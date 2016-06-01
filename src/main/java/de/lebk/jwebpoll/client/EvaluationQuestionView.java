package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Question;
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

import java.io.IOException;

public class EvaluationQuestionView {

    public static void setQuestionView(Accordion accordion, Question question, boolean disabled) {
        TitledPane tp = new TitledPane();
        GridPane rootGrid = null;
        TableView<Answer> answerTable = null;
        try {
            rootGrid = FXMLLoader.load(QuestionView.class.getResource("/client/evaluationQuestionView.fxml"));
            answerTable = (TableView<Answer>) rootGrid.lookup("#answerTable");
            TextField titleTxF = (TextField) rootGrid.lookup("#titleTxF");
            tp.setText(question.getTitle());

            switch (question.getType()) {
                case SINGLE:
                    fillForSingleAndMultipleChoice(question, answerTable);
                    addChart(rootGrid,question);
                    break;
                case MULTIPLE:
                    fillForSingleAndMultipleChoice(question, answerTable);
                    addChart(rootGrid,question);
                    break;
                case FREE:
                    fillForFree(question, answerTable);
                    rootGrid.setColumnSpan(answerTable,2);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (accordion.getPanes().size() == 0) {
            accordion.setExpandedPane(tp);
        }

        rootGrid.setVisible(true);
        tp.setVisible(true);
        answerTable.setVisible(true);
        tp.setContent(rootGrid);
        accordion.getPanes().add(tp);
    }

    private static void fillForSingleAndMultipleChoice(Question question, TableView<Answer> answerTable) {
        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
        typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.75));


        TableColumn<Answer, String> countColumn = new TableColumn<>("Häufigkeit");
        countColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Answer, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Answer, String> cellData) {
                return new SimpleStringProperty(String.valueOf(cellData.getValue().getVotes().size()));
            }
        });
        countColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(0.25));

        if (question.getAnswers() != null) {
            answerTable.getItems().addAll(question.getAnswers());
        }

        answerTable.getColumns().add(countColumn);



    }

    private static  void addChart(GridPane rootgrid,Question item) {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList();
        for(Answer answer:item.getAnswers()){
            pieChartData.add(new PieChart.Data(answer.getText(),answer.getVotes().size()));
        }


        final PieChart chart = new PieChart(pieChartData);
        rootgrid.add(chart,1,0);
    }

    private static void fillForFree(Question question, TableView<Answer> answerTable) {
        TableColumn<Answer, String> typeColumn = (TableColumn<Answer, String>) answerTable.getColumns().get(0);
        typeColumn.setCellValueFactory(new PropertyValueFactory<Answer, String>("text"));
        typeColumn.prefWidthProperty().bind(answerTable.widthProperty().multiply(1));


        if (question.getAnswers() != null) {
            answerTable.getItems().addAll(question.getAnswers());
        }

    }
}
