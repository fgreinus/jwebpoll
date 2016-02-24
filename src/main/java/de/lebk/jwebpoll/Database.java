package de.lebk.jwebpoll;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;

    private Connection dbConn;

    private Database() throws Exception {
        SQLiteConfig sqliteConfig = new SQLiteConfig();
        String databaseUrl = "jdbc:sqlite:jwebpoll.sqlite";
        try {
            dbConn = DriverManager.getConnection(databaseUrl, sqliteConfig.toProperties());
        } catch (SQLException e) {
            throw new Exception("Error connecting to sqlite database");
        }
    }

    public static Database getInstance() throws Exception {
        if (instance == null) {
            instance = new Database();
        }

        return instance;
    }

    public Connection getConnection()
    {
        try {
            if (dbConn.isValid(5)) {
                return dbConn;
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

}
