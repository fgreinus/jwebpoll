package de.lebk.jwebpoll.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import de.lebk.jwebpoll.Database;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable(tableName = "polls")
public class Poll {
    final static Logger logger = Logger.getLogger(Poll.class);
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
        try {
            this.questions = Database.getInstance().getPollDao().getEmptyForeignCollection("questions");
        } catch (SQLException e) {
            this.questions = null;
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
        }
    }

    public Poll(String title, String description, PollState state) {
        this();
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

    public ForeignCollection<Question> getQuestions() {
        return questions;
    }

    public void setTitle(String title) {
        this.title = title;
        this.update();
    }

    public void setDescription(String description) {
        this.description = description;
        this.update();
    }

    public void setState(PollState state) {
        this.state = state;
        this.update();
    }

    private void update() {
        try {
            Database.getInstance().getPollDao().update(this);
        } catch (SQLException e) {
            this.questions = null;
            if (logger.isDebugEnabled()) {
                logger.debug("", e);
            }
        }
    }
}
