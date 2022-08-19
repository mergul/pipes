package com.streams.pipes.model;

import java.math.BigInteger;
import java.util.Optional;

public final class Factorial {

    public static MyTrampoline<Integer> createFactTrampoline(final int n, final int sum) {
        if (n == 1) return new MyTrampoline<>() {
            public Integer getValue() {
                return sum;
            }
        };

        return new MyTrampoline<>() {
            public Optional<MyTrampoline<Integer>> nextTrampoline() {
                return Optional.of(createFactTrampoline(n - 1, sum * n));
            }
        };
    }

    public static MyTrampoline<BigInteger> createFibTrampoline(final int n, final BigInteger acc1, final BigInteger acc2) {
        if (n < 3) return new MyTrampoline<>() {
            public BigInteger getValue() {
                return acc1;
            }
        };
        return new MyTrampoline<>() {
            public Optional<MyTrampoline<BigInteger>> nextTrampoline() {
                return Optional.of(createFibTrampoline(n - 1, acc1.add(acc2), acc1));
            }
        };
    }
    public static ITrampoline<BigInteger> loop(int times, BigInteger prod, BigInteger acc) {
        return times < 3
                ? ITrampoline.done(prod) :
                ITrampoline.more(() -> loop(times - 1, prod.add(acc), prod));
    }
    private static TailCall<BigInteger> sum(int arg, BigInteger acc1, BigInteger acc2) {
        return arg < 3
                ? TailCall.pure(acc1)
                : TailCall.suspend(() -> sum(arg - 1, acc1.add(acc2), acc1));
    }
    public static void main(String[] args) {
        MyTrampoline<Integer> factorial = createFactTrampoline(5, 1);
        System.out.println(factorial.compute());
        MyTrampoline<BigInteger> fib = createFibTrampoline(100, BigInteger.ONE, BigInteger.ONE);
        System.out.println(fib.compute());
        System.out.println(loop(100, BigInteger.ONE, BigInteger.ONE).compute());
        System.out.println(sum(100, BigInteger.ONE, BigInteger.ONE).run());
        System.out.println(ICustomerRegistrationValidator.isEmailValid().and(ICustomerRegistrationValidator.isPhoneValid()).apply(UserPayload.of("@1234").withIsAdmin(true).build()));
    }
}
