package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "polls")
public class Poll {
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public int id;

    @DatabaseField(dataType = DataType.DATE_STRING)
    public Date created;

    @DatabaseField
    public String title;

    @DatabaseField
    public String description;

    @DatabaseField
    public PollState state;

    @ForeignCollectionField(eager = true)
    public ForeignCollection<Question> questions;

    public Poll() {

    }

    public Poll(String title, String description, PollState state) {
        this.created = new Date();
        this.title = title;
        this.description = description;
        this.state = state;

        Dao dao = Database.getInstance().getDaoForClass(this.getClass().getName());

        try {
            this.questions = dao.getEmptyForeignCollection("questions");
        } catch (SQLException e) {
            this.questions = null;
        }
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

    public ForeignCollection<Question> getQuestions() {
        return questions;
    }

    public void setId(int id) {
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
