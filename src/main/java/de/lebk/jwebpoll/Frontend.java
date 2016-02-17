package de.lebk.jwebpoll;

import freemarker.template.Configuration;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Frontend {

    protected static Frontend instance;

    protected final String templateDir = "src/main/resources/templates";
    protected final String assetDir = "/assets";

    protected FreeMarkerEngine fmEngine;

    public static Frontend getInstance()
    {
        if (Frontend.instance == null) {
            Frontend.instance = new Frontend();
        }

        return Frontend.instance;
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
        get("/poll/:id", RequestHandler::pollAction, fmEngine);
    }

    private Frontend()
    {
        initializeSparkConfiguration();
        bindSparkRoutes();
    }

    private static class RequestHandler
    {
        public static ModelAndView indexAction(Request request, Response response)
        {
            Map<String, Object> attributes = new HashMap<>();

            attributes.put("test", "Test123");

            return new ModelAndView(attributes, "index.ftl");
        }

        public static ModelAndView pollAction(Request request, Response response)
        {
            Map<String, Object> attributes = new HashMap<>();

            return new ModelAndView(attributes, "poll.ftl");
        }
    }
}
