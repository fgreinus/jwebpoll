package de.lebk.jwebpoll;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        spawnWebServer();
    }

    private static void spawnWebServer() throws Exception
    {
        Frontend.getInstance();
    }
}


