package de.lebk.jwebpoll.client;

import de.lebk.jwebpoll.data.QuestionType;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;

public class QuestionTypeListCell extends ListCell<QuestionType> {
    @Override
    protected void updateItem(QuestionType item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            Text txt = new Text("Unbekannt");
            switch (item) {
                case SINGLE:
                    txt.setText("Einzelauswahl");
                    break;
                case MULTIPLE:
                    txt.setText("Mehrfachauswahl");
                    break;
                case FREE:
                    txt.setText("Freitext");
                    break;
            }
            this.setGraphic(txt);
        }
    }
}
