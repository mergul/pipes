package com.streams.pipes;

import com.streams.pipes.model.CustomPair;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContinuationTest {
    @FunctionalInterface
    private interface Continuation<R> {
        Thunk apply(R result);
    }
    @FunctionalInterface
    private interface Thunk {
        Thunk run();
    }
    private static void trampoline(Thunk thunk) {
        while (thunk != null) {
            thunk = thunk.run();
        }
    }
    public static <T> Thunk lazy(Thunk thunk) {
        return () -> thunk;
    }
    private static Thunk add(BigInteger a, BigInteger b, Continuation<BigInteger> cont) {
        BigInteger sum = a.add(b);
        return () -> cont.apply(sum);
    }
    public static Thunk multiply(BigInteger value, BigInteger product, Continuation<BigInteger> cont) {
        return () -> cont.apply(value.multiply(product));
    }
    private static Thunk subtract(int a, int b, Continuation<Integer> cont) {
        int sum = a - b;
        return () -> cont.apply(sum);
    }
    private static Thunk eq(int a, int b, Continuation<Boolean> cont) {
        boolean result = (a == b);
        return () -> cont.apply(result);
    }
    private static Thunk lt(int a, int b, Continuation<Boolean> cont) {
        boolean result = (a < b);
        return () -> cont.apply(result);
    }
    public static Thunk switchIt(int a, Continuation<Integer> firstBranch, Continuation<Integer> secondBranch, Continuation<Integer> thirdBranch) {
        switch (a) {
            case 1: return () -> secondBranch.apply( 1);
            case 2: return () -> thirdBranch.apply(2);
            default: return () -> firstBranch.apply(0);
        }
    }
    private static Thunk iff(boolean expr,
                             Continuation<Boolean> trueBranch,
                             Continuation<Boolean> falseBranch) {
        return (expr)
                ? () -> trueBranch.apply(true)
                : () -> falseBranch.apply(false);
    }
    private static Thunk fib(int n, BigInteger acc1, BigInteger acc2, Continuation<BigInteger> cont) {
        return lt(n, 3, nlt2 ->
                iff(nlt2,
                        trueArg -> cont.apply(acc1),
                        falseArg -> subtract(n, 1, nm1 ->
                                add(acc1, acc2, nm2 ->
                                        fib(nm1, nm2, acc1, cont)))));
    }
    private static Thunk factorial(int n, Continuation<BigInteger> cont) {
        return eq(n, 0, isNZero ->
                iff(isNZero,
                        trueArg -> cont.apply(BigInteger.ONE),
                        falseArg -> subtract(n, 1, nm1 ->
                                factorial(nm1, fnm1 ->
                                        multiply(BigInteger.valueOf(n), fnm1, cont)))));
    }
    private static <T> Continuation<T> endCall(Consumer<T> call) {
        return r -> {
            call.accept(r);
            return null;
        };
    }
    @Test
    public void testContinuation() {
        AtomicReference<BigInteger> result = new AtomicReference<>(BigInteger.ZERO);
        Consumer<BigInteger> consumer = result::set;
        Continuation<BigInteger> continuation = endCall(consumer);
        Thunk thunk = () -> continuation.apply(BigInteger.valueOf(42));
        thunk.run();
        assertEquals(BigInteger.valueOf(42), result.get());
    }
    @Test
    public void testTrampoline() {
        AtomicReference<BigInteger> result = new AtomicReference<>();
        Thunk thunk = add(BigInteger.ONE, BigInteger.TEN, (value) -> multiply(value, BigInteger.TEN, (value2) -> {
            result.set(value2);
            return null;
        }));
        trampoline(thunk);
        assertEquals(BigInteger.valueOf(110), result.get());
    }
    @Test
    public void testFib() {
        AtomicReference<BigInteger> result = new AtomicReference<>(BigInteger.ZERO);
        trampoline(fib(10, BigInteger.ONE, BigInteger.ONE, (value) -> () -> {
            result.set(value);
            return null;
        }));
        assertEquals(BigInteger.valueOf(55), result.get());
    }
    @Test
    public void ramping_works() {
        AtomicReference<BigInteger> res = new AtomicReference<>(BigInteger.ZERO);

        trampoline(factorial(10, (value) -> () -> { res.set(value); return null; }));
        assertEquals(new BigInteger("3628800"), res.get());

        trampoline(fib(10000, BigInteger.ONE, BigInteger.ONE, (value) -> () -> { res.set(value); return null; }));
        assertEquals(new BigInteger("33644764876431783266621612005107543310302148460680063906564769974680081442166662368155595513633734025582065332680836159373734790483865268263040892463056431887354544369559827491606602099884183933864652731300088830269235673613135117579297437854413752130520504347701602264758318906527890855154366159582987279682987510631200575428783453215515103870818298969791613127856265033195487140214287532698187962046936097879900350962302291026368131493195275630227837628441540360584402572114334961180023091208287046088923962328835461505776583271252546093591128203925285393434620904245248929403901706233888991085841065183173360437470737908552631764325733993712871937587746897479926305837065742830161637408969178426378624212835258112820516370298089332099905707920064367426202389783111470054074998459250360633560933883831923386783056136435351892133279732908133732642652633989763922723407882928177953580570993691049175470808931841056146322338217465637321248226383092103297701648054726243842374862411453093812206564914032751086643394517512161526545361333111314042436854805106765843493523836959653428071768775328348234345557366719731392746273629108210679280784718035329131176778924659089938635459327894523777674406192240337638674004021330343297496902028328145933418826817683893072003634795623117103101291953169794607632737589253530772552375943788434504067715555779056450443016640119462580972216729758615026968443146952034614932291105970676243268515992834709891284706740862008587135016260312071903172086094081298321581077282076353186624611278245537208532365305775956430072517744315051539600905168603220349163222640885248852433158051534849622434848299380905070483482449327453732624567755879089187190803662058009594743150052402532709746995318770724376825907419939632265984147498193609285223945039707165443156421328157688908058783183404917434556270520223564846495196112460268313970975069382648706613264507665074611512677522748621598642530711298441182622661057163515069260029861704945425047491378115154139941550671256271197133252763631939606902895650288268608362241082050562430701794976171121233066073310059947366875"), res.get());
        System.out.println(trampolineX(10000));
    }
    // The thunk returning version
    private CustomPair<Supplier<?>, BigInteger> factorial(long number, BigInteger sum){
        return (number == 1)
                ? new CustomPair<>(null, sum)
                : new CustomPair<>(() -> factorial(number - 1, sum.multiply(BigInteger.valueOf(number))), null);
    }
    private BigInteger trampolineX(long number) {
        CustomPair<Supplier<?>, BigInteger> supplierOrResult = new CustomPair<>(() -> factorial(number, BigInteger.ONE), null);

        while (supplierOrResult.getValue() == null) {
            supplierOrResult = (CustomPair<Supplier<?>, BigInteger>) supplierOrResult.getKey().get();
        }

        return supplierOrResult.getValue();
    }
}

