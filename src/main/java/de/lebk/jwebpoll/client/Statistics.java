package de.lebk.jwebpoll.client;

import com.j256.ormlite.dao.ForeignCollection;
import de.lebk.jwebpoll.data.Answer;

/**
 * Created by Samson on 01.06.2016.
 */
public class Statistics {

    public static double getAverage(int voteCount, int voteCountTotal) {
        return (double)voteCount/(double)voteCountTotal;
    }

    public static double getStandardDeviation(ForeignCollection<Answer> answers, double average) {
        return Math.sqrt(getVariance(answers, average));
    }

    public static double getVariance(ForeignCollection<Answer> answers, double average) {
        double voteCountTotal = 0;
        for (Answer answer : answers) {
            voteCountTotal += (double)answer.getVotes().size();
        }
        double sum = 0.0;
        for (Answer answer : answers) {
            if (answer.getVotes().size() > 0) {
                double answerValue = (double)answer.getValue();
                double termA = answerValue - average;
                double varianceElement = Math.pow(termA, 2);
                sum += varianceElement * (double)answer.getVotes().size();
            }
        }
        return (double)sum / (double)voteCountTotal;
    }
}
