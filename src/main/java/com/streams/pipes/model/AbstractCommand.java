package com.streams.pipes.model;

public abstract class AbstractCommand<T> {
    String key;
    T value;

    public abstract String getId();

    public abstract T getValue();
}
