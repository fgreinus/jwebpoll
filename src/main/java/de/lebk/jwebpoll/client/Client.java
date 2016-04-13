package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
    private TextField createdDateTxF, createdTimeTxF;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("JWebPoll");

        polls.add(new Poll("Neu", "", (short) 0));
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
        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollView.fxml"));
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
        this.createdDateTxF = (TextField) pollDetail.lookup("#createdDateTxF");
        this.createdTimeTxF = (TextField) pollDetail.lookup("#createdTimeTxF");
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

            SimpleDateFormat outputFormatDate = new SimpleDateFormat("dd.MM.yyyy");
            SimpleDateFormat outputFormatTime = new SimpleDateFormat("HH:mm:ss");
            this.createdDateTxF.setText(outputFormatDate.format(this.poll.getCreated()));
            this.createdTimeTxF.setText(outputFormatTime.format(this.poll.getCreated()));

            Text numTxt;
            TextField titleTxt;
            TextField hintTxt;
            Question q;
            for (int i = 0; i < this.poll.getQuestions().size(); i++)
            {
                q = this.poll.getQuestions().get(i);
                try
                {
                    GridPane questionView = (GridPane) FXMLLoader.load(Client.this.getClass().getResource("/client/questionView.fxml"));
                    numTxt = (Text) questionView.lookup("#numTxt");
                    titleTxt = (TextField) questionView.lookup("#titleTxF");
                    hintTxt = (TextField) questionView.lookup("#hintTxF");

                    numTxt.setText(String.format("%d.", i));
                    titleTxt.setText(q.getTitle());
                    hintTxt.setText(q.getHint());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
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