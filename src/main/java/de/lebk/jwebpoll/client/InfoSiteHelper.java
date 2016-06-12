package de.lebk.jwebpoll.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InfoSiteHelper {
    private static final Logger LOGGER = Logger.getLogger(InfoSiteHelper.class);
    private static final String RESOURCE_DIR = "/about/";
    public static void show(String resource) {
        Stage evaluationStage = new Stage();
        evaluationStage.setTitle(resource);
        evaluationStage.getIcons().add(new Image(InfoSiteHelper.class.getResource("/icon.png").toString()));
        WebView webview;
        GridPane helpGrid;
        try {
            helpGrid = FXMLLoader.load(ConfirmDialog.class.getResource("/client/helpSite.fxml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("", ex);
            return;
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            fileReader = new FileReader(InfoSiteHelper.class.getResource(RESOURCE_DIR + resource.toLowerCase() + ".html").getPath());
            bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            sb.append("<html><body><h2>Fehler (404 - Not Found)</h2> Bedauerlicher Weise konnte die Hilfeseite nicht geladen werden.<br>"
                    + "Noch bedauerlicher ist es, dass es keine Hilfeseite gibt wenn man wissen will, warum die Hilfeseite nicht lädt.</body> </html>");
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
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
