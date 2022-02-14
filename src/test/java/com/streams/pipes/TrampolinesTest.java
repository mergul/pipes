package com.streams.pipes;

import com.streams.pipes.model.MyTree;
import com.streams.pipes.model.Trampoline;
import com.streams.pipes.model.TreeOps;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.function.BiFunction;

public class TrampolinesTest {
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
    public void testTreeTraversalTrampoline() {
        // Given
        MyTree<String> myTree=new MyTree(
                ""+20,
                new MyTree(
                        ""+10,
                        new MyTree(
                                ""+-3,
                                new MyTree(
                                        ""+-4,
                                        new MyTree(
                                                ""+-6,
                                                new MyTree(""+-9, new MyTree(""+-11, null, new MyTree(""+-10, null, null)), null),
                                                null
                                                ),
                                        null
                                        ),
                                null
                                ),
                        new MyTree(""+16, null, null)
                        ),
                new MyTree(""+30, new MyTree(""+22, null, null), new MyTree(""+36, null, null))
                );
        //new MyTree<>(100, new MyTree<>(12, new MyTree<>(13, new MyTree<>(15, null, null), new MyTree<>(16, null, null)), new MyTree<>(14, new MyTree<>(18, null, null), new MyTree<>(19, null, null))), new MyTree<>(-2, new MyTree<>(-3, new MyTree<>(-5, null, null), new MyTree<>(-6, null, null)), new MyTree<>(-4, new MyTree<>(-8, null, null), new MyTree<>(-9, null, null))));

        System.out.println(TreeOps.foldLeft(myTree, (a, b)-> String.join(a.isEmpty()||b.isEmpty()?"":"|", a, b), ""));
    }
}
