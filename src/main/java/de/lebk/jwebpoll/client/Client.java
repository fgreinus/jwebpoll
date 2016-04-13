package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Poll;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class Client extends Application
{
    //- Main -
    public static void main(String[] args)
    {
        Client.launch(args);
    }

    //- Data -
    private List<Poll> polls = new ArrayList<>();
    private Poll poll;

    //- View -
    private ListView<Poll> pollList = new ListView<>();
    private TextField titleTxF;
    private TextArea descTxF;
    private TextField createdTxF;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("JWebPoll");

        polls.add(new Poll("1. Umfrage", "Eine Beschreibung", (short) 0));
        polls.add(new Poll("Bundestagswahl", "Kurze Beschreibung", (short) 0));

        SplitPane rootSplit = (SplitPane) FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        this.pollList.setCellFactory(new Callback<ListView<Poll>, ListCell<Poll>>()
        {
            @Override
            public ListCell<Poll> call(ListView<Poll> param)
            {
                return new PollListCell();
            }
        });
        for (Poll p : this.polls)
        {
            this.pollList.getItems().add(p);
        }
        this.pollList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Poll> observable, Poll oldValue, Poll newValue) ->
        {
            Client.this.setPoll(newValue);
        });
        rootSplit.getItems().add(this.pollList);
        rootSplit.setDividerPositions(1d / 3d);

        ScrollPane pollDetailScroller = new ScrollPane();
        pollDetailScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setFitToWidth(true);
        pollDetailScroller.setFitToHeight(true);
        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollDetail.fxml"));
        this.titleTxF = (TextField) pollDetail.lookup("#titleTxF");
        this.titleTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if(Client.this.poll != null
            && !Client.this.poll.getTitle().equals(newValue))
                Client.this.poll.setTitle(newValue);
        });
        this.descTxF = (TextArea) pollDetail.lookup("#descTxF");
        this.descTxF.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if(Client.this.poll != null
            && !Client.this.poll.getDescription().equals(newValue))
                Client.this.poll.setDescription(newValue);
        });
        this.createdTxF = (TextField) pollDetail.lookup("#createdTxF");
        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        primaryStage.setScene(new Scene(rootSplit, 800, 600));
        primaryStage.show();
    }

    public void setPoll(Poll newPoll)
    {
        this.poll = newPoll;
        if(this.poll != null)
        {
            this.titleTxF.setText(this.poll.getTitle());
            this.descTxF.setText(this.poll.getDescription());

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            this.createdTxF.setText(outputFormat.format(this.poll.getCreated()));
        }
    }

    //- Classes -
    private class PollListCell extends ListCell<Poll>
    {
        @Override
        protected void updateItem(Poll item, boolean empty)
        {
            super.updateItem(item, empty);

            if(item != null)
                this.setText(item.getTitle());
        }
    }
}