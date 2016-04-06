package de.lebk.jwebpoll.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application
{
    public static void main(String[] args)
    {
        Client.launch(args);
    }

    private ListView<String> pollList;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("JWebPoll");

        SplitPane rootSplit = (SplitPane) FXMLLoader.load(this.getClass().getResource("/client/client.fxml")); //root.lookup("#rootSplit");
        this.pollList = new ListView<String>();
        this.pollList.getItems().add("1. Umfrage");
        this.pollList.getItems().add("Bundestagswahlen");
        this.pollList.getItems().add("Mittag-Menü-Wahl");
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
}