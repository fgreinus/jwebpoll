package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "questions")
public class Question
{
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private Poll poll;

    @DatabaseField
    private String title;

    @DatabaseField
    private String hint;

    @DatabaseField
    private boolean required;

    @DatabaseField
    private short type;

    public Question(Poll poll, String title, boolean required, short type)
    {
        this.poll = poll;
        this.title = title;
        this.hint = "";
        this.required = required;
        this.type = type;
    }
}
