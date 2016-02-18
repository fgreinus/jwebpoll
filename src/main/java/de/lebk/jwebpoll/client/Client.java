package de.lebk.jwebpoll.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Client extends Application
{
    public static void main(String[] args)
    {
        Client.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("JWebPoll");

        GridPane root = new GridPane();

        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
