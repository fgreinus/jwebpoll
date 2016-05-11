package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "questions")
public class Question
{
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
    private ForeignCollection<Vote> votes;

    public Question() { }

    public Question(String title, boolean required, QuestionType type, Poll poll) {
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
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Poll getPoll() {
        return this.poll;
    }

    public ForeignCollection<Answer> getAnswers() {
        return answers;
    }
}
