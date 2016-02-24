package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "answers")
public class Answer {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private Question question;

    public Answer(Question question) {
        this.question = question;
    }
}
