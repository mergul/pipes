package com.streams.pipes.model;

import java.util.function.BiFunction;

public final class TreeOps <A, B> {

    public static <A, B> B foldLeft(Tree<A> tree, BiFunction<B, A, B> reduce, B init) {
        return trampolinedFoldLeft(tree, reduce, init).run();
    }

    private static <A, B> Trampoline<B> trampolinedFoldLeft(Tree<A> tree, BiFunction<B, A, B> reduce, B init) {
        return tree.visit(
                (leafValue) -> Trampoline.pure(reduce.apply(init, leafValue)),
                (child) -> {
                    boolean isLeft = child.getLeft()!=null;
                    return Trampoline.suspend(() -> trampolinedFoldLeft(isLeft? child.getLeft(): child.getRight(), reduce, init).bind(acc ->
                            Trampoline.pure(reduce.apply(isLeft ? acc : (B) child.getValue(), isLeft? child.getValue(): (A) acc))));
                },
                (child) ->
                        Trampoline.suspend(() -> trampolinedFoldLeft(child.getLeft(), reduce, init)
                                .bind(leftAcc -> Trampoline.pure(reduce.apply(leftAcc, child.getValue())))
                                .bind(result -> trampolinedFoldLeft(child.getRight(), reduce, result)))
        );
    }

}
