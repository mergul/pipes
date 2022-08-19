package com.streams.pipes.model;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A class that represents a calculation which, when run, will not overflow the stack.
 *
 * @param <A> The type of the calculated value
 * @see <a href="https://mrbackend.github.io/java-trampoline/index.html">java-trampoline web site</a>
 */
public abstract class Trampolinex<A> {

    /**
     * Creates a {@code Trampoline} that represents a constant value.
     * <p>
     * When the returned trampoline is run, the value is returned.
     * <p>
     * The purpose of {@code just} is to represent the bottom values of recursions.
     *
     * @param value The value which will be returned by this {@code Trampoline}.
     * @param <A>   The type of the value
     * @return A new Trampoline that represents the value.
     */
    public static <A> Trampolinex<A> pure(A value, Optional<Tree<?>> args) {
        return new Return<>(value, args);
    }
    public static <A> Trampolinex<A> flatten(Trampolinex<A> monad, Function<? super A, Trampolinex<A>> func) {
        return new FlatMap<>(monad, func, Optional.empty());
    }
    /*
     * Used to encode suspend as a flatMap
     */
    private static final Trampolinex<Object> RETURN_DUMMY = pure(null, Optional.empty());

    /**
     * Creates a {@code Trampoline} that represents a thunk (an unevaluated calculation that takes no parameters).
     * <p>
     * When the returned trampoline is run, it will evaluate {@code thunk} to get a new trampoline and then run that one
     * to get and return its result.
     * <p>
     * The purpose of {@code suspend} is to avoid an immediate recursion, instead returning a trampoline that represents
     * the recursion to be run later.
     *
     * @param thunk A Supplier that will return a value
     * @param <A>   The type of the value
     * @return A new Trampoline that represents the unevaluated calculation.
     * @throws NullPointerException if thunk is null
     */
    public static <A> Trampolinex<A> suspend(Supplier<Trampolinex<A>> thunk, Optional<Tree<?>> args) {
        Objects.requireNonNull(thunk, "thunk");
        return RETURN_DUMMY.bind(ignored -> thunk.get(), args);
    }

    private Trampolinex() {
    }

    /**
     * Evaluates this {@code Trampoline} in constant stack space (disregarding the stack space that is consumed by
     * the functions used to construct this {@code Trampoline}).
     * <p>
     * This is achieved by evaluating each of the functions and sub-trampolines used to create the trampoline. The point
     * is that each of these steps is done in a loop, such that the stack does not grow during the evaluation.
     *
     * @return The calculated value
     */
    public final A run() {
        Trampolinex<A> curr = this;
        while (curr.isSuspended()) {
            //    System.out.println("resuming --> " + ((FlatMap<?,?>) curr).trampoline);
            curr = curr.resume();
        }
        return curr.getValue();
    }

    /**
     * Creates a {@code Trampoline} that represents the application of {@code transformValue} to the result of this
     * trampoline.
     * <p>
     * When the returned trampoline is run, it will first run this trampoline, then call {@code transformValue} and
     * return its result.
     * <p>
     * The purpose of {@code map} is to transform the resulting value of a trampoline. The transform must be stack safe;
     * if a deep recursion is performed to transform the value, it should be wrapped in a trampoline instead, and passed
     * to {@code flatMap} instead of {@code map}.
     *
     * @param transformValue The function used to transform the result of this Trampoline
     * @param <B>            The transformed value's type
     * @return A new Trampoline that represents the application of transformValue to the result of this Trampoline
     * @throws NullPointerException if transformValue is null
     */
    public final <B> Trampolinex<B> map(Function<? super A, B> transformValue, Optional<Tree<?>> args) {
        Objects.requireNonNull(transformValue, "transformValue");
        return bind(value -> pure(transformValue.apply(value), args), args);
    }

    /**
     * Creates a {@code Trampoline} that represents the application of {@code calcNextTrampoline} to the result of this
     * trampoline.
     * <p>
     * When the returned trampoline is run, it will first run this trampoline, then call {@code getNextTrampoline} to
     * get a new trampoline, and finally run the new trampoline and return its result.
     * <p>
     * The purpose of {@code flatMap} is to chain two recursions where the second recursion depends on the first one.
     *
     * @param <B>                The type of the value returned by the next Trampoline (and, consequently, the returned
     *                           Trampoline)
     * @param calcNextTrampoline The function used to calculate the next Trampoline
     * @param args
     * @return A new Trampoline that represents the sequence of this Trampoline followed by the next Trampoline
     * @throws NullPointerException if calcNextTrampoline is null
     */
    public final <B> Trampolinex<B> bind(Function<? super A, Trampolinex<B>> calcNextTrampoline, Optional<Tree<?>> args) {
        Objects.requireNonNull(calcNextTrampoline, "calcNextTrampoline");
        return new FlatMap<>(this, calcNextTrampoline, args);
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
    abstract Trampolinex<A> resume();

    /*
     * Apply the parent's calcNextTrampoline function to the result of this Trampoline
     */
    abstract <B> Trampolinex<B> applyFlatMap(Function<? super A, Trampolinex<B>> parentCalcNextTrampoline);

    abstract A getValue();
    abstract Optional<Tree<?>> getArgs();

    private static final class Return<A> extends Trampolinex<A> {

        private final A value;
        private final Optional<Tree<?>> args;

        private Return(A value, Optional<Tree<?>> args) {
            this.value = value;
            this.args = args;
            // System.out.println("Return -> "+(args.isPresent()?args.get().getValue():" :: no args"));
        }

        @Override
        boolean isSuspended() {
            return false;
        }

        @Override
        Trampolinex<A> resume() {
            throw new IllegalStateException();
        }

        // resume(FlatMap(Return(value), calcNextTrampoline)) ==> calcNextTrampoline.apply(value)
        @Override
        <B> Trampolinex<B> applyFlatMap(Function<? super A, Trampolinex<B>> parentCalcNextTrampoline) {
            return parentCalcNextTrampoline.apply(value);
        }

        @Override
        A getValue() {
            return value;
        }

        @Override
        Optional<Tree<?>> getArgs() {
            return args;
        }

    }

    private static class Suspend<A> extends Trampolinex<A> {

        private final Supplier<Trampolinex<A>> resume;
        private final Optional<Tree<?>> args;

        private Suspend(Supplier<Trampolinex<A>> resume, Optional<Tree<?>> args) {
            this.resume = resume;
            this.args = args;
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
        Optional<Tree<?>> getArgs() {
            return args;
        }

        @Override
        <B> Trampolinex<B> applyFlatMap(Function<? super A, Trampolinex<B>> parentCalcNextTrampoline) {
            return resume().applyFlatMap(parentCalcNextTrampoline);
        }

        @Override
        public Trampolinex<A> resume() {
            return resume.get();
        }
    }


    private static final class FlatMap<A, B> extends Trampolinex<B> {

        private final Trampolinex<A> trampoline;
        private final Function<? super A, Trampolinex<B>> calcNextTrampoline;
        private final Optional<Tree<?>> args;

        private FlatMap(Trampolinex<A> trampoline, Function<? super A, Trampolinex<B>> calcNextTrampoline, Optional<Tree<?>> args) {
            this.trampoline = trampoline;
            this.calcNextTrampoline = calcNextTrampoline;
            this.args = args;
            // System.out.println("FlatMap -> "+(args.isPresent()?args.get().getValue():" :: no args"));
        }

        @Override
        boolean isSuspended() {
            return true;
        }

        @Override
        Trampolinex<B> resume() {
            return trampoline.applyFlatMap(calcNextTrampoline);
        }

        // resume(FlatMap(FlatMap(trampoline, calcNextTrampoline), parentCalcNextTrampoline)) ==>
        // FlatMap(trampoline, value -> FlatMap(calcNextTrampoline.apply(value), parentCalcNextTrampoline))
        @Override
        <C> Trampolinex<C> applyFlatMap(Function<? super B, Trampolinex<C>> parentCalcNextTrampoline) {
            return trampoline.bind(value -> calcNextTrampoline.apply(value).bind(parentCalcNextTrampoline, args), args);
        }

        @Override
        B getValue() {
            throw new IllegalStateException();
        }

        @Override
        Optional<Tree<?>> getArgs() {
            return args;
        }

    }
}
