package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.Frontend;
import de.lebk.jwebpoll.data.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application {
    //- Main -
    public static void main(String[] args) {
        Client.launch(args);
    }

    //- Data -
    private List<Poll> polls = new ArrayList<>();
    //poll selected in client window
    private Poll poll;
    //poll running on serve
    private Poll activePoll;

    //- View -
    private ListView<Poll> pollList = new ListView<>();
    private TextField titleTxF;
    private TextArea descTxF;
    private TextField createdDateTxF, createdTimeTxF;
    private ComboBox<PollState> stateCbo;
    private Button openBtn, closeBtn, resultsBtn;
    private Accordion questionsAccordion;
    private Button questionsAddBtn;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Title
        primaryStage.setTitle("JWebPoll");

        //Default Poll: new poll
        Poll newPoll = new Poll(0, "Neue Umfrage", "", PollState.NEW);
        this.polls.add(newPoll);

        //Example polls (to be deleted in future)
        this.polls.add(new Poll(0, "1. Umfrage", "Eine Beschreibung", PollState.OPEN));
        this.polls.add(new Poll(0, "Bundestagswahl", "Kurze Beschreibung", PollState.CLOSED));

        for (Poll p : this.polls) {
            if (p.getState() == PollState.OPEN) {
                this.activePoll = p;
                try {
                    spawnDatabase();
                    spawnWebServer(activePoll);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

        //ListView (Left side)
        SplitPane rootSplit = (SplitPane) FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        this.pollList.setCellFactory((ListView<Poll> param) ->
        {
            return new PollListCell();
        });
        for (Poll p : this.polls) {
            this.pollList.getItems().add(p);
        }
        this.pollList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Poll> observable, Poll oldValue, Poll newValue) ->
        {
            if(newValue != null)
                Client.this.setPoll(newValue);
        });
        rootSplit.getItems().add(this.pollList);
        rootSplit.setDividerPositions(1d / 5d);

        //PollView (Right side)
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
                    && !Client.this.poll.getTitle().equals(newValue))
                Client.this.poll.setTitle(newValue);
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
                spawnDatabase();
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
                spawnDatabase();
                spawnWebServer(activePoll);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.resultsBtn = (Button) pollDetail.lookup("#resultsBtn");
        this.resultsBtn.setOnAction((ActionEvent event) ->
        {
            //TODO View Results
        });

        this.questionsAccordion = (Accordion) pollDetail.lookup("#questionsAccordion");

        this.questionsAddBtn = (Button) pollDetail.lookup("#questionsAddBtn");
        this.questionsAddBtn.setOnAction((ActionEvent event) ->
        {
            Question newQuestion = new Question("", true, QuestionType.SINGLE);
            this.poll.getQuestions().add(newQuestion);
            TitledPane tp = new TitledPane();
            QuestionView.setQuestionView(tp, newQuestion, this.activePoll != null && this.activePoll == this.poll);
            this.questionsAccordion.getPanes().add(tp);
            this.questionsAccordion.setExpandedPane(tp);
        });
        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        //Stage size and finally show
        this.pollList.getSelectionModel().selectFirst();
        primaryStage.setScene(new Scene(rootSplit, 800, 600));
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
        for(Question item : this.poll.getQuestions())
        {
            TitledPane tp = new TitledPane();
            QuestionView.setQuestionView(tp, item, disabled);
            this.questionsAccordion.getPanes().add(tp);
            this.questionsAccordion.setExpandedPane(tp);
        }
    }

    public void enableControls() {
        boolean disable = this.activePoll != null && this.activePoll == this.poll;
        this.titleTxF.setDisable(disable);
        this.descTxF.setDisable(disable);
        this.createdDateTxF.setDisable(disable);
        this.createdTimeTxF.setDisable(disable);
        this.openBtn.setDisable(this.activePoll != null);
        this.closeBtn.setDisable(!disable);

        //TODO Enable / disable Accordion

        this.questionsAddBtn.setDisable(disable);
    }

    private void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }

    private void spawnDatabase() throws Exception {
        Database.getInstance();
    }
}