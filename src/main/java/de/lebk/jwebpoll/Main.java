package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.PollState;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.QuestionType;

import java.sql.SQLException;

public class Main {


    protected static Poll activePoll;

    public static void main(String[] args) throws Exception {
        spawnDatabase();

        if (selectActivePoll()) {
            spawnWebServer(activePoll);
        }
    }

    private static void spawnWebServer(Poll poll) throws Exception {
        Frontend.getInstance(poll);
    }

    private static void spawnDatabase() throws Exception {
        Database.getInstance();
    }

    private static boolean selectActivePoll() {
        Database db = null;
        try {
            db = Database.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Dao dao = db.getDaoForClass(Poll.class.getName());
        try {
            Poll poll = new Poll("1. Umfrage", "Eine Beschreibung", PollState.OPEN);
            dao.create(poll);

            Dao dao2 = db.getDaoForClass(Question.class.getName());
            dao2.create(new Question("Tolle Frage", true, QuestionType.SINGLE, poll));
            dao2.create(new Question("Tolle Frage 2", true, QuestionType.FREE, poll));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Object result = dao.queryBuilder().where().eq("state", PollState.OPEN).queryForFirst();
            if (result != null) {
                activePoll = (Poll) result;
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}


