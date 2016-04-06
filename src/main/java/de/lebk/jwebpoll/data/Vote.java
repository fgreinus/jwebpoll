package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;

@DatabaseTable(tableName = "votes")
public class Vote
{
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String session;

    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date created;

    @DatabaseField(canBeNull = false, foreign = true)
    private Question question;

    @DatabaseField(canBeNull = false, foreign = true)
    private Answer answer;

    @DatabaseField
    private String userText;

    ArrayList<Question> questions;

    public Vote(String session, Question question, Answer answer)
    {
        this.created = new Date();
        this.question = question;
        this.answer = answer;
        this.session = session;
        this.userText = "";
    }

    public Vote() { }
}
