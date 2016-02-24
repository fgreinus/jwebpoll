package de.lebk.jwebpoll.classes;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Samson on 17.02.2016.
 */
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
