package de.lebk.jwebpoll.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HelpSite {
    public static void show() {
        Stage evaluationStage = new Stage();
        evaluationStage.setTitle("Hilfe");
        WebView webview;
        GridPane helpGrid;
        try {
            helpGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/helpSite.fxml"));


        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            fileReader = new FileReader("src"+File.separator+"main"+File.separator+"resources"+File.separator+"help.html");
            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            sb.append("<html> <body> <h2> FEHLER </h2> Bedauerlicher Weise konnte die Hilfeseite nicht geladen werden. <br> "
                    +"Noch bedauerlicher ist es, dass es keine Hilfeseite gibt wenn man Wissen will warum die Hilfeseite nicht lädt.</body> </html>");
            e.printStackTrace();
        }

        webview = (WebView) helpGrid.lookup("#webview");
        webview.getEngine().loadContent(sb.toString());

        webview.setVisible(true);
        helpGrid.setVisible(true);
        evaluationStage.setScene(new Scene(helpGrid));
        evaluationStage.sizeToScene();
        evaluationStage.show();
    }
}
