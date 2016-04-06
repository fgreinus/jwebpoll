package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;

@DatabaseTable(tableName = "polls")
public class Poll {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date created;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField
    private short state;

    ArrayList<Question> questions;

    public Poll(String title, String description, short state)
    {
        this.questions = new ArrayList<Question>();
        this.created = new Date();
        this.title = title;
        this.description = description;
        this.state = state;
    }

    public Poll() { }
}
