package main.java;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        staticFileLocation("/public"); // Static files (css,js etc..)

        get("/hello", (req, res) -> "JWebPoll is awesome");
    }
}


