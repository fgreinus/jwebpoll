package de.lebk.jwebpoll.client;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.Frontend;
import de.lebk.jwebpoll.data.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Client extends Application {
    //- Main -
    public static void main(String[] args) {
        Client.launch(args);
    }

    //- Data -
    private List<Poll> polls = new ArrayList<>();
    //poll selected in client window
    private static Poll poll;
    private static boolean pollHasChanges;
    //poll running on server
    private static Poll activePoll;

    //- DB -
    private Database db;

    //- View -
    private ListView<Poll> pollList;
    private Button pollAddBtn, pollRemoveBtn;
    private Button pollSaveBtn, pollCancelBtn;
    private TextField titleTxF;
    private TextArea descTxF;
    private TextField createdDateTxF, createdTimeTxF;
    private ComboBox<PollState> stateCbo;
    private Button openBtn, closeBtn, resultsBtn;
    private Accordion questionsAccordion;
    private Button questionsAddBtn, questionsRemoveBtn;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JWebPoll");
        primaryStage.setOnCloseRequest((WindowEvent we) ->
        {
            try {
                Frontend.kill();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        primaryStage.getIcons().add(new Image(Client.class.getResource("/icon.png").toString()));

        // Start DB
        this.db = Database.getInstance();

        this.polls.addAll(this.db.getPollDao().queryForAll());
        for (Poll p : this.polls) {
            if (p.getState() == PollState.OPEN) {
                Client.activePoll = p;
                try {
                    spawnWebServer(Client.activePoll);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        // ListView (Left side)
        SplitPane rootSplit = FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        GridPane pollListView = FXMLLoader.load(this.getClass().getResource("/client/pollListView.fxml"));
        this.pollList = (ListView<Poll>) pollListView.lookup("#pollList");
        this.pollList.setCellFactory((ListView<Poll> param) ->
        {
            return new PollListCell();
        });
        this.pollList.getItems().addAll(this.polls);
        this.pollList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Poll> observable, Poll oldValue, Poll newValue) ->
        {
            Client.this.setPoll(newValue);
        });

        this.pollAddBtn = (Button) pollListView.lookup("#pollAddBtn");
        this.pollAddBtn.setOnAction((ActionEvent ev) ->
        {
            if (Client.pollHasChanges) {
                ConfirmDialog.show("Bevor die Umfrage geöffnet werden kann, muss sie noch einmal gespeichert werden.\n" +
                        "\n" +
                        "Möchten Sie die Umfrage jetzt speichern?", confirmed ->
                {
                    Client.pollHasChanges = !this.db.savePoll(Client.poll);
                }, primaryStage);
            }
            if (!pollHasChanges) {
                Poll newPoll = new Poll("<Neue Umfrage>", "", PollState.NEW);
                this.polls.add(newPoll);
                this.pollList.getItems().addAll(newPoll);
                Client.poll = newPoll;
            }
        });

        rootSplit.getItems().add(pollListView);
        rootSplit.setDividerPositions(1d / 5d);

        // PollView (Right side)
        ScrollPane pollDetailScroller = new ScrollPane();
        pollDetailScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setFitToWidth(true);
        pollDetailScroller.setFitToHeight(true);
        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollView.fxml"));
        this.titleTxF = (TextField) pollDetail.lookup("#titleTxF");
        this.titleTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if (Client.poll != null
                    && !Client.poll.getTitle().equals(newValue)) {
                Client.poll.setTitle(newValue);
                Client.this.pollList.refresh();
                Client.pollHasChanges = true;
            }
        });

        this.pollRemoveBtn = (Button) pollListView.lookup("#pollRemoveBtn");
        this.pollRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Umfrage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    if(this.db.deletePoll(Client.poll))
                    {
                        this.polls.remove(Client.poll);
                        int index = this.pollList.getItems().indexOf(Client.poll);
                        this.pollList.getItems().remove(Client.poll);
                        if (index > 0)
                            this.pollList.getSelectionModel().select(--index);
                        else
                            this.pollList.getSelectionModel().selectFirst();
                        this.setPoll(null);
                    }
                }
            }, primaryStage);
        });

        this.pollSaveBtn = (Button) pollDetail.lookup("#pollSaveBtn");
        this.pollSaveBtn.setOnAction((ActionEvent ev) ->
        {
            if(this.db.savePoll(Client.poll))
                MsgBox.show("Bestätigung", "Die Umfrage wurde gespeichert!", null, primaryStage);
            else
                MsgBox.show("Fehlgeschlagen!", "Die Umfrage konnte nicht gespeichert werden!", null, primaryStage);
        });
        this.pollCancelBtn = (Button) pollDetail.lookup("#pollCancelBtn");
        this.pollCancelBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Änderungen wirklich verwerfen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    Poll edited = Client.poll;
                    try {
                        Poll reloaded = (Poll) this.db.getPollDao().queryForId(edited.getId());

                        if (Client.activePoll == edited)
                            Client.activePoll = reloaded;
                        this.polls.remove(edited);
                        this.polls.add(reloaded);
                        this.pollList.getItems().remove(edited);
                        this.pollList.getItems().add(reloaded);
                        this.pollList.getItems().sort((Poll poll1, Poll poll2) ->
                        {
                            return poll1.getTitle().compareTo(poll2.getTitle());
                        });
                        this.pollList.getSelectionModel().select(reloaded);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }, primaryStage);
        });

        this.descTxF = (TextArea) pollDetail.lookup("#descTxF");
        this.descTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if (Client.poll != null
                    && !Client.poll.getDescription().equals(newValue))
                Client.poll.setDescription(newValue);
        });
        this.createdDateTxF = (TextField) pollDetail.lookup("#createdDateTxF");
        this.createdTimeTxF = (TextField) pollDetail.lookup("#createdTimeTxF");
        this.stateCbo = (ComboBox<PollState>) pollDetail.lookup("#stateCbo");
        this.stateCbo.getItems().addAll(PollState.NEW, PollState.OPEN, PollState.CLOSED);
        this.stateCbo.setCellFactory((ListView<PollState> param) ->
        {
            return new PollStateListCell();
        });
        this.stateCbo.setButtonCell(new PollStateListCell());
        this.openBtn = (Button) pollDetail.lookup("#openBtn");
        this.openBtn.setOnAction((ActionEvent event) ->
        {
            ConfirmDialog.show("Bevor die Umfrage geöffnet werden kann, muss sie noch einmal gespeichert werden.\n" +
                    "\n" +
                    "Möchten Sie die Umfrage jetzt speichern?", confirmed ->
            {
                Client.poll.setState(PollState.OPEN);
                Client.activePoll = Client.poll;
                this.openBtn.setVisible(false);
                this.closeBtn.setVisible(true);
                this.stateCbo.setValue(Client.poll.getState());
                this.enableControls();
                this.pollList.refresh();
                this.db.savePoll(Client.poll);
                try {
                    spawnWebServer(Client.activePoll);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, primaryStage);
        });
        this.closeBtn = (Button) pollDetail.lookup("#closeBtn");
        this.closeBtn.setOnAction((ActionEvent event) ->
        {
            Client.poll.setState(PollState.CLOSED);
            Client.activePoll = null;
            this.closeBtn.setVisible(false);
            this.openBtn.setVisible(true);
            this.stateCbo.setValue(Client.poll.getState());
            this.enableControls();
            this.pollList.refresh();
            try {
                spawnWebServer(Client.activePoll);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.resultsBtn = (Button) pollDetail.lookup("#resultsBtn");
        this.resultsBtn.setOnAction((ActionEvent event) ->
        {
            EvaluationDialog.show(Client.poll);
        });

        this.questionsAccordion = (Accordion) pollDetail.lookup("#questionsAccordion");

        this.questionsAddBtn = (Button) pollDetail.lookup("#questionsAddBtn");
        this.questionsRemoveBtn = (Button) pollDetail.lookup("#questionsRemoveBtn");
        this.questionsAddBtn.setOnAction((ActionEvent event) ->
        {
            Question newQuestion = new Question("", true, QuestionType.SINGLE, Client.poll);
            Client.poll.getQuestions().add(newQuestion);
            newQuestion.setId(0);
            TitledPane tp = QuestionView.setQuestionView(this.questionsAccordion, newQuestion, Client.activePoll != null && Client.activePoll == Client.poll);

            this.questionsRemoveBtn.setOnAction((ActionEvent ev) ->
            {
                ConfirmDialog.show("Frage wirklich entfernen?", (boolean confirmed) ->
                {
                    if (confirmed) {
                        this.poll.getQuestions().remove(newQuestion);
                        this.questionsAccordion.getPanes().remove(tp);
                    }
                }, primaryStage);
            });
        });

        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        // Stage size and finally show
        if(this.pollList.getItems().isEmpty())
            this.setPoll(null);
        else
            this.pollList.getSelectionModel().selectFirst();
        primaryStage.setScene(new Scene(rootSplit));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public void setPoll(Poll newPoll) {
        Client.poll = newPoll;

        this.titleTxF.setText(Client.poll == null ? "" : Client.poll.getTitle());
        this.descTxF.setText(Client.poll == null ? "" : Client.poll.getDescription());

        SimpleDateFormat outputFormatDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat outputFormatTime = new SimpleDateFormat("HH:mm:ss");
        this.createdDateTxF.setText(Client.poll == null ? "" : outputFormatDate.format(Client.poll.getCreated()));
        this.createdTimeTxF.setText(Client.poll == null ? "" : outputFormatTime.format(Client.poll.getCreated()));
        this.stateCbo.setValue(Client.poll == null ? PollState.NEW : Client.poll.getState());
        this.openBtn.setVisible(Client.poll == null ? true : Client.poll.getState() == PollState.NEW || Client.poll.getState() == PollState.CLOSED);
        this.closeBtn.setVisible(Client.poll == null ? false : Client.poll.getState() == PollState.OPEN);
        this.enableControls();

        boolean disabled = Client.poll == null || (Client.activePoll != null && Client.activePoll == Client.poll);

        this.questionsAccordion.getPanes().clear();
        if(Client.poll != null)
            for (Question item : Client.poll.getQuestions())
                QuestionView.setQuestionView(this.questionsAccordion, item, disabled);
    }

    public static Poll getActivePoll() {
        return Client.activePoll;
    }

    public void enableControls() {
        boolean disable = Client.poll == null || (Client.activePoll != null && Client.activePoll == Client.poll);
        this.titleTxF.setDisable(disable);
        this.pollRemoveBtn.setDisable(disable);
        this.pollSaveBtn.setDisable(disable);
        this.pollCancelBtn.setDisable(disable);
        this.descTxF.setDisable(disable);
        this.createdDateTxF.setDisable(disable);
        this.createdTimeTxF.setDisable(disable);
        this.questionsAccordion.getPanes().clear();
        if(Client.poll != null)
            for (Question item : Client.poll.getQuestions())
                QuestionView.setQuestionView(this.questionsAccordion, item, disable);

        this.questionsAddBtn.setDisable(disable);
        this.questionsRemoveBtn.setDisable(disable);

        this.openBtn.setDisable(Client.poll == null || Client.activePoll != null);
        this.closeBtn.setDisable(!disable);
        this.resultsBtn.setDisable(Client.poll == null);
    }

    private void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }
}