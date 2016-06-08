package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.PollState;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

public class PollStateListCell extends ListCell<PollState> {
    private static final Logger logger = Logger.getLogger(PollStateListCell.class);
    @Override
    protected void updateItem(PollState item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            Text txt = new Text("Unbekannt");
            switch (item) {
                case NEW:
                    txt.setText("Neu");
                    break;
                case OPEN:
                    txt.setText("Offen");
                    break;
                case CLOSED:
                    txt.setText("Geschlossen");
                    break;
            }
            this.setGraphic(txt);
        }
    }
}