package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.sql.SQLException;

@DatabaseTable(tableName = "answers")
public class Answer
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    private String text;

    @DatabaseField
    private int value;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "question_id", canBeNull = true)
    private Question question;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Vote> votes;

    public Answer()
    {
    }

    public Answer(String text, int value, Question question)
    {
        this.text = text;
        this.value = value;
        this.question = question;

        Dao dao = Database.getInstance().getDaoForClass(this.getClass().getName());

        try {
            this.votes = dao.getEmptyForeignCollection("votes");
        } catch (SQLException e) {
            this.votes = null;
        }
    }



    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }

    public Question getQuestion()
    {
        return question;
    }

    public void setQuestion(Question question)
    {
        this.question = question;
    }

    public ForeignCollection<Vote> getVotes()
    {
        return votes;
    }
}