package de.lebk.jwebpoll;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        spawnDatabase();
        spawnWebServer();
    }

    private static void spawnWebServer() throws Exception
    {
        Frontend.getInstance();
    }

    private static void spawnDatabase() throws Exception {
        Database.getInstance();
    }
}


