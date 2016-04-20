package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.PollState;

import java.sql.SQLException;
import java.util.List;

public class Main {

    protected static Poll activePoll;

    public static void main(String[] args) throws Exception {
        spawnDatabase();

        if (selectActivePoll())
        {
            spawnWebServer(activePoll);
        }
    }

    private static void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }

    private static void spawnDatabase() throws Exception {
        Database.getInstance();
    }

    private static boolean selectActivePoll()
    {
        Database db = null;
        try {
            db = Database.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Dao dao = db.getDaoForClass(Poll.class.getName());
        try {
            Object result = dao.queryBuilder().where().eq("state", PollState.OPEN).queryForFirst();
            if (result != null)
            {
                activePoll = (Poll) result;
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}


