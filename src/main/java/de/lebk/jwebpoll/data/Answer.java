package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@DatabaseTable(tableName = "answers")
public class Answer {
    @DatabaseField(generatedId = true)
    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    @DatabaseField
    private SimpleStringProperty text = new SimpleStringProperty();

    @DatabaseField
    private SimpleIntegerProperty value = new SimpleIntegerProperty();

    public Answer() {
    }

    public Answer(String text, int value) {
        this.text.set(text);
        this.value.set(value);
    }

    public int getId() {
        return id.get();
    }

    public String getText() {
        return text.get();
    }

    public int getValue() {
        return value.get();
    }
}