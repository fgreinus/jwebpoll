package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Answer;
import de.lebk.jwebpoll.data.Poll;
import de.lebk.jwebpoll.data.Question;
import de.lebk.jwebpoll.data.Vote;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Frontend {
    private final String templateDir = "src/main/resources/templates";
    private final String assetDir = "/assets";

    final static Logger logger = Logger.getLogger(Frontend.class);

    public static final int PORT = 4567;

    private FreeMarkerEngine fmEngine;
    protected static Database db = Database.getInstance();
    protected Poll activePoll;

    public static void kill() {
        stop();
    }

    private void initializeSparkConfiguration(String networkAddress) {
        ipAddress(networkAddress);
        // so that all static files will be served directly by spark and we don't have to care any longer about them :)
        staticFileLocation(assetDir);

        Configuration fmConfig = new Configuration();
        try {
            fmConfig.setDirectoryForTemplateLoading(new File(templateDir)); // otherwise freemarker would magically determine what directory to use...
        } catch (IOException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("", ex);
            }
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
            // Do request-Handling here!

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

            Dao voteDao = Frontend.db.getVoteDao();
            Dao questionDao = Frontend.db.getQuestionDao();
            Dao answerDao = Frontend.db.getAnswerDao();

            for (String questionKeyString : givenAnswersMap.keySet()) {

                int questionId = 0;
                try {
                    questionId = Integer.parseInt(questionKeyString);
                } catch (Exception ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("", ex);
                    }
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
                    } catch (Exception ex) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("", ex);
                        }
                    }

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

    public Frontend(Poll activePoll, String networkAddress) {
        Frontend.kill();
        this.initializeSparkConfiguration(networkAddress);
        this.bindSparkRoutes();
        this.activePoll = activePoll;
    }
}
