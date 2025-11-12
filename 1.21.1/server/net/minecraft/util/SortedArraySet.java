/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrays
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

public class SortedArraySet<T>
extends AbstractSet<T> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private final Comparator<T> comparator;
    T[] contents;
    int size;

    private SortedArraySet(int n, Comparator<T> comparator) {
        this.comparator = comparator;
        if (n < 0) {
            throw new IllegalArgumentException("Initial capacity (" + n + ") is negative");
        }
        this.contents = SortedArraySet.castRawArray(new Object[n]);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create() {
        return SortedArraySet.create(10);
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create(int n) {
        return new SortedArraySet(n, Comparator.naturalOrder());
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator) {
        return SortedArraySet.create(comparator, 10);
    }

    public static <T> SortedArraySet<T> create(Comparator<T> comparator, int n) {
        return new SortedArraySet<T>(n, comparator);
    }

    private static <T> T[] castRawArray(Object[] objectArray) {
        return objectArray;
    }

    private int findIndex(T t) {
        return Arrays.binarySearch(this.contents, 0, this.size, t, this.comparator);
    }

    private static int getInsertionPosition(int n) {
        return -n - 1;
    }

    @Override
    public boolean add(T t) {
        int n = this.findIndex(t);
        if (n >= 0) {
            return false;
        }
        int n2 = SortedArraySet.getInsertionPosition(n);
        this.addInternal(t, n2);
        return true;
    }

    private void grow(int n) {
        if (n <= this.contents.length) {
            return;
        }
        if (this.contents != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
            n = (int)Math.max(Math.min((long)this.contents.length + (long)(this.contents.length >> 1), 0x7FFFFFF7L), (long)n);
        } else if (n < 10) {
            n = 10;
        }
        Object[] objectArray = new Object[n];
        System.arraycopy(this.contents, 0, objectArray, 0, this.size);
        this.contents = SortedArraySet.castRawArray(objectArray);
    }

    private void addInternal(T t, int n) {
        this.grow(this.size + 1);
        if (n != this.size) {
            System.arraycopy(this.contents, n, this.contents, n + 1, this.size - n);
        }
        this.contents[n] = t;
        ++this.size;
    }

    void removeInternal(int n) {
        --this.size;
        if (n != this.size) {
            System.arraycopy(this.contents, n + 1, this.contents, n, this.size - n);
        }
        this.contents[this.size] = null;
    }

    private T getInternal(int n) {
        return this.contents[n];
    }

    public T addOrGet(T t) {
        int n = this.findIndex(t);
        if (n >= 0) {
            return this.getInternal(n);
        }
        this.addInternal(t, SortedArraySet.getInsertionPosition(n));
        return t;
    }

    @Override
    public boolean remove(Object object) {
        int n = this.findIndex(object);
        if (n >= 0) {
            this.removeInternal(n);
            return true;
        }
        return false;
    }

    @Nullable
    public T get(T t) {
        int n = this.findIndex(t);
        if (n >= 0) {
            return this.getInternal(n);
        }
        return null;
    }

    public T first() {
        return this.getInternal(0);
    }

    public T last() {
        return this.getInternal(this.size - 1);
    }

    @Override
    public boolean contains(Object object) {
        int n = this.findIndex(object);
        return n >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.contents, this.size, Object[].class);
    }

    @Override
    public <U> U[] toArray(U[] UArray) {
        if (UArray.length < this.size) {
            return Arrays.copyOf(this.contents, this.size, UArray.getClass());
        }
        System.arraycopy(this.contents, 0, UArray, 0, this.size);
        if (UArray.length > this.size) {
            UArray[this.size] = null;
        }
        return UArray;
    }

    @Override
    public void clear() {
        Arrays.fill(this.contents, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof SortedArraySet) {
            SortedArraySet sortedArraySet = (SortedArraySet)object;
            if (this.comparator.equals(sortedArraySet.comparator)) {
                return this.size == sortedArraySet.size && Arrays.equals(this.contents, sortedArraySet.contents);
            }
        }
        return super.equals(object);
    }

    class ArrayIterator
    implements Iterator<T> {
        private int index;
        private int last = -1;

        ArrayIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.index < SortedArraySet.this.size;
        }

        @Override
        public T next() {
            if (this.index >= SortedArraySet.this.size) {
                throw new NoSuchElementException();
            }
            this.last = this.index++;
            return SortedArraySet.this.contents[this.last];
        }

        @Override
        public void remove() {
            if (this.last == -1) {
                throw new IllegalStateException();
            }
            SortedArraySet.this.removeInternal(this.last);
            --this.index;
            this.last = -1;
        }
    }
}

