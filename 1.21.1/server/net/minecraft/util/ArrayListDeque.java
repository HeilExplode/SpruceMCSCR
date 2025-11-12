/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.ListAndDeque;

public class ArrayListDeque<T>
extends AbstractList<T>
implements ListAndDeque<T> {
    private static final int MIN_GROWTH = 1;
    private Object[] contents;
    private int head;
    private int size;

    public ArrayListDeque() {
        this(1);
    }

    public ArrayListDeque(int n) {
        this.contents = new Object[n];
        this.head = 0;
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @VisibleForTesting
    public int capacity() {
        return this.contents.length;
    }

    private int getIndex(int n) {
        return (n + this.head) % this.contents.length;
    }

    @Override
    public T get(int n) {
        this.verifyIndexInRange(n);
        return this.getInner(this.getIndex(n));
    }

    private static void verifyIndexInRange(int n, int n2) {
        if (n < 0 || n >= n2) {
            throw new IndexOutOfBoundsException(n);
        }
    }

    private void verifyIndexInRange(int n) {
        ArrayListDeque.verifyIndexInRange(n, this.size);
    }

    private T getInner(int n) {
        return (T)this.contents[n];
    }

    @Override
    public T set(int n, T t) {
        this.verifyIndexInRange(n);
        Objects.requireNonNull(t);
        int n2 = this.getIndex(n);
        T t2 = this.getInner(n2);
        this.contents[n2] = t;
        return t2;
    }

    @Override
    public void add(int n, T t) {
        ArrayListDeque.verifyIndexInRange(n, this.size + 1);
        Objects.requireNonNull(t);
        if (this.size == this.contents.length) {
            this.grow();
        }
        int n2 = this.getIndex(n);
        if (n == this.size) {
            this.contents[n2] = t;
        } else if (n == 0) {
            --this.head;
            if (this.head < 0) {
                this.head += this.contents.length;
            }
            this.contents[this.getIndex((int)0)] = t;
        } else {
            for (int i = this.size - 1; i >= n; --i) {
                this.contents[this.getIndex((int)(i + 1))] = this.contents[this.getIndex(i)];
            }
            this.contents[n2] = t;
        }
        ++this.modCount;
        ++this.size;
    }

    private void grow() {
        int n = this.contents.length + Math.max(this.contents.length >> 1, 1);
        Object[] objectArray = new Object[n];
        this.copyCount(objectArray, this.size);
        this.head = 0;
        this.contents = objectArray;
    }

    @Override
    public T remove(int n) {
        this.verifyIndexInRange(n);
        int n2 = this.getIndex(n);
        T t = this.getInner(n2);
        if (n == 0) {
            this.contents[n2] = null;
            ++this.head;
        } else if (n == this.size - 1) {
            this.contents[n2] = null;
        } else {
            for (int i = n + 1; i < this.size; ++i) {
                this.contents[this.getIndex((int)(i - 1))] = this.get(i);
            }
            this.contents[this.getIndex((int)(this.size - 1))] = null;
        }
        ++this.modCount;
        --this.size;
        return t;
    }

    @Override
    public boolean removeIf(Predicate<? super T> predicate) {
        int n = 0;
        for (int i = 0; i < this.size; ++i) {
            T t = this.get(i);
            if (predicate.test(t)) {
                ++n;
                continue;
            }
            if (n == 0) continue;
            this.contents[this.getIndex((int)(i - n))] = t;
            this.contents[this.getIndex((int)i)] = null;
        }
        this.modCount += n;
        this.size -= n;
        return n != 0;
    }

    private void copyCount(Object[] objectArray, int n) {
        for (int i = 0; i < n; ++i) {
            objectArray[i] = this.get(i);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<T> unaryOperator) {
        for (int i = 0; i < this.size; ++i) {
            int n = this.getIndex(i);
            this.contents[n] = Objects.requireNonNull(unaryOperator.apply(this.getInner(i)));
        }
    }

    @Override
    public void forEach(Consumer<? super T> consumer) {
        for (int i = 0; i < this.size; ++i) {
            consumer.accept(this.get(i));
        }
    }

    @Override
    public void addFirst(T t) {
        this.add(0, t);
    }

    @Override
    public void addLast(T t) {
        this.add(this.size, t);
    }

    @Override
    public boolean offerFirst(T t) {
        this.addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        this.addLast(t);
        return true;
    }

    @Override
    public T removeFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.remove(0);
    }

    @Override
    public T removeLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.remove(this.size - 1);
    }

    @Override
    public ListAndDeque<T> reversed() {
        return new ReversedView(this);
    }

    @Override
    @Nullable
    public T pollFirst() {
        if (this.size == 0) {
            return null;
        }
        return this.removeFirst();
    }

    @Override
    @Nullable
    public T pollLast() {
        if (this.size == 0) {
            return null;
        }
        return this.removeLast();
    }

    @Override
    public T getFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.get(0);
    }

    @Override
    public T getLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.get(this.size - 1);
    }

    @Override
    @Nullable
    public T peekFirst() {
        if (this.size == 0) {
            return null;
        }
        return this.getFirst();
    }

    @Override
    @Nullable
    public T peekLast() {
        if (this.size == 0) {
            return null;
        }
        return this.getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object object) {
        for (int i = 0; i < this.size; ++i) {
            T t = this.get(i);
            if (!Objects.equals(object, t)) continue;
            this.remove(i);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object object) {
        for (int i = this.size - 1; i >= 0; --i) {
            T t = this.get(i);
            if (!Objects.equals(object, t)) continue;
            this.remove(i);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new DescendingIterator();
    }

    @Override
    public /* synthetic */ List reversed() {
        return this.reversed();
    }

    @Override
    public /* synthetic */ SequencedCollection reversed() {
        return this.reversed();
    }

    @Override
    public /* synthetic */ Deque reversed() {
        return this.reversed();
    }

    class ReversedView
    extends AbstractList<T>
    implements ListAndDeque<T> {
        private final ArrayListDeque<T> source;

        public ReversedView(ArrayListDeque<T> arrayListDeque2) {
            this.source = arrayListDeque2;
        }

        @Override
        public ListAndDeque<T> reversed() {
            return this.source;
        }

        @Override
        public T getFirst() {
            return this.source.getLast();
        }

        @Override
        public T getLast() {
            return this.source.getFirst();
        }

        @Override
        public void addFirst(T t) {
            this.source.addLast(t);
        }

        @Override
        public void addLast(T t) {
            this.source.addFirst(t);
        }

        @Override
        public boolean offerFirst(T t) {
            return this.source.offerLast(t);
        }

        @Override
        public boolean offerLast(T t) {
            return this.source.offerFirst(t);
        }

        @Override
        public T pollFirst() {
            return this.source.pollLast();
        }

        @Override
        public T pollLast() {
            return this.source.pollFirst();
        }

        @Override
        public T peekFirst() {
            return this.source.peekLast();
        }

        @Override
        public T peekLast() {
            return this.source.peekFirst();
        }

        @Override
        public T removeFirst() {
            return this.source.removeLast();
        }

        @Override
        public T removeLast() {
            return this.source.removeFirst();
        }

        @Override
        public boolean removeFirstOccurrence(Object object) {
            return this.source.removeLastOccurrence(object);
        }

        @Override
        public boolean removeLastOccurrence(Object object) {
            return this.source.removeFirstOccurrence(object);
        }

        @Override
        public Iterator<T> descendingIterator() {
            return this.source.iterator();
        }

        @Override
        public int size() {
            return this.source.size();
        }

        @Override
        public boolean isEmpty() {
            return this.source.isEmpty();
        }

        @Override
        public boolean contains(Object object) {
            return this.source.contains(object);
        }

        @Override
        public T get(int n) {
            return this.source.get(this.reverseIndex(n));
        }

        @Override
        public T set(int n, T t) {
            return this.source.set(this.reverseIndex(n), t);
        }

        @Override
        public void add(int n, T t) {
            this.source.add(this.reverseIndex(n) + 1, t);
        }

        @Override
        public T remove(int n) {
            return this.source.remove(this.reverseIndex(n));
        }

        @Override
        public int indexOf(Object object) {
            return this.reverseIndex(this.source.lastIndexOf(object));
        }

        @Override
        public int lastIndexOf(Object object) {
            return this.reverseIndex(this.source.indexOf(object));
        }

        @Override
        public List<T> subList(int n, int n2) {
            return this.source.subList(this.reverseIndex(n2) + 1, this.reverseIndex(n) + 1).reversed();
        }

        @Override
        public Iterator<T> iterator() {
            return this.source.descendingIterator();
        }

        @Override
        public void clear() {
            this.source.clear();
        }

        private int reverseIndex(int n) {
            return n == -1 ? -1 : this.source.size() - 1 - n;
        }

        @Override
        public /* synthetic */ List reversed() {
            return this.reversed();
        }

        @Override
        public /* synthetic */ SequencedCollection reversed() {
            return this.reversed();
        }

        @Override
        public /* synthetic */ Deque reversed() {
            return this.reversed();
        }
    }

    class DescendingIterator
    implements Iterator<T> {
        private int index;

        public DescendingIterator() {
            this.index = ArrayListDeque.this.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return this.index >= 0;
        }

        @Override
        public T next() {
            return ArrayListDeque.this.get(this.index--);
        }

        @Override
        public void remove() {
            ArrayListDeque.this.remove(this.index + 1);
        }
    }
}

