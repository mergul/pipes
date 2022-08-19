package com.streams.pipes.model;

import java.util.Optional;

public class MyTrampoline<T>
{
    public T getValue() {
        throw new RuntimeException("Not implemented");
    }

    public Optional<MyTrampoline<T>> nextTrampoline() {
        return Optional.empty();
    }

    public final T compute() {
        MyTrampoline<T> trampoline = this;

        while (trampoline.nextTrampoline().isPresent()) {
            trampoline = trampoline.nextTrampoline().get();
        }

        return trampoline.getValue();
    }
}
