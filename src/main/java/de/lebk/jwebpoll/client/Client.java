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
import java.util.List;
import java.util.Map;

public class Client extends Application {
    //- Main -
    public static void main(String[] args) {
        Client.launch(args);
    }

    //- Data -
    private List<Poll> polls = new ArrayList<>();
    //poll selected in client window
    private static Poll poll;
    //poll running on server
    private static Poll activePoll;

    //- DataAccessObjects -
    Dao pollDao;
    Dao questionDao;
    Dao answerDao;
    Dao voteDao;

    //- View -
    private ListView<Poll> pollList;
    private Button pollAddBtn;
    private TextField titleTxF;
    private Button pollRemoveBtn;
    private TextArea descTxF;
    private TextField createdDateTxF, createdTimeTxF;
    private ComboBox<PollState> stateCbo;
    private Button openBtn, closeBtn, resultsBtn;
    private Accordion questionsAccordion;
    private Button questionsAddBtn;

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
        Database db = Database.getInstance();
        this.pollDao = db.getDaoForClass(Poll.class.getName());
        this.questionDao = db.getDaoForClass(Question.class.getName());
        this.answerDao = db.getDaoForClass(Answer.class.getName());
        this.voteDao = db.getDaoForClass(Vote.class.getName());

        Poll bundestagswahl = new Poll("Bundestagswahl", "Kurze Beschreibung", PollState.NEW);
        this.polls.addAll(this.pollDao.queryForAll());
        boolean addDefaultPoll = this.polls.isEmpty();

        if (addDefaultPoll) {
            for (Poll p : this.polls) {
                if (p.getTitle().equals(bundestagswahl.getTitle()))
                    addDefaultPoll = false;
            }

            if (addDefaultPoll) {
                this.pollDao.create(bundestagswahl);

                Question kanzlerkandidat = new Question("Wer ist ihr Kanzlerkandidat?", true, QuestionType.SINGLE, bundestagswahl);
                this.questionDao.create(kanzlerkandidat);

                Answer merkel = new Answer("Merkel", -100, kanzlerkandidat);
                this.answerDao.create(merkel);
                Answer trump = new Answer("Trump", -666, kanzlerkandidat);
                this.answerDao.create(trump);
                Answer haustier = new Answer("Mein Haustier", 1000, kanzlerkandidat);
                this.answerDao.create(haustier);
                Answer nachbar = new Answer("Mein Nachbar", 10, kanzlerkandidat);
                this.answerDao.create(nachbar);
                Vote vote1 = new Vote("DefaultVoter1", kanzlerkandidat, haustier, "");
                this.voteDao.create(vote1);
                Vote vote2 = new Vote("DefaultVoter2", kanzlerkandidat, haustier, "");
                this.voteDao.create(vote2);
                Vote vote3 = new Vote("DefaultVoter3", kanzlerkandidat, nachbar, "");
                this.voteDao.create(vote3);

                bundestagswahl = (Poll) pollDao.queryForId(bundestagswahl.getId());

                this.polls.add(bundestagswahl);
            }
        }

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
            if (newValue != null)
                Client.this.setPoll(newValue);
        });

        this.pollAddBtn = (Button) pollListView.lookup("#pollAddBtn");
        this.pollAddBtn.setOnAction((ActionEvent ev) ->
        {
            Poll newPoll = new Poll("<Neue Umfrage>", "", PollState.NEW);
            this.polls.add(newPoll);
            try {
                this.pollDao.create(newPoll);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            this.pollList.getItems().addAll(newPoll);
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
            }
        });

        this.pollRemoveBtn = (Button) pollDetail.lookup("#pollRemoveBtn");
        this.pollRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Umfrage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    try {
                        this.pollDao.delete(Client.poll);
                        this.polls.remove(Client.poll);
                        int index = this.pollList.getItems().indexOf(Client.poll);
                        this.pollList.getItems().remove(Client.poll);
                        if (index > 0)
                            this.pollList.getSelectionModel().select(--index);
                        else
                            this.pollList.getSelectionModel().selectFirst();
                        Client.poll = null;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
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
            Client.poll.setState(PollState.OPEN);
            Client.activePoll = Client.poll;
            this.openBtn.setVisible(false);
            this.closeBtn.setVisible(true);
            this.stateCbo.setValue(Client.poll.getState());
            this.enableControls();
            this.pollList.refresh();
            try {
                spawnWebServer(Client.activePoll);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            //TODO View Results
            EvaluationDialog.show(Client.poll);
        });

        this.questionsAccordion = (Accordion) pollDetail.lookup("#questionsAccordion");

        this.questionsAddBtn = (Button) pollDetail.lookup("#questionsAddBtn");
        this.questionsAddBtn.setOnAction((ActionEvent event) ->
        {
            Question newQuestion = new Question("", true, QuestionType.SINGLE, Client.poll);
            Client.poll.getQuestions().add(newQuestion);
            QuestionView.setQuestionView(this.questionsAccordion, newQuestion, Client.activePoll != null && Client.activePoll == Client.poll);
        });
        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        // Stage size and finally show
        this.pollList.getSelectionModel().selectFirst();
        primaryStage.setScene(new Scene(rootSplit));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public void setPoll(Poll newPoll) {
        Client.poll = newPoll;

        this.titleTxF.setText(Client.poll.getTitle());
        this.descTxF.setText(Client.poll.getDescription());

        SimpleDateFormat outputFormatDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat outputFormatTime = new SimpleDateFormat("HH:mm:ss");
        this.createdDateTxF.setText(outputFormatDate.format(Client.poll.getCreated()));
        this.createdTimeTxF.setText(outputFormatTime.format(Client.poll.getCreated()));
        this.stateCbo.setValue(Client.poll.getState());
        this.openBtn.setVisible(Client.poll.getState() == PollState.NEW || Client.poll.getState() == PollState.CLOSED);
        this.closeBtn.setVisible(Client.poll.getState() == PollState.OPEN);
        this.enableControls();

        boolean disabled = Client.activePoll != null && Client.activePoll == Client.poll;

        this.questionsAccordion.getPanes().clear();
        for (Question item : Client.poll.getQuestions())
            QuestionView.setQuestionView(this.questionsAccordion, item, disabled);
    }

    public static Poll getActivePoll() {
        return Client.activePoll;
    }

    public void enableControls() {
        boolean disable = Client.activePoll != null && Client.activePoll == Client.poll;
        this.titleTxF.setDisable(disable);
        this.pollRemoveBtn.setDisable(disable);
        this.descTxF.setDisable(disable);
        this.createdDateTxF.setDisable(disable);
        this.createdTimeTxF.setDisable(disable);
        this.openBtn.setDisable(Client.activePoll != null);
        this.closeBtn.setDisable(!disable);

        this.questionsAccordion.getPanes().clear();
        for (Question item : Client.poll.getQuestions())
            QuestionView.setQuestionView(this.questionsAccordion, item, disable);

        this.questionsAddBtn.setDisable(disable);
    }

    private void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }
}