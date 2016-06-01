package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Frontend {

    private static Frontend instance;

    private final String templateDir = "src/main/resources/templates";
    private final String assetDir = "/assets";

    private FreeMarkerEngine fmEngine;
    protected static Database db;
    protected Poll activePoll;


    public static Frontend getInstance(Poll activePoll) throws Exception {
        if (Frontend.instance == null) {
            Frontend.instance = new Frontend(activePoll);
        } else {
            Frontend.instance.activePoll = activePoll;
        }

        return Frontend.instance;
    }

    public static void kill() {
        stop();
    }

    private void initializeSparkConfiguration() {
        InetAddress host = null;
        try {
            host = InetAddress.getLocalHost();
            String hostAddress = host.getHostAddress();
            ipAddress(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // so that all static files will be served directly by spark and we don't have to care any longer about them :)
        staticFileLocation(assetDir);

        Configuration fmConfig = new Configuration();
        try {
            fmConfig.setDirectoryForTemplateLoading(new File(templateDir)); // otherwise freemarker would magically determine what directory to use...
        } catch (IOException ignored) {
        }

        fmEngine = new FreeMarkerEngine(fmConfig);
    }

    private void bindSparkRoutes() {
        get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("poll", activePoll);
            attributes.put("success", false);

            return new ModelAndView(attributes, "index.ftl");
        }, fmEngine);

        post("/", (request, response) -> {
            // Do request-Handling here! //@TODO: Handle Checkbox-inputs!
            System.out.println(request.body()); // This is just a test

            HashMap<String, ArrayList<String>> givenAnswersMap = new HashMap<>();

            // at first parse all results
            for (String inputKey : request.queryParams()) {

                String inputValue = request.queryParams(inputKey);
                String realInputKey = inputKey.contains("_") ? inputKey.substring(0, inputKey.indexOf("_")) : inputKey;

                if (!givenAnswersMap.containsKey(realInputKey)) {
                    givenAnswersMap.put(realInputKey, new ArrayList<>());
                }

                givenAnswersMap.get(realInputKey).add(inputValue);
            }

            Database db = Database.getInstance();
            Dao voteDao = db.getDaoForClass(Vote.class.getName());
            Dao questionDao = db.getDaoForClass(Question.class.getName());
            Dao answerDao = db.getDaoForClass(Answer.class.getName());

            for (String questionKeyString : givenAnswersMap.keySet()) {

                int questionId = 0;
                try {
                    questionId = Integer.parseInt(questionKeyString);
                } catch (Exception e) {
                    continue;
                }

                Object questionResult = questionDao.queryBuilder().where().eq("id", questionId).queryForFirst();
                if (questionResult == null) {
                    continue;
                }

                Question question = (Question) questionResult;

                for (String answer : givenAnswersMap.get(questionKeyString)) {
                    Vote newVote = new Vote();

                    int answerId = 0;
                    try {
                        answerId = Integer.parseInt(answer);
                    } catch (Exception e) { }

                    Object answerResult = answerDao.queryBuilder().where().eq("id", answerId).queryForFirst();
                    if (answerResult == null) {
                        newVote.setAnswer(null);
                        newVote.setUserText(answer);
                    } else {
                        newVote.setAnswer((Answer) answerResult);
                    }
                    newVote.setSession(request.session().id());
                    newVote.setQuestion(question);
                    voteDao.create(newVote);
                }
            }

            Map<String, Object> attributes = new HashMap<>();

            attributes.put("poll", activePoll);
            attributes.put("success", true);

            return new ModelAndView(attributes, "index.ftl");
        }, fmEngine);
    }

    private Frontend(Poll activePoll) throws Exception {
        initializeSparkConfiguration();
        bindSparkRoutes();
        db = Database.getInstance();
        this.activePoll = activePoll;
    }
}
