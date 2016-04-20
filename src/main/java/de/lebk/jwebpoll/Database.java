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


    private Database() throws Exception {
        Class.forName("org.sqlite.JDBC");
        SQLiteConfig sqliteConfig = new SQLiteConfig();
        String databaseUrl = "jdbc:sqlite:jwebpoll.sqlite";
        try {
            dbConn = new JdbcConnectionSource(databaseUrl);
        } catch (SQLException e) {
            throw new Exception("Error connecting to sqlite database");
        }

        initializeDatabaseTables();
        initializeModelDataAccessObjects();
    }

    public static Database getInstance() throws Exception {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public ConnectionSource getConnection()
    {
        if (dbConn.isOpen()) {
            return dbConn;
        } else {
            return null;
        }
    }

    private void initializeDatabaseTables()
    {
        try {
            TableUtils.createTableIfNotExists(dbConn, Answer.class);
            TableUtils.createTableIfNotExists(dbConn, Poll.class);
            TableUtils.createTableIfNotExists(dbConn, Question.class);
            TableUtils.createTableIfNotExists(dbConn, Vote.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeModelDataAccessObjects()
    {
        daoList = new Hashtable<>();

        try {

            daoList.put(Poll.class.getName(), DaoManager.createDao(dbConn, Poll.class));
            daoList.put(Answer.class.getName(), DaoManager.createDao(dbConn, Answer.class));
            daoList.put(Question.class.getName(), DaoManager.createDao(dbConn, Question.class));
            daoList.put(Vote.class.getName(), DaoManager.createDao(dbConn, Vote.class));

        } catch (SQLException e) {

        }
    }

    public Dao getDaoForClass(String className)
    {
        if (daoList.containsKey(className)) {
            return daoList.get(className);
        } else {
            return null;
        }
    }

    public Object getLastObjectOfTable(String className)
    {
        Dao dao = getDaoForClass(className);

        Object result = null;

        try {
            result = dao.queryBuilder().orderBy("id", false).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

}
