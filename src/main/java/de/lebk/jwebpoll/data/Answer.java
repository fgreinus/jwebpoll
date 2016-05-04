package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "answers")
public class Answer
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String text;

    @DatabaseField
    private String value;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "question_id", canBeNull = true)
    private Question question;

    public Answer()
    {
    }

    public Answer(String text, String value)
    {
        this.text = text;
        this.value = value;
    }

    public int getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    public String getValue()
    {
        return value;
    }
}