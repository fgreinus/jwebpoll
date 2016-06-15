package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.Database;
import de.lebk.jwebpoll.Frontend;
import de.lebk.jwebpoll.client.CellRenderer.PollListCell;
import de.lebk.jwebpoll.client.CellRenderer.PollStateListCell;
import de.lebk.jwebpoll.client.Dialogs.ConfirmDialog;
import de.lebk.jwebpoll.client.Evaluation.EvaluationDialog;
import de.lebk.jwebpoll.data.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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

    //- Data -
    private static final Logger LOGGER = Logger.getLogger(Client.class);
    private List<Poll> polls = new ArrayList<>();
    private static Poll poll; // Poll selected in client
    private static Poll activePoll; // Poll running on server

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

    //- Dialogs -
    private EvaluationDialog evaluationDialog;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JWebPoll");
        primaryStage.setOnCloseRequest((WindowEvent we) ->
        {
            try {
                Frontend.kill();

                if(this.evaluationDialog != null)
                    this.evaluationDialog.close();

            } catch (Exception e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Front end kill failed: ", e);
                }
                e.printStackTrace();
            }
        });
        primaryStage.getIcons().add(new Image(Client.class.getResource("/icon.png").toString()));

        this.polls.addAll(Database.DB.getPollDao().queryForAll());
        for (Poll p : this.polls) {
            if (p.getState() == PollState.OPEN) {
                p.setState(PollState.CLOSED);
                //Client.activePoll = p;
                //break;
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
        this.pollAddBtn.setOnAction((ActionEvent ev) -> this.newPoll());
        this.pollRemoveBtn = (Button) pollListView.lookup("#pollRemoveBtn");
        this.pollRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Umfrage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    if (Database.DB.deletePoll(Client.poll)) {
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

        //- MenuBar -
        MenuBar menuBar = (MenuBar) rootGrid.lookup("#menuBar");
        //-- Menu Poll --
        Menu menuPoll = new Menu("Umfrage");
        MenuItem newPoll = new MenuItem("Neu");
        newPoll.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN));
        newPoll.setOnAction(event -> this.newPoll());
        MenuItem results = new MenuItem("Ergebnisse");
        results.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN));
        results.setOnAction(event -> this.openResults());
        menuPoll.getItems().addAll(newPoll, results);
        //-- Menu About --
        Menu menuAbout = new Menu("Über");
        MenuItem help = new MenuItem("Hilfe");
        help.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        help.setOnAction((ActionEvent event) -> InfoSiteHelper.show("Help"));
        MenuItem about = new MenuItem("Über");
        about.setOnAction((ActionEvent event) -> InfoSiteHelper.show("About"));
        about.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN));
        MenuItem license = new MenuItem("Lizenzen");
        license.setOnAction((ActionEvent event) -> InfoSiteHelper.show("License"));
        license.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCodeCombination.CONTROL_DOWN));
        menuAbout.getItems().addAll(help, about, license);
        menuBar.getMenus().addAll(menuPoll, menuAbout);

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
            this.titledPanes.put(tp, newQuestion);
            this.questionsAccordion.setExpandedPane(tp);
        });

        this.questionsRemoveBtn = (Button) pollDetail.lookup("#questionsRemoveBtn");
        this.questionsRemoveBtn.setOnAction((ActionEvent ev) ->
        {
            ConfirmDialog.show("Frage wirklich entfernen?", (boolean confirmed) ->
            {
                if (confirmed) {
                    Question toRemove = this.titledPanes.get(this.questionsAccordion.getExpandedPane());
                    try {
                        Database.DB.getQuestionDao().delete(toRemove);
                        for (Answer answer : toRemove.getAnswers()) {
                            Database.DB.getAnswerDao().delete(answer);
                        }
                        Client.poll.getQuestions().remove(toRemove);
                        this.titledPanes.remove(this.questionsAccordion.getExpandedPane());
                        this.questionsAccordion.getPanes().remove(this.questionsAccordion.getExpandedPane());
                    } catch (SQLException ex) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Removing question failed: ", ex);
                        }
                        ex.printStackTrace();
                    }
                }
            }, primaryStage);
        });
        this.questionsAccordion.expandedPaneProperty().addListener((observable, oldValue, newValue) -> this.questionsRemoveBtn.setDisable(newValue == null));

        this.stateCbo = (ComboBox<PollState>) pollDetail.lookup("#stateCbo");
        this.stateCbo.getItems().addAll(PollState.NEW, PollState.OPEN, PollState.CLOSED);
        this.stateCbo.setCellFactory((ListView<PollState> param) -> new PollStateListCell());
        this.stateCbo.setButtonCell(new PollStateListCell());

        this.linkCbo = (ComboBox<String>) pollDetail.lookup("#linkCbo");
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            Enumeration<InetAddress> addresses;
            while (networkInterfaces.hasMoreElements()) {
                addresses = networkInterfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    this.linkCbo.getItems().add(addresses.nextElement().getHostAddress() + (Frontend.PORT != 80 ? ":" + Frontend.PORT : ""));
                }
            }
        } catch (SocketException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Getting network addresses failed: ", ex);
            }
            ex.printStackTrace();
        }
        this.linkCbo.setEditable(true);
        this.linkCbo.getEditor().setEditable(false);
        this.linkCbo.getSelectionModel().selectFirst();
        for(int i = 0; i < this.linkCbo.getItems().size(); i++)
            if(!this.linkCbo.getItems().get(i).startsWith(Frontend.LOCALHOST_V4) && !this.linkCbo.getItems().get(i).startsWith(Frontend.LOCALHOST_V6))
            {
                // Select first non-localhost address
                this.linkCbo.getSelectionModel().select(i);
                break;
            }
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
                Database.DB.getPollDao().update(Client.poll);
            } catch (SQLException ex) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Opening poll failed: ", ex);
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
            this.openResults();
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
        this.questionsRemoveBtn.setDisable(disable || Client.poll.getQuestions().isEmpty() || this.questionsAccordion.getExpandedPane() == null);

        if(disable)
        {
            int selectedIndex = this.linkCbo.getSelectionModel().getSelectedIndex();
            this.linkCbo.setOnAction((ActionEvent ev) ->
            {
                this.linkCbo.getSelectionModel().select(selectedIndex);
            });
        }
        else
            this.linkCbo.setOnAction(null);
        this.openBtn.setDisable(Client.poll == null || Client.activePoll != null);
        this.closeBtn.setDisable(!disable);
        this.resultsBtn.setDisable(Client.poll == null);
    }

    private void newPoll()
    {
        Poll newPoll = new Poll("", "", PollState.NEW);
        try {
            Database.DB.getPollDao().create(newPoll);
            Client.poll = newPoll;
            this.polls.add(Client.poll);
            this.pollList.getItems().addAll(Client.poll);
            this.pollList.getSelectionModel().select(Client.poll);
        } catch (SQLException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Adding poll failed: ", ex);
            }
            ex.printStackTrace();
        }
    }

    private void openResults()
    {
        if(this.evaluationDialog != null)
            evaluationDialog.toFront();
        else
        {
            this.evaluationDialog = new EvaluationDialog(Client.poll.getId());
            this.evaluationDialog.setOnCloseRequest(event -> this.evaluationDialog = null);
        }
    }

    private String getSelectedAddress()
    {
        return this.linkCbo.getSelectionModel().getSelectedItem().endsWith(":" + Frontend.PORT) ? this.linkCbo.getSelectionModel().getSelectedItem().substring(0, this.linkCbo.getSelectionModel().getSelectedItem().lastIndexOf(':')) : this.linkCbo.getSelectionModel().getSelectedItem();
    }
}