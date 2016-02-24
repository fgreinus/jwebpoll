package de.lebk.jwebpoll.classes;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Samson on 17.02.2016.
 */
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
