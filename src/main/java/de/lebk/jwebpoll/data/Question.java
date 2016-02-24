package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "questions")
public class Question {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private Poll poll;

    public Question(Poll poll) {
        this.poll = poll;
    }
}
