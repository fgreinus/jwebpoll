package de.lebk.jwebpoll.client;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;

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

        VBox root = (VBox) FXMLLoader.load(this.getClass().getResource("/client/client.fxml"));
        SplitPane rootSplit = (SplitPane) root.lookup("#rootSplit");
        this.pollList = new ListView<String>();
        this.pollList.getItems().add("1. Umfrage");
        this.pollList.getItems().add("Bundestagswahlen");
        this.pollList.getItems().add("Mittag-Menü-Wahl");
        rootSplit.getItems().add(this.pollList);

        GridPane pollDetail = (GridPane) FXMLLoader.load(this.getClass().getResource("/client/pollDetail.fxml"));
        rootSplit.getItems().add(pollDetail);

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}