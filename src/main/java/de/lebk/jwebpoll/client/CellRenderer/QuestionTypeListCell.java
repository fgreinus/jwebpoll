package de.lebk.jwebpoll.client.CellRenderer;

import de.lebk.jwebpoll.data.QuestionType;
import javafx.scene.control.ListCell;
import javafx.scene.text.Text;

public class QuestionTypeListCell extends ListCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        Text txt = null;
        if (item != null) {
            txt = new Text("Unbekannt");
            if (item.equals(QuestionType.SINGLE.toString()))
                txt.setText("Einzelauswahl");
            else if (item.equals(QuestionType.MULTIPLE.toString()))
                txt.setText("Mehrfachauswahl");
            else if (item.equals(QuestionType.FREE.toString()))
                txt.setText("Freitext");
            else
                txt.setText(item);
        }
        this.setGraphic(txt);
    }
}