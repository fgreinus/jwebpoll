package de.lebk.jwebpoll.client.Evaluation;

public class ExtendedStatsTableHelperObject {
    private String text;
    private double value;

    public ExtendedStatsTableHelperObject(String text, double value) {
        this.text = text;
        this.value = value;
    }

    private String getText() {
        return text;
    }

    private double getValue() {
        return value;
    }
}
