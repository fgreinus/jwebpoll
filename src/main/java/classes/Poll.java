package classes;

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

    ArrayList<Question> questions;

    @DatabaseField()
    private Date timestamp;

    public Poll() {
        questions = new ArrayList<Question>();
        timestamp = new Date();
    }
}
