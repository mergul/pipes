package com.streams.pipes.model;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Trampoline<A> {
    public static <A> Trampoline<A> pure(A value) {
        return new Return<>(value);
    }

    public static <A> Trampoline<A> suspend(Supplier<Trampoline<A>> thunk) {
        Objects.requireNonNull(thunk, "thunk");
        return new Suspend<>(thunk);
    }

    private Trampoline() {
    }

    public final A run() {
        Trampoline<A> curr = this;
        while (curr.isSuspended()) {
            //    System.out.println("resuming --> " + ((FlatMap<?,?>) curr).trampoline);
            curr = curr.resume();
        }
        return curr.getValue();
    }

    public final <B> Trampoline<B> map(Function<? super A, B> transformValue) {
        Objects.requireNonNull(transformValue, "transformValue");
        return bind(value -> pure(transformValue.apply(value)));
    }

    public final <B> Trampoline<B> bind(Function<? super A, Trampoline<B>> calcNextTrampoline) {
        Objects.requireNonNull(calcNextTrampoline, "calcNextTrampoline");
        return new FlatMap<>(this, calcNextTrampoline);
    }

    abstract boolean isSuspended();
    /*
     * Transforms this Trampoline to one that's one step closer to a Return(value).
     *
     * resume(FlatMap(Return(value), calcNextTrampoline)) ==> calcNextTrampoline.apply(value)=Return(value),
     * resume(FlatMap(FlatMap(trampoline, calcNextTrampoline), parentCalcNextTrampoline)) ==>
     *    FlatMap(trampoline, value -> FlatMap(calcNextTrampoline.apply(value), parentCalcNextTrampoline)) ==>
     *
     *
     * Throws IllegalStateException if called for a Return(value)
     */
    abstract Trampoline<A> resume();

    abstract <B> Trampoline<B> applyFlatMap(Function<? super A, Trampoline<B>> parentCalcNextTrampoline);

    abstract A getValue();

    private static final class Return<A> extends Trampoline<A> {

        private final A value;

        private Return(A value) {
            this.value = value;
        }

        @Override
        boolean isSuspended() {
            return false;
        }

        @Override
        Trampoline<A> resume() {
            throw new IllegalStateException();
        }

        // resume(FlatMap(Return(value), calcNextTrampoline)) ==> calcNextTrampoline.apply(value)=Return(value)
        @Override
        <B> Trampoline<B> applyFlatMap(Function<? super A, Trampoline<B>> parentCalcNextTrampoline) {
            return parentCalcNextTrampoline.apply(value);
        }

        @Override
        A getValue() {
            return value;
        }

    }

    private static class Suspend<A> extends Trampoline<A> {

        private final Supplier<Trampoline<A>> resume;

        private Suspend(Supplier<Trampoline<A>> resume) {
            this.resume = resume;
        }

        @Override
        public boolean isSuspended() {
            return true;
        }

        @Override
        public A getValue() {
            throw new IllegalStateException("Suspend has no value");
        }

        @Override
        <B> Trampoline<B> applyFlatMap(Function<? super A, Trampoline<B>> parentCalcNextTrampoline) {
            return resume().applyFlatMap(parentCalcNextTrampoline);
        }
      
        // resume(FlatMap(Suspend(Supplier), calcNextTrampoline)) ==> calcNextTrampoline.apply(value)=Suspend(Supplier)
        @Override
        public Trampoline<A> resume() {
            return resume.get();
        }
    }


    private static final class FlatMap<A, B> extends Trampoline<B> {

        private final Trampoline<A> trampoline;
        private final Function<? super A, Trampoline<B>> calcNextTrampoline;

        private FlatMap(Trampoline<A> trampoline, Function<? super A, Trampoline<B>> calcNextTrampoline) {
            this.trampoline = trampoline;
            this.calcNextTrampoline = calcNextTrampoline;
        }

        @Override
        boolean isSuspended() {
            return true;
        }

        @Override
        Trampoline<B> resume() {
            return trampoline.applyFlatMap(calcNextTrampoline);
        }

        // resume(FlatMap(FlatMap(trampoline, calcNextTrampoline), parentCalcNextTrampoline)) ==>
        // FlatMap(trampoline, value -> FlatMap(calcNextTrampoline.apply(value), parentCalcNextTrampoline))
        @Override
        <C> Trampoline<C> applyFlatMap(Function<? super B, Trampoline<C>> parentCalcNextTrampoline) {
            return trampoline.bind(value -> calcNextTrampoline.apply(value).bind(parentCalcNextTrampoline));
        }

        @Override
        B getValue() {
            throw new IllegalStateException();
        }

    }
}
