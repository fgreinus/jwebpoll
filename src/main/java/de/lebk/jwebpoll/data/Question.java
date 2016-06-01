package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;

import java.sql.SQLException;

@DatabaseTable(tableName = "questions")
public class Question {
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

    public Question() {
        try {
            this.answers = Database.getInstance().getQuestionDao().getEmptyForeignCollection("answers");
        } catch (SQLException e) {
            this.answers = null;
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

    public void update() {
        try {
            Database.getInstance().getQuestionDao().update(this);
        } catch (SQLException e) {
            this.answers = null;
        }
    }
}
