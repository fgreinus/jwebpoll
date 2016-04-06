package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Poll;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        spawnDatabase();
        spawnWebServer();
    }

    private static void spawnWebServer() throws Exception {
        Frontend.getInstance();
    }

    private static void spawnDatabase() throws Exception {
        Database.getInstance();
    }

    private void sqlExample()
    {
        Poll poll = new Poll("Toller Titel", "Beschreibung", (short)1);

        try {
            Database db = Database.getInstance();
            Dao pollDao = db.getDaoForClass(Poll.class.getName());
            pollDao.create(poll);

            List<Poll> list = pollDao.queryForAll();
            for (Poll p : list) {
                System.out.println(p);
            }
        } catch (Exception e) { }
    }
}


