package de.lebk.jwebpoll.client.CellRenderer;

import de.lebk.jwebpoll.data.PollState;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;

public class PollStateListCell extends ListCell<PollState> {
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