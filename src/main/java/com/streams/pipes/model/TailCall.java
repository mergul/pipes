package com.streams.pipes.model;

import java.util.function.Supplier;

public abstract class TailCall<T> {
    private TailCall() {}
    public final T run() {
        TailCall<T> curr = this;
        while (curr.isSuspended()) {
            curr = curr.resume();
        }
        return curr.getValue();
    }
    public abstract boolean isSuspended();
    public abstract TailCall<T> resume();
    public abstract T getValue();

    public static class Return<T> extends TailCall<T> {

        private final T t;

        private Return(T t) {
            this.t = t;
        }

        @Override
        public boolean isSuspended() {
            return false;
        }

        @Override
        public T getValue() {
            return t;
        }

        @Override
        public TailCall<T> resume() {
            throw new IllegalStateException("Return has no resume");
        }
    }

    public static class Suspend<T> extends TailCall<T> {

        private final Supplier<TailCall<T>> resume;

        private Suspend(Supplier<TailCall<T>> resume) {
            this.resume = resume;
        }

        @Override
        public boolean isSuspended() {
            return true;
        }

        @Override
        public T getValue() {
            throw new IllegalStateException("Suspend has no value");
        }

        @Override
        public TailCall<T> resume() {
            return resume.get();
        }
    }

    public static <T> Return<T> pure(T t) {
        return new Return<>(t);
    }

    public static <T> Suspend<T> suspend(Supplier<TailCall<T>> thunk) {
        return new Suspend<>(thunk);
    }
}
