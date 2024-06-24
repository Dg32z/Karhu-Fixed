/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.data.task;

public interface IAbstractTickTask<T> {
    public Runnable getRunnable();

    public EmptyPredicate conditionUntil();

    public String getId();
}

