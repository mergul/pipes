package com.streams.pipes.model;

public class CustomPair<T, U> {
    private final T key;
    private final U value;

    public CustomPair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public U getValue() {
        return value;
    }
}
