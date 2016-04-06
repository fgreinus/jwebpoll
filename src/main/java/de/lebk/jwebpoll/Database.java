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

public class Database {

    private static Database instance;

    private ConnectionSource dbConn;


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

        }
    }

}
