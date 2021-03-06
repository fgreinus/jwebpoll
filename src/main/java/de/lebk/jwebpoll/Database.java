package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import org.apache.log4j.Logger;
import org.sqlite.SQLiteConfig;

import java.sql.SQLException;
import java.util.Hashtable;

public class Database {
    private static final Logger LOGGER = Logger.getLogger(Database.class);

    //- DB -
    public static final Database DB = new Database();
    private ConnectionSource dbConn;

    //- Dao -
    private Hashtable<String, Dao> daoList;

    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
            System.exit(0);
        }
        SQLiteConfig sqliteConfig = new SQLiteConfig();
        String databaseUrl = "jdbc:sqlite:jwebpoll.sqlite";
        try {
            dbConn = new JdbcConnectionSource(databaseUrl);
        } catch (SQLException ex) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
            System.exit(0);
        }

        initTables();
        initDAOs();
    }

    private void initTables() {
        try {
            TableUtils.createTableIfNotExists(dbConn, Answer.class);
            TableUtils.createTableIfNotExists(dbConn, Poll.class);
            TableUtils.createTableIfNotExists(dbConn, Question.class);
            TableUtils.createTableIfNotExists(dbConn, Vote.class);
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
    }

    private void initDAOs() {
        this.daoList = new Hashtable<>();
        try {
            this.daoList.put(Poll.class.getName(), DaoManager.createDao(dbConn, Poll.class));
            this.daoList.put(Answer.class.getName(), DaoManager.createDao(dbConn, Answer.class));
            this.daoList.put(Question.class.getName(), DaoManager.createDao(dbConn, Question.class));
            this.daoList.put(Vote.class.getName(), DaoManager.createDao(dbConn, Vote.class));
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
    }

    private Dao getDaoForClass(String className) {
        if (daoList.containsKey(className)) {
            return daoList.get(className);
        } else {
            return null;
        }
    }

    public Dao<Poll, Integer> getPollDao() {
        return (Dao<Poll, Integer>) this.getDaoForClass(Poll.class.getName());
    }

    public Dao<Question, Integer> getQuestionDao() {
        return (Dao<Question, Integer>) this.getDaoForClass(Question.class.getName());
    }

    public Dao<Answer, Integer> getAnswerDao() {
        return (Dao<Answer, Integer>) this.getDaoForClass(Answer.class.getName());
    }

    public Dao<Vote, Integer> getVoteDao() {
        return (Dao<Vote, Integer>) this.getDaoForClass(Vote.class.getName());
    }

    public boolean deletePoll(Poll localPoll) {
        if (localPoll == null || localPoll.getId() == 0)
            return false;
        try {
            Poll dbPoll = (Poll) this.getPollDao().queryForId(localPoll.getId());

            if (dbPoll != null) {
                for (Question dbQ : dbPoll.getQuestions()) {
                    for (Answer dbA : dbQ.getAnswers()) {
                        for (Vote dbV : dbA.getVotes())
                            this.getVoteDao().delete(dbV);
                        this.getAnswerDao().delete(dbA);
                    }
                    this.getQuestionDao().delete(dbQ);
                }
                this.getPollDao().delete(dbPoll);
            }

            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ex);
            }
        }
        return false;
    }
}
