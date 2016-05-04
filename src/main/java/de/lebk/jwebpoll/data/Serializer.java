package de.lebk.jwebpoll.data;

import com.sun.xml.internal.bind.api.impl.NameConverter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Samson on 06.04.2016.
 */
public class Serializer {
    public static void write(String fullyNamedPath, String... lines) {
        Path file = Paths.get(fullyNamedPath);
        try {
            Files.write(file, Arrays.asList(lines), Charset.forName("UTF-8"), StandardOpenOption.CREATE_NEW);
        }
        catch (IOException ioEx) {
            // TODO: Report to front-end
        }
    }

    public static void write(String fullyNamedPath, Poll... polls) {
        StringBuilder sb = new StringBuilder();
        sb.append("PollTitle;PollDescription");
        sb.append("\r\n");
        for (Poll poll : polls) {
            sb.append('"').append(poll.getTitle()).append('"').append(';');
            sb.append('"').append(poll.getDescription()).append('"');//.append(';');
            //sb.append('"').append(poll.getState()).append('"');
            sb.append("\r\n");
        }
        write(fullyNamedPath, sb.toString());
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

                    result.add(new Poll(-1, title, description, PollState.NEW));
                }
            }
        }
        catch (IOException ioEx) {
            // TODO: Report to front-end
            System.out.println(ioEx.getMessage());
        }

        return result;
    }
}
