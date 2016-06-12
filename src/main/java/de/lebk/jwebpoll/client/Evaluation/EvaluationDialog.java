package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.ConfirmDialog;
import de.lebk.jwebpoll.client.MsgBox;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
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
        if (poll != null)
            initalizeEvaluationDialog();
    }

    private void initalizeEvaluationDialog() {
        Stage evaluationStage = new Stage();
        evaluationStage.getIcons().add(new Image(EvaluationDialog.class.getResource("/icon.png").toString()));
        evaluationStage.setTitle("Auswertung: " + poll.getTitle());
        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            initalizeMenuBar((MenuBar) evaluationGrid.lookup("#menuBar"));
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
        Menu action = new Menu("Aktion");
        action.setAccelerator(new KeyCodeCombination(KeyCode.A));
        MenuItem refresh = new MenuItem("Aktualisieren");
        refresh.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        refresh.setOnAction((ActionEvent event) -> refresh());
        MenuItem export = new MenuItem("Export CSV");
        export.setAccelerator(new KeyCodeCombination(KeyCode.E));
        export.setOnAction((ActionEvent event) ->
        {
            DirectoryChooser directoryChooser=new DirectoryChooser();
            File selectedDirectory =
                    directoryChooser.showDialog(new Stage());

            String text = "Poll exported.";
            if (!Serializer.toCsv(selectedDirectory.getAbsolutePath()+File.separator+"pollx.csv", poll)) {
                text = "Poll export failed.";
            }
            MsgBox.show("Exported", text, null, null);
        });
        action.getItems().addAll(refresh, export);
        menuBar.getMenus().addAll(action);
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
