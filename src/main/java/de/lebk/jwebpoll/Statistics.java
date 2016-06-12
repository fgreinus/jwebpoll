package de.lebk.jwebpoll;

public class Statistics {

    public static double getStandardDeviation(int[] values, double avg) {
        return Math.sqrt(getVariance(values, avg));
    }

    public static double getVariance(int[] values, double avg) {
        double sum = 0;
        for (int i = 0; i < values.length; i++) {
            sum += ((double) values[i] - avg) * ((double) values[i] - avg);
        }
        return sum / values.length;
    }

    public static double round(double x) {
        return (double) ((int) (x * 100)) / 100;
    }
}
