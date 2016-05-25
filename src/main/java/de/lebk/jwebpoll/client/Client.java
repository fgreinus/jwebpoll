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
    private Poll poll;
    //poll running on server
    private Poll activePoll;

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

//            haustier.getVotes().add(vote1);
//            haustier.getVotes().add(vote2);
//            nachbar.getVotes().add(vote3);
//            kanzlerkandidat.getAnswers().add(merkel);
//            kanzlerkandidat.getAnswers().add(trump);
//            kanzlerkandidat.getAnswers().add(haustier);
//            kanzlerkandidat.getAnswers().add(nachbar);
//            bundestagswahl.getQuestions().add(kanzlerkandidat);
                this.polls.add(bundestagswahl);
            }
        }

        for (Poll p : this.polls) {
            if (p.getState() == PollState.OPEN) {
                this.activePoll = p;
                try {
                    spawnWebServer(activePoll);
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
            if (Client.this.poll != null
                    && !Client.this.poll.getTitle().equals(newValue)) {
                Client.this.poll.setTitle(newValue);
                //Client.this.pollList.refresh();
            }
        });

        this.pollRemoveBtn = (Button) pollDetail.lookup("#pollRemoveBtn");
        this.pollRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Umfrage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    try {
                        this.pollDao.delete(this.poll);
                        this.polls.remove(this.poll);
                        int index = this.pollList.getItems().indexOf(this.poll);
                        this.pollList.getItems().remove(this.poll);
                        if (index > 0)
                            this.pollList.getSelectionModel().select(--index);
                        else
                            this.pollList.getSelectionModel().selectFirst();
                        this.poll = null;
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        this.descTxF = (TextArea) pollDetail.lookup("#descTxF");
        this.descTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if (Client.this.poll != null
                    && !Client.this.poll.getDescription().equals(newValue))
                Client.this.poll.setDescription(newValue);
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
            this.poll.setState(PollState.OPEN);
            this.activePoll = this.poll;
            this.openBtn.setVisible(false);
            this.closeBtn.setVisible(true);
            this.stateCbo.setValue(this.poll.getState());
            this.enableControls();
            try {

                spawnWebServer(activePoll);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.closeBtn = (Button) pollDetail.lookup("#closeBtn");
        this.closeBtn.setOnAction((ActionEvent event) ->
        {
            this.poll.setState(PollState.CLOSED);
            this.activePoll = null;
            this.closeBtn.setVisible(false);
            this.openBtn.setVisible(true);
            this.stateCbo.setValue(this.poll.getState());
            this.enableControls();
            try {
                spawnWebServer(activePoll);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.resultsBtn = (Button) pollDetail.lookup("#resultsBtn");
        this.resultsBtn.setOnAction((ActionEvent event) ->
        {
            //TODO View Results
            ArrayList<Map.Entry<Answer, Integer>> answerCounter = new ArrayList<Map.Entry<Answer, Integer>>();
            for (Question question : Client.this.poll.questions) {
                System.out.println(question.getTitle());
                for (Answer answer : question.getAnswers()) {
                    System.out.println("  " + answer.getText() + ": " + answer.getVotes().size());
                }
            }
        });

        this.questionsAccordion = (Accordion) pollDetail.lookup("#questionsAccordion");

        this.questionsAddBtn = (Button) pollDetail.lookup("#questionsAddBtn");
        this.questionsAddBtn.setOnAction((ActionEvent event) ->
        {
            Question newQuestion = new Question("", true, QuestionType.SINGLE, this.poll);
            this.poll.getQuestions().add(newQuestion);
            QuestionView.setQuestionView(this.questionsAccordion, newQuestion, this.activePoll != null && this.activePoll == this.poll);
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
        this.poll = newPoll;

        this.titleTxF.setText(this.poll.getTitle());
        this.descTxF.setText(this.poll.getDescription());

        SimpleDateFormat outputFormatDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat outputFormatTime = new SimpleDateFormat("HH:mm:ss");
        this.createdDateTxF.setText(outputFormatDate.format(this.poll.getCreated()));
        this.createdTimeTxF.setText(outputFormatTime.format(this.poll.getCreated()));
        this.stateCbo.setValue(this.poll.getState());
        this.openBtn.setVisible(this.poll.getState() == PollState.NEW || this.poll.getState() == PollState.CLOSED);
        this.closeBtn.setVisible(this.poll.getState() == PollState.OPEN);
        this.enableControls();

        boolean disabled = this.activePoll != null && this.activePoll == this.poll;

        this.questionsAccordion.getPanes().clear();
        for (Question item : this.poll.getQuestions())
            QuestionView.setQuestionView(this.questionsAccordion, item, disabled);
    }

    public void enableControls() {
        boolean disable = this.activePoll != null && this.activePoll == this.poll;
        this.titleTxF.setDisable(disable);
        this.pollRemoveBtn.setDisable(disable);
        this.descTxF.setDisable(disable);
        this.createdDateTxF.setDisable(disable);
        this.createdTimeTxF.setDisable(disable);
        this.openBtn.setDisable(this.activePoll != null);
        this.closeBtn.setDisable(!disable);

        this.questionsAccordion.getPanes().clear();
        for (Question item : this.poll.getQuestions())
            QuestionView.setQuestionView(this.questionsAccordion, item, disable);

        this.questionsAddBtn.setDisable(disable);
    }

    private void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }
}