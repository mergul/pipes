package com.streams.pipes.model;

import java.util.stream.Stream;

public interface ITrampoline<T> {

    T get();

    default ITrampoline<T> jump() {
        return this;
    }

    default T compute() {
        return get();
    }

    default boolean complete() {
        return true;
    }

    static <T> ITrampoline<T> done(final T result) {
        return () -> result;
    }

    static <T> ITrampoline<T> more(final ITrampoline<ITrampoline<T>> trampoline) {
        return new ITrampoline<T>() {
            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public ITrampoline<T> jump() {
                return trampoline.compute();
            }

            @Override
            public T get() {
                return trampoline(this);
            }

            T trampoline(final ITrampoline<T> trampoline) {
                return Stream.iterate(trampoline, ITrampoline::jump)
                        .filter(ITrampoline::complete)
                        .findFirst()
                        .map(ITrampoline::compute)
                        .orElseThrow();
            }
        };
    }
}

