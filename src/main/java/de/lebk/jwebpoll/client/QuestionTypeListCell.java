package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.QuestionType;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.io.IOException;

public class QuestionTypeListCell extends ListCell<QuestionType> {
    final static Logger logger = Logger.getLogger(QuestionTypeListCell.class);

    @Override
    protected void updateItem(QuestionType item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            try {
                Text pollStateTxt = (Text) FXMLLoader.load(QuestionTypeListCell.this.getClass().getResource("/client/text.fxml"));
                String txt = "Unbekannt";
                switch (item) {
                    case SINGLE:
                        txt = "Einzelauswahl";
                        break;
                    case MULTIPLE:
                        txt = "Mehrfachauswahl";
                        break;
                    case FREE:
                        txt = "Freitext";
                        break;
                }
                pollStateTxt.setText(txt);

                this.setGraphic(pollStateTxt);
            } catch (IOException ex) {
                ex.printStackTrace();
                if (logger.isDebugEnabled()) {
                    logger.debug("", ex);
                }
            }
        }
    }
}
