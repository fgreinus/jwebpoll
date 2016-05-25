package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.Poll;
import javafx.scene.control.ListCell;

public class PollListCell extends ListCell<Poll> {
    @Override
    protected void updateItem(Poll item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null)
            this.setText(item.getTitle());
        else {
            this.setGraphic(null);
            this.setText(null);
        }
    }
}