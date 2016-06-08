package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.Frontend;
import de.lebk.jwebpoll.data.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Client extends Application {
    //- Main -
    public static void main(String[] args) {
        Client.launch(args);
    }

    final static Logger logger = Logger.getLogger(Client.class);
    //- Data -
    private List<Poll> polls = new ArrayList<>();
    //poll selected in client window
    private static Poll poll;
    //poll running on server
    private static Poll activePoll;

    //- DB -
    private Database db;

    //- View -
    private ListView<Poll> pollList;
    private Button pollAddBtn, pollRemoveBtn;
    private TextField titleTxF;
    private ComboBox<String> linkCbo;
    private TextArea descTxF;
    private TextField createdDateTxF, createdTimeTxF;
    private ComboBox<PollState> stateCbo;
    private Button openBtn, closeBtn, resultsBtn;
    private Accordion questionsAccordion;
    private Button questionsAddBtn, questionsRemoveBtn;

    private Hashtable<TitledPane, Question> titledPanes = new Hashtable<>();

    @Override
    public void start(Stage primaryStage) throws Exception {


        primaryStage.setTitle("JWebPoll");
        primaryStage.setOnCloseRequest((WindowEvent we) ->
        {
            try {
                Frontend.kill();
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Front end kill failed: ", e);
                }
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
                break;
            }
        }

        // ListView (Left side)
        GridPane rootGrid = FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        SplitPane rootSplit = (SplitPane) rootGrid.lookup("#rootSplit");
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
            Poll newPoll = new Poll("", "", PollState.NEW);
            try {
                this.db.getPollDao().create(newPoll);
                Client.poll = newPoll;
                this.polls.add(Client.poll);
                this.pollList.getItems().addAll(Client.poll);
                this.pollList.getSelectionModel().select(Client.poll);
            } catch (SQLException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding poll failed: ", ex);
                }
                ex.printStackTrace();
            }
        });
        this.pollRemoveBtn = (Button) pollListView.lookup("#pollRemoveBtn");
        this.pollRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Umfrage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    if (this.db.deletePoll(Client.poll)) {
                        this.polls.remove(Client.poll);
                        int index = this.pollList.getItems().indexOf(Client.poll);
                        this.pollList.getItems().remove(Client.poll);
                        if (index > 0)
                            this.pollList.getSelectionModel().select(--index);
                        else
                            this.pollList.getSelectionModel().selectFirst();
                        this.setPoll(this.pollList.getSelectionModel().getSelectedItem());
                    }
                }
            }, primaryStage);
        });

        rootSplit.getItems().add(pollListView);
        rootSplit.setDividerPositions(1d / 5d);

        //Menubar
        MenuBar menuBar = (MenuBar) rootGrid.lookup("#menuBar");
        // --- Menu Hilfe
        Menu menuHelp = new Menu("Über");
        MenuItem about = new MenuItem("Über");
        about.setOnAction((ActionEvent event) ->
        {
            InfoSiteHelper.show("about");
        });
        MenuItem help = new MenuItem("Hilfe");
        help.setOnAction((ActionEvent event) ->
        {
            InfoSiteHelper.show("help");
        });
        MenuItem license = new MenuItem("Lizenzen");
        license.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                InfoSiteHelper.show("license");
            }
        });
        menuHelp.getItems().addAll(about, help, license);
        menuBar.getMenus().addAll(menuHelp);

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

        this.descTxF = (TextArea) pollDetail.lookup("#descTxF");
        this.descTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if (Client.poll != null
                    && !Client.poll.getDescription().equals(newValue))
                Client.poll.setDescription(newValue);
        });
        this.createdDateTxF = (TextField) pollDetail.lookup("#createdDateTxF");
        this.createdTimeTxF = (TextField) pollDetail.lookup("#createdTimeTxF");

        this.questionsAccordion = (Accordion) pollDetail.lookup("#questionsAccordion");
        this.questionsAddBtn = (Button) pollDetail.lookup("#questionsAddBtn");
        this.questionsAddBtn.setOnAction((ActionEvent event) ->
        {
            Question newQuestion = new Question("", true, QuestionType.SINGLE, Client.poll);
            Client.poll.getQuestions().add(newQuestion);
            TitledPane tp = QuestionView.setQuestionView(this.questionsAccordion, newQuestion, Client.activePoll != null && Client.activePoll == Client.poll);
            titledPanes.put(tp, newQuestion);
        });

        this.questionsRemoveBtn = (Button) pollDetail.lookup("#questionsRemoveBtn");
        this.questionsRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Frage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    Question toRemove = titledPanes.get(this.questionsAccordion.getExpandedPane());
                    try {
                        this.db.getQuestionDao().delete(toRemove);
                        for (Answer answer : toRemove.getAnswers()) {
                            this.db.getAnswerDao().delete(answer);
                        }
                        Client.poll.getQuestions().remove(toRemove);
                        titledPanes.remove(this.questionsAccordion.getExpandedPane());
                        this.questionsAccordion.getPanes().remove(this.questionsAccordion.getExpandedPane());
                    } catch (SQLException ex) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Removing question failed: ", ex);
                        }
                        ex.printStackTrace();
                    }
                }
            }, primaryStage);
        });
        this.questionsAccordion.expandedPaneProperty().addListener((observable, oldValue, newValue) ->
        {
            this.questionsRemoveBtn.setDisable(newValue == null);
        });

        this.stateCbo = (ComboBox<PollState>) pollDetail.lookup("#stateCbo");
        this.stateCbo.getItems().addAll(PollState.NEW, PollState.OPEN, PollState.CLOSED);
        this.stateCbo.setCellFactory((ListView<PollState> param) ->
        {
            return new PollStateListCell();
        });
        this.stateCbo.setButtonCell(new PollStateListCell());

        this.linkCbo = (ComboBox<String>) pollDetail.lookup("#linkCbo");
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Enumeration<InetAddress> addresses;
            while (networkInterfaces.hasMoreElements()) {
                addresses = networkInterfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    this.linkCbo.getItems().add(addresses.nextElement().getHostAddress() + ":" + Frontend.PORT);
                }
            }
        } catch (SocketException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Getting network addresses failed: ", ex);
            }
            ex.printStackTrace();
        }
        this.linkCbo.getSelectionModel().selectFirst();
        if (Client.activePoll != null) {
            new Frontend(Client.activePoll, this.getSelectedAddress());
        }
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
                this.db.getPollDao().update(Client.poll);
            } catch (SQLException ex) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Opening poll failed: ", ex);
                }
                ex.printStackTrace();
            }
            new Frontend(Client.activePoll, this.getSelectedAddress());
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
            Frontend.kill();
        });
        this.resultsBtn = (Button) pollDetail.lookup("#resultsBtn");
        this.resultsBtn.setOnAction((ActionEvent event) ->
        {
            EvaluationDialog.show(Client.poll.getId());
        });

        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        // Stage size and finally show
        if (this.pollList.getItems().isEmpty())
            this.setPoll(null);
        else
            this.pollList.getSelectionModel().selectFirst();
        primaryStage.setScene(new Scene(rootGrid));
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
        if (Client.poll != null)
            for (Question question : Client.poll.getQuestions()) {
                TitledPane tp = QuestionView.setQuestionView(this.questionsAccordion, question, disabled);
                this.titledPanes.put(tp, question);
            }
    }

    public static Poll getActivePoll() {
        return Client.activePoll;
    }

    public void enableControls() {
        boolean disable = Client.poll == null || (Client.activePoll != null && Client.activePoll == Client.poll);
        this.titleTxF.setDisable(disable);
        this.pollRemoveBtn.setDisable(disable);
        this.descTxF.setDisable(disable);
        this.createdDateTxF.setDisable(disable);
        this.createdTimeTxF.setDisable(disable);
        this.questionsAccordion.getPanes().clear();
        if (Client.poll != null)
            for (Question question : Client.poll.getQuestions()) {
                TitledPane tp = QuestionView.setQuestionView(this.questionsAccordion, question, disable);
                this.titledPanes.put(tp, question);
            }

        this.questionsAddBtn.setDisable(disable);
        this.questionsRemoveBtn.setDisable(disable || Client.poll.getQuestions().isEmpty());

        this.linkCbo.setDisable(disable);
        this.openBtn.setDisable(Client.poll == null || Client.activePoll != null);
        this.closeBtn.setDisable(!disable);
        this.resultsBtn.setDisable(Client.poll == null);
    }

    private String getSelectedAddress()
    {
        return this.linkCbo.getSelectionModel().getSelectedItem().substring(0, this.linkCbo.getSelectionModel().getSelectedItem().lastIndexOf(':'));
    }
}