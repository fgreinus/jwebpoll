package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@DatabaseTable(tableName = "answers")
public class Answer {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String text;

    @DatabaseField
    private int value;

    public Answer() {
    }

    public Answer(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }
}