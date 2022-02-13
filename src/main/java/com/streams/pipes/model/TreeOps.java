package com.streams.pipes.model;

import java.util.function.BiFunction;

public final class TreeOps <A, B> {

    public static <A, B> B foldLeft(Tree<A> tree, BiFunction<B, A, B> reduce, B init) {
        return trampolinedFoldLeft(tree, reduce, init).run();
    }

    private static <A, B> Trampoline<B> trampolinedFoldLeft(Tree<A> tree, BiFunction<B, A, B> reduce, B init) {
        return tree.visit(
                leafValue -> Trampoline.pure(reduce.apply(init, leafValue)),
                child -> Trampoline.suspend(() -> trampolinedFoldLeft(child, reduce, init)),
                (leftChild, rightChild) ->
                        Trampoline.suspend(() -> trampolinedFoldLeft(leftChild, reduce, init))
                                .bind(leftAcc -> trampolinedFoldLeft(rightChild, reduce, leftAcc)),
                (bValue, aValue) -> Trampoline.pure(reduce.apply( bValue, aValue))
        );
    }

}
