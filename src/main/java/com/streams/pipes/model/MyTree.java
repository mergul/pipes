package com.streams.pipes.model;

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
            Function<Tree<A>, Trampoline<B>> onBinaryBranch) {
        return isLeaf()
                ? onLeaf.apply(value) :
                (this.left != null && this.right != null)
                        ? onBinaryBranch.apply(this)
                        : onUnaryBranch.apply(this);
    }

    @Override
    public A getValue() {
        return this.value;
    }

    @Override
    public Boolean isLeaf() {
        return this.left == null && this.right == null;
    }

    @Override
    public Tree<A> getLeft() {
        return this.left;
    }

    @Override
    public Tree<A> getRight() {
        return this.right;
    }
}

abstract class Tree<A> {
    public abstract <B> Trampoline<B> visit(
            Function<A, Trampoline<B>> onLeaf,
            Function<Tree<A>, Trampoline<B>> onUnaryBranch,
            Function<Tree<A>, Trampoline<B>> onBinaryBranch);

    public abstract A getValue();

    public abstract Boolean isLeaf();

    public abstract Tree<A> getLeft();

    public abstract Tree<A> getRight();
}
