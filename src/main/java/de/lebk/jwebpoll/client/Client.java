package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Poll;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

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

    private StringProperty title = new SimpleStringProperty();

    //- View -
    private ListView<Poll> pollList = new ListView<>();

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("JWebPoll");

        title.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
        {
            if(Client.this.poll != null)
                Client.this.poll.setTitle(newValue);
        });

        //polls.add(new Poll("1. Umfrage", "Eine Beschreibung", (short) 0));
        //polls.add(new Poll("Bundestagswahl", "Kurze Beschreibung", (short) 0));

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
        this.pollList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Poll>()
        {
            @Override
            public void changed(ObservableValue<? extends Poll> observable, Poll oldValue, Poll newValue)
            {
                //TODO Handle oldValue!
                Client.this.setPoll(newValue);
            }
        });
        rootSplit.getItems().add(this.pollList);
        rootSplit.setDividerPositions(1d / 3d);

        ScrollPane pollDetailScroller = new ScrollPane();
        pollDetailScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pollDetailScroller.setFitToWidth(true);
        pollDetailScroller.setFitToHeight(true);
        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollDetail.fxml"));
        pollDetailScroller.setContent(pollDetail);
        rootSplit.getItems().add(pollDetailScroller);

        primaryStage.setScene(new Scene(rootSplit, 800, 600));
        primaryStage.show();
    }

    public void setPoll(Poll newPoll)
    {
        this.poll = newPoll;
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