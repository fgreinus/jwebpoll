package de.lebk.jwebpoll.client.Evaluation;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.client.ConfirmDialog;
import de.lebk.jwebpoll.client.MsgBox;
import de.lebk.jwebpoll.data.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class EvaluationDialog extends Stage {
    private static final Logger LOGGER = Logger.getLogger(EvaluationDialog.class);

    private int pollid;
    private Accordion questionsAccordion;
    private Poll poll;
    private boolean showExtendedStats = false;

    public EvaluationDialog(int pollid) {
        this.pollid = pollid;
        this.getIcons().add(new Image(EvaluationDialog.class.getResource("/icon.png").toString()));
        GridPane evaluationGrid;
        try {
            evaluationGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/evaluationDialog.fxml"));
            fillMenuBar((MenuBar) evaluationGrid.lookup("#menuBar"));
            this.questionsAccordion = (Accordion) evaluationGrid.lookup("#questionsAccordion");
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("", ex);
            return;
        }
        this.refresh();
        this.setScene(new Scene(evaluationGrid));
        this.sizeToScene();
        this.show();
    }

    private void fillMenuBar(MenuBar menuBar) {
        Menu action = new Menu("Aktion");
        action.setAccelerator(new KeyCodeCombination(KeyCode.A));
        MenuItem refresh = new MenuItem("Aktualisieren");
        refresh.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        refresh.setOnAction((ActionEvent event) -> refresh());
        MenuItem extendedStats = new MenuItem("Erweiterte Statistiken");
        extendedStats.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        extendedStats.setOnAction((ActionEvent event) ->
        {
            this.showExtendedStats = !this.showExtendedStats;
            this.refresh();
        });
        MenuItem export = new MenuItem("Export CSV");
        export.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN));
        export.setOnAction((ActionEvent event) ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("pollx.csv");
            File choosenFile = fileChooser.showSaveDialog(this.getOwner());
            if(choosenFile != null)
            {
                String text = "Umfrage exportiert.";
                if (!Serializer.toCsv(choosenFile.getAbsolutePath(), this.poll))
                    text = "Exportieren fehlgeschlagen!";
                MsgBox.show("Export", text, null, this.getOwner());
            }
        });
        action.getItems().addAll(refresh, extendedStats, export);
        menuBar.getMenus().addAll(action);
    }

    private void fillAccordion(Poll poll) {
        if(this.questionsAccordion == null)
            return;
        this.questionsAccordion.getPanes().remove(0, this.questionsAccordion.getPanes().size());
        if (poll != null)
            for (Question question : poll.questions)
                EvaluationQuestionView.setQuestionView(this.questionsAccordion, question, this.showExtendedStats);
    }


    private void refresh() {
        this.poll = loadPoll(this.pollid);
        fillAccordion(this.poll);
        this.setTitle(this.poll == null ? "Auswertung" : "Auswertung: " + this.poll.getTitle());
    }

    private Poll loadPoll(int id) {
        try {
            return Database.DB.getPollDao().queryForId(id);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        return null;
    }
}
