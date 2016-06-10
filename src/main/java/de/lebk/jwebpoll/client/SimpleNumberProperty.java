package de.lebk.jwebpoll.client;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class SimpleNumberProperty extends ObjectProperty<Number> {
    private SimpleIntegerProperty intProp;
    private SimpleDoubleProperty doubleProp;

    public SimpleNumberProperty(SimpleIntegerProperty intProp) {
        if(intProp == null)
            throw new IllegalArgumentException("Initial Property cannot be null");
        this.intProp = intProp;
    }

    public SimpleNumberProperty(SimpleDoubleProperty doubleProp) {
        if(doubleProp == null)
            throw new IllegalArgumentException("Initial Property cannot be null");
        this.doubleProp = doubleProp;
    }

    @Override
    public void bind(ObservableValue<? extends Number> observable) {
        if (intProp != null)
            intProp.bind(observable);
        if (doubleProp != null)
            doubleProp.bind(observable);
    }

    @Override
    public void unbind() {
        if (intProp != null)
            intProp.unbind();
        if (doubleProp != null)
            doubleProp.unbind();
    }

    @Override
    public boolean isBound() {
        if (intProp != null)
            return intProp.isBound();
        if (doubleProp != null)
            return doubleProp.isBound();
        return false;
    }

    @Override
    public Object getBean() {
        if (intProp != null)
            return intProp.getBean();
        if (doubleProp != null)
            return doubleProp.getBean();
        return null;
    }

    @Override
    public String getName() {
        if (intProp != null)
            return intProp.getName();
        if (doubleProp != null)
            return doubleProp.getName();
        return null;
    }

    @Override
    public Number get() {
        if (intProp != null)
            return intProp.get();
        if (doubleProp != null)
            return doubleProp.get();
        return 0;
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        if (intProp != null)
            intProp.addListener(listener);
        if (doubleProp != null)
            doubleProp.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        if (intProp != null)
            intProp.removeListener(listener);
        if (doubleProp != null)
            doubleProp.removeListener(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        if (intProp != null)
            intProp.addListener(listener);
        if (doubleProp != null)
            doubleProp.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        if (intProp != null)
            intProp.removeListener(listener);
        if (doubleProp != null)
            doubleProp.removeListener(listener);
    }

    @Override
    public void set(Number value) {
        if (intProp != null)
            intProp.set((int) value);
        if (doubleProp != null)
            doubleProp.set((double) value);
    }
}
