package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.*;

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
            Question q1 = new Question("Tolle Frage", false, QuestionType.FREE, poll);
            dao2.create(q1);
            Question q2 = new Question("Tolle Frage 2", true, QuestionType.MULTIPLE, poll);
            dao2.create(q2);

            Dao dao3 = db.getDaoForClass(Answer.class.getName());
            Answer a1 = new Answer("Antwort Nummer 1", 1, q1);
            dao3.create(a1);
            Answer a2 = new Answer("Antwort Nummer 1", 2, q2);
            dao3.create(a2);
            Answer a3 = new Answer("Antwort Nummer 2", 1, q2);
            dao3.create(a3);

            Dao dao4 = db.getDaoForClass(Vote.class.getName());
            Vote v1 = new Vote("sessid123123", q1, a1, "");
            Vote v2 = new Vote("sessid1231212", q1, a2, "");
            dao4.create(v1);
            dao4.create(v2);
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


