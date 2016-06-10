package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.ConfirmDialog;
import de.lebk.jwebpoll.data.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class EvaluationDialog {
    private static final Logger LOGGER = Logger.getLogger(EvaluationDialog.class);

    private int pollid;
    private Accordion questionsAccordion;
    private Poll poll;

    public EvaluationDialog(int pollid) {
        this.pollid = pollid;
        loadPollFromDB();
        if (poll != null) {
            initalizeEvaluationDialog();
        }
    }

    private void initalizeEvaluationDialog() {
        Stage evaluationStage = new Stage();
        evaluationStage.getIcons().add(new Image(EvaluationDialog.class.getResource("/icon.png").toString()));
        evaluationStage.setTitle("Auswertung: " + poll.getTitle());
        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            //Initalise Menu Bar
            MenuBar menuBar = (MenuBar) evaluationGrid.lookup("#menuBar");
            initalizeMenuBar(menuBar);
            this.questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");
            initalizeAccordion();
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("", ex);
            return;
        }

        evaluationStage.setScene(new Scene(evaluationGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }

    private void initalizeMenuBar(MenuBar menuBar) {
        Menu erweitert = new Menu("Erweitert");
        MenuItem erweiterteStats = new MenuItem("Erweiterte Statistiken");
        erweiterteStats.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                EvaluationExtendedStats.show(poll);
            }
        });
        MenuItem refresh = new MenuItem("Neu Laden");
        refresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                refresh();
            }
        });
        MenuItem export = new MenuItem("Exportiere Ergebnisse als CSV");
        erweitert.getItems().addAll(erweiterteStats, refresh, export);
        menuBar.getMenus().addAll(erweitert);
    }

    private void initalizeAccordion() {
        for (Question question : poll.questions)
            EvaluationQuestionView.setQuestionView(questionsAccordion, question);
    }


    private void refresh() {
        questionsAccordion.getPanes().remove(0, questionsAccordion.getPanes().size());
        loadPollFromDB();
        initalizeAccordion();

    }

    private void loadPollFromDB() {
        Poll poll = null;
        try {
            poll = Database.DB.getPollDao().queryForId(pollid);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        this.poll = poll;
    }
}