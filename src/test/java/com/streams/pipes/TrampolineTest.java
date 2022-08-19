package com.streams.pipes;

import com.streams.pipes.model.MyTree;
import com.streams.pipes.model.Trampoline;
import com.streams.pipes.model.TreeOps;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class TrampolineTest {

    @Test
    public void testRet() {
        // Given
        int x = -1265660452;

        // When
        Trampoline<Integer> actual = Trampoline.pure(x);

        // Then
        int actualResult = actual.run();
        assertEquals(x, actualResult);
    }

    @Test
    public void testSuspendNull() {
        // When - Then
        expectException(
                () -> Trampoline.suspend(null),
                NullPointerException.class);
    }

    @Test
    public void testSuspend() {
        // Given
        int x = 812476349;

        // When
        Trampoline<Integer> actual = Trampoline.suspend(() -> Trampoline.pure(x));

        // Then
        int actualResult = actual.run();
        assertEquals(x, actualResult);
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testSuspendIsStackSafe() {
        // Given
        Trampoline<Integer> instance = suspended(1293394316, 100000);

        // When
        instance.run();
    }

    private static Trampoline<Integer> suspended(int x, int depth) {
        return (depth == 0) ?
                Trampoline.pure(x) :
                Trampoline.suspend(() -> suspended(x, depth - 1));
    }

    @Test
    public void testMapNull() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(0);

        // When - Then
        expectException(
                () -> instance.map(null),
                NullPointerException.class);
    }

    @Test
    public void testMapOnPure() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-2027639537);
        Function<Integer, Integer> f = x -> x - 1716704574;

        // When
        Trampoline<Integer> actual = instance.map(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testMapOnFlatMapped() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(1715890257).bind(x -> Trampoline.pure(x - 1046750814));
        Function<Integer, Integer> f = x -> x + 176502226;

        // When
        Trampoline<Integer> actual = instance.map(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run());
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testMapIsStackSafe() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-1396586044);
        Function<Integer, Integer> f = x -> x + 1568394758;
        for (int i = 0; i < 100000; ++i) {
            instance = instance.map(f);
        }

        // When
        instance.run();
    }

    @Test
    public void testFlatMapNull() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(0);

        // When - Then
        expectException(
                () -> instance.bind(null),
                NullPointerException.class);
    }

    @Test
    public void testFlatMapFromPureToPure() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-1546978697);
        Function<Integer, Trampoline<Integer>> f = x -> Trampoline.pure(x + 760609985);

        // When
        Trampoline<Integer> actual = instance.bind(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run()).run();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testFlatMapFromPureToFlatMapped() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-1701222901);
        Function<Integer, Trampoline<Integer>> f =
                x -> Trampoline.pure(858780315).bind(y -> Trampoline.pure(x + y + 637589551));

        // When
        Trampoline<Integer> actual = instance.bind(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run()).run();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testFlatMapFromFlatMappedToPure() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-1870413411).bind(x -> Trampoline.pure(x - 1338307766));
        Function<Integer, Trampoline<Integer>> f = x -> Trampoline.pure(x - 1400084479);

        // When
        Trampoline<Integer> actual = instance.bind(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run()).run();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testFlatMapFromFlatMappedToFlatMapped() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(-179065790).bind(x -> Trampoline.pure(x - 26564703));
        Function<Integer, Trampoline<Integer>> f =
                x -> Trampoline.pure(-1470929881).bind(y -> Trampoline.pure((x + y) - 1103607279));

        // When
        Trampoline<Integer> actual = instance.bind(f);

        // Then
        int actualResult = actual.run();
        int expectedResult = f.apply(instance.run()).run();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testLeftHeavyFlatMapIsStackSafe() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(1037279461);
        Function<Integer, Trampoline<Integer>> f = x -> Trampoline.pure(x - 1883573890);
        for (int i = 0; i < 100000; ++i) {
            instance = instance.bind(f);
        }

        // When
        instance.run();
    }

    @Test
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    public void testRightHeavyFlatMapIsStackSafe() {
        // Given
        Trampoline<Integer> instance = Trampoline.pure(774471901).bind(x -> rightHeavy(x, 100000));

        // When
        instance.run();
    }

    private static Trampoline<Integer> rightHeavy(int x, int depth) {
        return (depth == 0) ?
                Trampoline.pure(x - 391018460) :
                Trampoline.pure(1480669361).bind(y -> rightHeavy((x + y) - 1332171485, depth - 1));
    }

    /*
     * The following tests test identities that should hold for all monads (types with flatMap)
     */

    /*
     * The left identity monad law says that pure(x).flatMap(f) = f(x)
     */
    @Test
    public void testLeftIdentityMonadLaw() {
        // Given
        int x = -444582204;
        Function<Integer, Trampoline<Integer>> calcNextTrampoline = y -> Trampoline.pure(y - 136899410);

        // When
        int actual = Trampoline.pure(x).bind(calcNextTrampoline).run();

        // Then
        int expected = calcNextTrampoline.apply(x).run();
        assertEquals(expected, actual);
    }

    /*
     * The right identity monad law says that t.flatMap(x -> pure(x)) = t
     */
    @Test
    public void testRightIdentityMonadLaw() {
        // Given
        Trampoline<Integer> trampoline = Trampoline.pure(61690741);

        // When
        int actual = trampoline.bind(x->Trampoline.pure(x)).run();

        // Then
        int expected = trampoline.run();
        assertEquals(expected, actual);
    }

    /*
     * We use a rock-paper-scissors algebra to test that flatMap is associative even for non-associative algebras.
     *
     * RPS_BEST_OF[RPS_BEST_OF[0][1]][2] = RPS_BEST_OF[1][2] = 2
     * RPS_BEST_OF[0][RPS_BEST_OF[1][2]] = RPS_BEST_OF[0][2] = 0
     */
    private static final int[][] RPS_BEST_OF = {
            {0, 1, 0},
            {1, 1, 2},
            {0, 2, 2}};

    /*
     * The associativity monad law says that t.flatMap(f).flatMap(g) = t.flatMap(x -> f(x).flatMap(g))
     */
    @Test
    public void testAssociativityMonadLaw() {
        // Given
        Trampoline<Integer> trampoline = Trampoline.pure(0);
        Function<Integer, Trampoline<Integer>> calcNextTrampoline1 = x -> Trampoline.pure(RPS_BEST_OF[x][1]);
        Function<Integer, Trampoline<Integer>> calcNextTrampoline2 = x -> Trampoline.pure(RPS_BEST_OF[x][2]);

        // When
        int actual = trampoline.bind(calcNextTrampoline1).bind(calcNextTrampoline2).run();

        // Then
        int expected = trampoline.bind(x -> calcNextTrampoline1.apply(x).bind(calcNextTrampoline2)).run();
        assertEquals(expected, actual);
    }

    /*
     * Test t.map(f) = t.flatMap(x -> pure(f(x)))
     */
    @Test
    public void testMapToFlatMapRetIdentity() {
        // Given
        Trampoline<Integer> trampoline = Trampoline.pure(2123764208);
        Function<Integer, Integer> transformValue = x -> x + 1015637170;

        // When
        int actual = trampoline.map(transformValue).run();

        // Then
        int expected = trampoline.bind(x -> Trampoline.pure(transformValue.apply(x))).run();
        assertEquals(expected, actual);
    }

    /*
     * Test flatten(t) == t.flatMap(identity())
     */
    @Test
    public void testFlattenToFlatMapIdentityIdentity() {
        // Given
        Trampoline<Trampoline<Integer>> trampolinedTrampoline = Trampoline.pure(Trampoline.pure(232486295));

        // When
        int actual = flatten(trampolinedTrampoline).run();

        // Then
        int expected = trampolinedTrampoline.bind(trampoline -> trampoline).run();
        assertEquals(expected, actual);
    }

    /*
     * Test t.flatMap(f) = flatten(t.map(f))
     */
    @Test
    public void testFlatMapToFlattenMapIdentity() {
        // Given
        Trampoline<Integer> trampoline = Trampoline.pure(1411084928);
        Function<Integer, Trampoline<Integer>> calcNextTrampoline = x -> Trampoline.pure(x + 1625544605);

        // When
        int actual = trampoline.bind(calcNextTrampoline).run();

        // Then
        int expected = flatten(trampoline.map(calcNextTrampoline)).run();
        assertEquals(expected, actual);
    }

    private static <A> Trampoline<A> flatten(Trampoline<Trampoline<A>> trampolinedTrampoline) {
        return Trampoline.suspend(trampolinedTrampoline::run);
    }

    private static <A> void expectException(
            Supplier<A> thunkThatIsExpectedToThrowException,
            Class<? extends Exception> expectedExceptionClass) {

        try {
            thunkThatIsExpectedToThrowException.get();
            fail(String.format("Expected exception %s, but none was thrown", expectedExceptionClass));
        } catch (Exception actualException) {
            if (!expectedExceptionClass.isInstance(actualException)) {
                fail(String.format("Expected exception %s, but got %s", expectedExceptionClass, actualException));
            }
        }
    }
    @Test
    public void testFibonacciTrampoline() {
        // Given
        Trampoline<BigInteger> fibonacciTrampoline = sum(10000, TrampolineTest::reduce, BigInteger.ONE, BigInteger.ONE);
        System.out.println(fibonacciTrampoline.run());
    }
    public static BigInteger reduce(BigInteger a, BigInteger b) {
        return a.add(b);
    }
    public static Trampoline<BigInteger> sum(int n, BiFunction<BigInteger, BigInteger, BigInteger> reduce, BigInteger acc1, BigInteger acc2){
        return n < 3
                ? Trampoline.pure(acc1)
                : Trampoline.suspend(() -> sum(n - 1, TrampolineTest::reduce, reduce.apply(acc1, acc2), acc1));
    }

    @Test
    public void testFibonacciTrampoline2() {
        // Given
        MyTree<Integer> myTree=new MyTree(
                10,
                new MyTree(
                        20,
                        new MyTree(
                                -3,
                                new MyTree(
                                        -2,
                                        new MyTree(
                                                -6,
                                                new MyTree(-9, new MyTree(-1, null, null), null),
                                                null
                                                ),
                                        null
                                        ),
                                null
                                ),
                        new MyTree(-6, null, null)
                        ),
                new MyTree(30, new MyTree(13, null, null), new MyTree(16, null, null))
                );
        //new MyTree<>(100, new MyTree<>(12, new MyTree<>(13, new MyTree<>(15, null, null), new MyTree<>(16, null, null)), new MyTree<>(14, new MyTree<>(18, null, null), new MyTree<>(19, null, null))), new MyTree<>(-2, new MyTree<>(-3, new MyTree<>(-5, null, null), new MyTree<>(-6, null, null)), new MyTree<>(-4, new MyTree<>(-8, null, null), new MyTree<>(-9, null, null))));

        int integer=TreeOps.foldLeft(myTree, Integer::sum, 0);
        System.out.println(integer);
    }
}
