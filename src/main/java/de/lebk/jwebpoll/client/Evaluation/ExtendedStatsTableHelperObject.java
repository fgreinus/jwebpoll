package de.lebk.jwebpoll.client.Evaluation;

/**
 * Created by lostincoding on 08.06.16.
 */
public class ExtendedStatsTableHelperObject {
    private String text;
    private double value;

    public ExtendedStatsTableHelperObject(String text,double value){
        this.text=text;
        this.value=value;
    }

    private String getText() {
        return text;
    }
    private double getValue(){
        return value;
    }
}
