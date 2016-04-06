package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "answers")
public class Answer
{
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private Question question;

    @DatabaseField
    private String text;

    @DatabaseField
    private String value;

    public Answer(Question question, String text, String value)
    {
        this.question = question;
        this.text = text;
        this.value = value;
    }

}
