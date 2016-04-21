package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.PollState;

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
        Poll poll = new Poll("Toller Titel", "Beschreibung", PollState.NEW);

        try
        {
            Database db = Database.getInstance();
            Dao pollDao = db.getDaoForClass(Poll.class.getName());
            pollDao.create(poll);

            List<Poll> list = pollDao.queryForAll();
            for (Poll p : list) {
                System.out.println(p);
            }
        }
        catch (Exception e) { }
    }
}


