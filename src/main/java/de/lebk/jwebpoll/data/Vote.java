package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;

@DatabaseTable(tableName = "votes")
public class Vote
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    private String session;

    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date created;

    @DatabaseField(canBeNull = false, foreign = true)
    private Question question;

    @DatabaseField(canBeNull = true, foreign = true)
    private Answer answer;

    @DatabaseField
    private String userText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public Vote(String session, Question question, Answer answer, String userText)
    {
        this.created = new Date();
        this.question = question;
        this.answer = answer;
        this.session = session;
        this.userText = userText;
    }

    public Vote()
    {
        this.created = new Date();
    }
}
