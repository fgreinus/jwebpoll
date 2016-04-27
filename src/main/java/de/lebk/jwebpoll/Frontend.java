package de.lebk.jwebpoll;

import com.j256.ormlite.dao.Dao;
import de.lebk.jwebpoll.data.Poll;
import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
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


    public static Frontend getInstance(Poll activePoll) throws Exception
    {
        if (Frontend.instance == null) {
            Frontend.instance = new Frontend(activePoll);
        } else {
            Frontend.instance.activePoll = activePoll;
        }

        return Frontend.instance;
    }

    public void stop()
    {
        if (isRunning())
            instance.stop();
    }

    public boolean isRunning()
    {
        return instance.isRunning();
    }

    private void initializeSparkConfiguration()
    {
        // so that all static files will be served directly by spark and we don't have to care any longer about them :)
        staticFileLocation(assetDir);

        Configuration fmConfig = new Configuration();
        try {
            fmConfig.setDirectoryForTemplateLoading(new File(templateDir)); // otherwise freemarker would magically determine what directory to use...
        } catch (IOException ignored) { }

        fmEngine = new FreeMarkerEngine(fmConfig);
    }

    private void bindSparkRoutes()
    {
        get("/", RequestHandler::indexAction, fmEngine);
    }

    private Frontend(Poll activePoll) throws Exception
    {
        initializeSparkConfiguration();
        bindSparkRoutes();
        db = Database.getInstance();
        this.activePoll = activePoll;
    }

    private static class RequestHandler
    {
        public static ModelAndView indexAction(Request request, Response response)
        {
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("test", "Test123");

            // TEST FOR FORMS
            Map<String, String> testArray1 = new HashMap<String, String>();
            ArrayList<HashMap> testArray = new ArrayList<HashMap>();


           // testArray.add("EINS");

            attributes.put("formArray", testArray1);

            return new ModelAndView(attributes, "index.ftl");
        }

        public static ModelAndView pollAction(Request request, Response response)
        {
            Map<String, Object> attributes = new HashMap<>();

            return new ModelAndView(attributes, "poll.ftl");
        }
    }
}
