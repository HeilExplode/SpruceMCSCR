/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.core;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E>
extends AbstractList<E> {
    private final List<E> list;
    @Nullable
    private final E defaultValue;

    public static <E> NonNullList<E> create() {
        return new NonNullList<Object>(Lists.newArrayList(), null);
    }

    public static <E> NonNullList<E> createWithCapacity(int n) {
        return new NonNullList<Object>(Lists.newArrayListWithCapacity((int)n), null);
    }

    public static <E> NonNullList<E> withSize(int n, E e) {
        Validate.notNull(e);
        Object[] objectArray = new Object[n];
        Arrays.fill(objectArray, e);
        return new NonNullList<Object>(Arrays.asList(objectArray), e);
    }

    @SafeVarargs
    public static <E> NonNullList<E> of(E e, E ... EArray) {
        return new NonNullList<E>(Arrays.asList(EArray), e);
    }

    protected NonNullList(List<E> list, @Nullable E e) {
        this.list = list;
        this.defaultValue = e;
    }

    @Override
    @Nonnull
    public E get(int n) {
        return this.list.get(n);
    }

    @Override
    public E set(int n, E e) {
        Validate.notNull(e);
        return this.list.set(n, e);
    }

    @Override
    public void add(int n, E e) {
        Validate.notNull(e);
        this.list.add(n, e);
    }

    @Override
    public E remove(int n) {
        return this.list.remove(n);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        } else {
            for (int i = 0; i < this.size(); ++i) {
                this.set(i, this.defaultValue);
            }
        }
    }
}

