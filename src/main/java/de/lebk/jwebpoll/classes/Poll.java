package de.lebk.jwebpoll.classes;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Samson on 17.02.2016.
 */
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
