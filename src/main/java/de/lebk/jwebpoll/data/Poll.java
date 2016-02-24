package de.lebk.jwebpoll.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;

@DatabaseTable(tableName = "polls")
public class Poll {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private Date timestamp = new Date();

    @DatabaseField
    private String description = "";

    @DatabaseField
    private boolean active = false;

    ArrayList<Question> questions;

    public Poll(String description) {
        this.questions = new ArrayList<Question>();
        this.timestamp = new Date();
        this.description = description;
    }
}
