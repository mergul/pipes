package com.streams.pipes.model;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MyTree<A> extends Tree<A> {
    private final A value;
    private final Tree<A> left;
    private final Tree<A> right;

    public MyTree(A value, Tree<A> left, Tree<A> right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }

    @Override
    public <B> Trampoline<B> visit(
            Function<A, Trampoline<B>> onLeaf,
            Function<Tree<A>, Trampoline<B>> onUnaryBranch,
            BiFunction<Tree<A>, Tree<A>, Trampoline<B>> onBinaryBranch,
            BiFunction<B, A, Trampoline<B>> reducer) {
        System.out.println("visit -> " + this.value);
        if (isLeaf()) {
            System.out.println("visit -> " + this.value + " is leaf");
            return onLeaf.apply(value);
        } else {
            return (this.left != null && this.right != null 
            ? onBinaryBranch.apply(this.left, this.right)
            : onUnaryBranch.apply(this.left != null ? this.left : this.right))
            .bind((root) -> reducer.apply(root, this.value));
        }
    }

    @Override
    public A getValue() {
        return this.value;
    }

    @Override
    public Boolean isLeaf() {
        return this.left == null && this.right == null;
    }
}
abstract class Tree<A> {
    public abstract <B> Trampoline<B> visit(
            Function<A, Trampoline<B>> onLeaf,
            Function<Tree<A>, Trampoline<B>> onUnaryBranch,
            BiFunction<Tree<A>, Tree<A>, Trampoline<B>> onBinaryBranch,
            BiFunction<B, A, Trampoline<B>> reducer);

    public abstract A getValue();

    public abstract Boolean isLeaf();
}
