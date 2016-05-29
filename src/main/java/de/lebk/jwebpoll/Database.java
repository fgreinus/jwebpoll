package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import de.lebk.jwebpoll.client.Client;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConnection;
import org.sqlite.jdbc4.JDBC4Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Database {

    private static Database instance;

    private ConnectionSource dbConn;

    private Hashtable<String, Dao> daoList;

    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.exit(0);
        }
        SQLiteConfig sqliteConfig = new SQLiteConfig();
        String databaseUrl = "jdbc:sqlite:jwebpoll.sqlite";
        try {
            dbConn = new JdbcConnectionSource(databaseUrl);
        } catch (SQLException e) {
            System.exit(0);
        }

        initializeDatabaseTables();
        initializeModelDataAccessObjects();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public ConnectionSource getConnection() {
        if (dbConn.isOpen()) {
            return dbConn;
        } else {
            return null;
        }
    }

    private void initializeDatabaseTables() {
        try {
            TableUtils.createTableIfNotExists(dbConn, Answer.class);
            TableUtils.createTableIfNotExists(dbConn, Poll.class);
            TableUtils.createTableIfNotExists(dbConn, Question.class);
            TableUtils.createTableIfNotExists(dbConn, Vote.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeModelDataAccessObjects() {
        daoList = new Hashtable<>();

        try {
            daoList.put(Poll.class.getName(), DaoManager.createDao(dbConn, Poll.class));
            daoList.put(Answer.class.getName(), DaoManager.createDao(dbConn, Answer.class));
            daoList.put(Question.class.getName(), DaoManager.createDao(dbConn, Question.class));
            daoList.put(Vote.class.getName(), DaoManager.createDao(dbConn, Vote.class));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private Dao getDaoForClass(String className) {
        if (daoList.containsKey(className)) {
            return daoList.get(className);
        } else {
            return null;
        }
    }

    public Dao getPollDao() {
        return this.getDaoForClass(Poll.class.getName());
    }

    public Dao getQuestionDao() {
        return this.getDaoForClass(Question.class.getName());
    }

    public Dao getAnswerDao() {
        return this.getDaoForClass(Answer.class.getName());
    }

    public Dao getVoteDao() {
        return this.getDaoForClass(Vote.class.getName());
    }

    public Object getLastObjectOfTable(String className) {
        Dao dao = getDaoForClass(className);

        Object result = null;

        try {
            result = dao.queryBuilder().orderBy("id", false).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void savePoll(Poll localPoll) {
        try {
            Poll dbPoll = null;
            if (localPoll.getId() != 0)
                dbPoll = (Poll) this.getPollDao().queryForId(localPoll.getId());

            Hashtable<Integer, Question> dbQuestionsToDelete = new Hashtable<>();
            Hashtable<Integer, Answer> dbAnswersToDelete = new Hashtable<>();
            Hashtable<Integer, Question> dbQuestionsToUpdate = new Hashtable<>();
            Hashtable<Integer, Answer> dbAnswersToUpdate = new Hashtable<>();
            Hashtable<Integer, Question> dbQuestionsToCreate = new Hashtable<>();
            Hashtable<Integer, Answer> dbAnswersToCreate = new Hashtable<>();

            for (Question localQ : localPoll.getQuestions()) {
                dbQuestionsToCreate.put(localQ.getId(), localQ);
                for (Answer localA : localQ.getAnswers()) {
                    dbAnswersToCreate.put(localA.getId(), localA);
                }
            }

            if (dbPoll != null) {

                for (Question dbQ : dbPoll.getQuestions()) {
                    dbQuestionsToDelete.put(dbQ.getId(), dbQ);
                    dbQuestionsToUpdate.put(dbQ.getId(), dbQ);
                    dbQuestionsToCreate.remove(dbQ.getId());
                    for (Answer dbA : dbQ.getAnswers()) {
                        dbAnswersToDelete.put(dbA.getId(), dbA);
                        dbAnswersToUpdate.put(dbA.getId(), dbA);
                        dbAnswersToCreate.remove(dbA.getId());
                    }
                }

                for (Question localQ : localPoll.getQuestions()) {
                    dbQuestionsToDelete.remove(localQ.getId());
                    for (Answer localA : localQ.getAnswers()) {
                        dbAnswersToDelete.remove(localA.getId());
                    }
                }

                for (Answer toDelete : dbAnswersToDelete.values()) {
                    dbQuestionsToUpdate.remove(toDelete.getId());
                    this.getAnswerDao().delete(toDelete);
                }
                for (Question toDelete : dbQuestionsToDelete.values()) {
                    this.getQuestionDao().delete(toDelete);
                }

                for (Answer toUpdate : dbAnswersToUpdate.values()) {
                    this.getAnswerDao().update(toUpdate);
                }
                for (Question toUpdate : dbQuestionsToUpdate.values()) {
                    this.getQuestionDao().update(toUpdate);
                }
            }

            for (Answer toCreate : dbAnswersToCreate.values()) {
                this.getAnswerDao().create(toCreate);
            }
            for (Question toCreate : dbQuestionsToCreate.values()) {
                this.getQuestionDao().create(toCreate);
            }

            // Finally save / update the poll
            if (dbPoll != null)
                this.getPollDao().update(localPoll);
            else
                this.getPollDao().create(localPoll);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
