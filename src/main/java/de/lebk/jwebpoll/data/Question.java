package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

@DatabaseTable(tableName = "questions")
public class Question
{
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String title;

    @DatabaseField
    private String hint;

    @DatabaseField
    private boolean required;

    @DatabaseField
    private QuestionType type;

    private final ArrayList<Answer> answers = new ArrayList<>();

    public Question() { }

    public Question(String title, boolean required, QuestionType type)
    {
        this.title = title;
        this.hint = "";
        this.required = required;
        this.type = type;
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

    public ArrayList<Answer> getAnswers()
    {
        return answers;
    }
}
