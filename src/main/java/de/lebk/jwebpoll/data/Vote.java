package de.lebk.jwebpoll.data;

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

    @DatabaseField
    private Date created;

    @DatabaseField
    private Question question;

    @DatabaseField
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
}
