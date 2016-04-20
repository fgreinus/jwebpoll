package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "polls")
public class Poll
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date created;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField
    private PollState state;

    ArrayList<Question> questions;

    public Poll()
    {

    }

    public Poll(int id, String title, String description, PollState state)
    {
        this.id = id;
        this.questions = new ArrayList<Question>();
        this.created = new Date();
        this.title = title;
        this.description = description;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public PollState getState() {
        return state;
    }

    public List<Question> getQuestions()
    {
        if(this.questions == null)
            this.questions = new ArrayList<>();
        return questions;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setState(PollState state) {
        this.state = state;
    }
}
