package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.PollState;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.io.IOException;

public class PollStateListCell extends ListCell<PollState> {
    final static Logger logger = Logger.getLogger(PollStateListCell.class);
    @Override
    protected void updateItem(PollState item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            try {
                Text pollStateTxt = (Text) FXMLLoader.load(PollStateListCell.class.getResource("/client/text.fxml"));
                String txt = "Unbekannt";
                switch (item) {
                    case NEW:
                        txt = "Neu";
                        break;
                    case OPEN:
                        txt = "Offen";
                        break;
                    case CLOSED:
                        txt = "Geschlossen";
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