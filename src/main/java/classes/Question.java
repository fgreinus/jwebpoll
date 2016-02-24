package classes;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Samson on 17.02.2016.
 */
@DatabaseTable(tableName = "questions")
public class Question {
    @DatabaseField(id = true)
    private int id;

    public Question() {

    }
}
