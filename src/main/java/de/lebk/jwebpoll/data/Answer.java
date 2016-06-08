package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;
import org.apache.log4j.Logger;

import java.sql.SQLException;

@DatabaseTable(tableName = "answers")
public class Answer {
    private static final Logger LOGGER = Logger.getLogger(Answer.class);

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

    public Answer() {
        try {
            this.votes = Database.getDB().getAnswerDao().getEmptyForeignCollection("votes");
        } catch (SQLException e) {
            this.votes = null;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", e);
            }
        }
    }

    public Answer(String text, int value, Question question) {
        this();
        this.text = text;
        this.value = value;
        this.question = question;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public ForeignCollection<Vote> getVotes() {
        return votes;
    }
}