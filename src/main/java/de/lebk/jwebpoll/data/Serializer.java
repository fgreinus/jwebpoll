package de.lebk.jwebpoll.data;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Serializer {
    private static final Logger LOGGER = Logger.getLogger(Serializer.class);

    public static void write(String fullyNamedPath, String... lines) {
        Path file = Paths.get(fullyNamedPath);
        try {
            Files.write(file, Arrays.asList(lines), Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException ioEx) {
            // TODO: Report to front-end
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ioEx);
            }
        }
    }

    public static boolean write(String fullyNamedPath, String content) {
        Path file = Paths.get(fullyNamedPath);
        try {
            Files.deleteIfExists(file);
            Files.write(file, Arrays.asList(content), Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW);
        } catch (IOException ioEx) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ioEx);
            }
        }
        return Files.exists(file, LinkOption.NOFOLLOW_LINKS);
    }

    public static String serialize(Poll... polls) {
        StringBuilder sb = new StringBuilder();
        sb.append("PollTitle;PollDescription");
        sb.append("\r\n");
        for (Poll poll : polls) {
            sb.append(convert(poll.getTitle()));
            sb.append('"').append(poll.getDescription()).append('"');
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public static void write(String fullyNamedPath, Poll... polls) {
        write(fullyNamedPath, serialize(polls));
    }

    public static boolean toCsv(String fullyNamedPath, Poll poll) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Questions;Answer;Votes;WeightedVotes;%n"));
        for (Question question : poll.getQuestions()) {
            for (Answer answer : question.getAnswers()) {
                int votesCount = answer.getVotes().size();
                int weightedVotesCount = answer.getVotes().size() * answer.getValue();
                String questionTitle = question.getTitle().replace("\"", "\"\"");
                String answerText = answer.getText().replace("\"", "\"\"");
                sb.append(String.format("\"%s\";\"%s\";%s;%s;%n", questionTitle, answerText, votesCount, weightedVotesCount));
            }
        }
        return write(fullyNamedPath, sb.toString());
    }

    public static ArrayList<Poll> read(String fullyNamedPath) {
        Path path = Paths.get(fullyNamedPath);
        ArrayList<Poll> result = new ArrayList<Poll>();

        try {
            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));

            boolean first = true;
            for (String str : lines) {
                if (first) {
                    first = false;
                    continue;
                }

                if (str.contains(";")) {
                    String[] split = str.split(";");

                    // Remove leading and trailing quote marks if any, otherwise return the original
                    String title = split[0].matches("\".*\"") ? split[0].substring(1, split[0].length() - 1) : split[0];
                    String description = split[1].matches("\".*\"") ? split[1].substring(1, split[1].length() - 1) : split[1];

                    result.add(new Poll(title, description, PollState.NEW));
                }
            }
        }
        catch (IOException ioEx) {
            // TODO: Report to front-end
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("", ioEx);
            }
            System.out.println(ioEx.getMessage());
        }

        return result;
    }

    public static String convert(Object obj) {
        return "\"" + obj.toString() + "\";";
    }
}
