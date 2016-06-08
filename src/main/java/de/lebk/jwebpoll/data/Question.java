package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;
import org.apache.log4j.Logger;

import java.sql.SQLException;

@DatabaseTable(tableName = "questions")
public class Question {
    final static Logger logger = Logger.getLogger(Question.class);

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String hint;

    @DatabaseField
    private boolean required;

    @DatabaseField
    private QuestionType type;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "poll_id")
    private Poll poll;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Answer> answers;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Vote> freetextVotes;

    public Question() {
        try {
            this.answers = Database.getDB().getQuestionDao().getEmptyForeignCollection("answers");
            this.freetextVotes = Database.getDB().getQuestionDao().getEmptyForeignCollection("freetextVotes");
        } catch (SQLException e) {
            this.answers = null;
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
        }
    }

    public Question(String title, boolean required, QuestionType type, Poll poll) {
        this();
        this.title = title;
        this.hint = "";
        this.required = required;
        this.type = type;
        this.poll = poll;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.update();
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        this.update();
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        this.update();
    }

    public QuestionType getType() {
        return type;
    }

    public String getTypeString() {
        return type.toString();
    }

    public void setType(QuestionType type) {
        this.type = type;
        this.update();
    }

    public Poll getPoll() {
        return this.poll;
    }

    public ForeignCollection<Answer> getAnswers() {
        return answers;
    }

    public ForeignCollection<Vote> getFreetextVotes() {
        return freetextVotes;
    }

    public void update() {
        try {
            Database.getDB().getQuestionDao().update(this);
        } catch (SQLException e) {
            this.answers = null;
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
        }
    }
}
