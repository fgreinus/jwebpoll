package de.lebk.jwebpoll.client.CellRenderer;

import de.lebk.jwebpoll.client.Client;
import de.lebk.jwebpoll.data.Poll;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class PollListCell extends ListCell<Poll> {
    @Override
    protected void updateItem(Poll item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            String tmpTitle = item.getTitle() == null || item.getTitle().isEmpty() ? "<Neue Umfrage>" : item.getTitle();
            if (item == Client.getActivePoll()) {
                GridPane grid = new GridPane();
                ImageView imageView = new ImageView(PollListCell.class.getResource("/icon.png").toString());
                imageView.setFitWidth(20);
                imageView.setFitHeight(20);
                grid.add(imageView, 0, 0);
                Text text = new Text(tmpTitle);
                text.styleProperty().set("-fx-font-weight:bold;");
                grid.add(text, 1, 0);
                this.setGraphic(grid);
            } else
                this.setText(tmpTitle);
        } else {
            this.setGraphic(null);
            this.setText(null);
        }
    }
}