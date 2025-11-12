/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.HashCommon
 *  it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 */
package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.NoSuchElementException;
import net.minecraft.util.Mth;

public class SpatialLongSet
extends LongLinkedOpenHashSet {
    private final InternalMap map;

    public SpatialLongSet(int n, float f) {
        super(n, f);
        this.map = new InternalMap(n / 64, f);
    }

    public boolean add(long l) {
        return this.map.addBit(l);
    }

    public boolean rem(long l) {
        return this.map.removeBit(l);
    }

    public long removeFirstLong() {
        return this.map.removeFirstBit();
    }

    public int size() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    protected static class InternalMap
    extends Long2LongLinkedOpenHashMap {
        private static final int X_BITS = Mth.log2(60000000);
        private static final int Z_BITS = Mth.log2(60000000);
        private static final int Y_BITS;
        private static final int Y_OFFSET = 0;
        private static final int Z_OFFSET;
        private static final int X_OFFSET;
        private static final long OUTER_MASK;
        private int lastPos = -1;
        private long lastOuterKey;
        private final int minSize;

        public InternalMap(int n, float f) {
            super(n, f);
            this.minSize = n;
        }

        static long getOuterKey(long l) {
            return l & (OUTER_MASK ^ 0xFFFFFFFFFFFFFFFFL);
        }

        static int getInnerKey(long l) {
            int n = (int)(l >>> X_OFFSET & 3L);
            int n2 = (int)(l >>> 0 & 3L);
            int n3 = (int)(l >>> Z_OFFSET & 3L);
            return n << 4 | n3 << 2 | n2;
        }

        static long getFullKey(long l, int n) {
            l |= (long)(n >>> 4 & 3) << X_OFFSET;
            l |= (long)(n >>> 2 & 3) << Z_OFFSET;
            return l |= (long)(n >>> 0 & 3) << 0;
        }

        public boolean addBit(long l) {
            int n;
            long l2 = InternalMap.getOuterKey(l);
            int n2 = InternalMap.getInnerKey(l);
            long l3 = 1L << n2;
            if (l2 == 0L) {
                if (this.containsNullKey) {
                    return this.replaceBit(this.n, l3);
                }
                this.containsNullKey = true;
                n = this.n;
            } else {
                if (this.lastPos != -1 && l2 == this.lastOuterKey) {
                    return this.replaceBit(this.lastPos, l3);
                }
                long[] lArray = this.key;
                n = (int)HashCommon.mix((long)l2) & this.mask;
                long l4 = lArray[n];
                while (l4 != 0L) {
                    if (l4 == l2) {
                        this.lastPos = n;
                        this.lastOuterKey = l2;
                        return this.replaceBit(n, l3);
                    }
                    n = n + 1 & this.mask;
                    l4 = lArray[n];
                }
            }
            this.key[n] = l2;
            this.value[n] = l3;
            if (this.size == 0) {
                this.first = this.last = n;
                this.link[n] = -1L;
            } else {
                int n3 = this.last;
                this.link[n3] = this.link[n3] ^ (this.link[this.last] ^ (long)n & 0xFFFFFFFFL) & 0xFFFFFFFFL;
                this.link[n] = ((long)this.last & 0xFFFFFFFFL) << 32 | 0xFFFFFFFFL;
                this.last = n;
            }
            if (this.size++ >= this.maxFill) {
                this.rehash(HashCommon.arraySize((int)(this.size + 1), (float)this.f));
            }
            return false;
        }

        private boolean replaceBit(int n, long l) {
            boolean bl = (this.value[n] & l) != 0L;
            int n2 = n;
            this.value[n2] = this.value[n2] | l;
            return bl;
        }

        public boolean removeBit(long l) {
            long l2 = InternalMap.getOuterKey(l);
            int n = InternalMap.getInnerKey(l);
            long l3 = 1L << n;
            if (l2 == 0L) {
                if (this.containsNullKey) {
                    return this.removeFromNullEntry(l3);
                }
                return false;
            }
            if (this.lastPos != -1 && l2 == this.lastOuterKey) {
                return this.removeFromEntry(this.lastPos, l3);
            }
            long[] lArray = this.key;
            int n2 = (int)HashCommon.mix((long)l2) & this.mask;
            long l4 = lArray[n2];
            while (l4 != 0L) {
                if (l2 == l4) {
                    this.lastPos = n2;
                    this.lastOuterKey = l2;
                    return this.removeFromEntry(n2, l3);
                }
                n2 = n2 + 1 & this.mask;
                l4 = lArray[n2];
            }
            return false;
        }

        private boolean removeFromNullEntry(long l) {
            if ((this.value[this.n] & l) == 0L) {
                return false;
            }
            int n = this.n;
            this.value[n] = this.value[n] & (l ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[this.n] != 0L) {
                return true;
            }
            this.containsNullKey = false;
            --this.size;
            this.fixPointers(this.n);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        private boolean removeFromEntry(int n, long l) {
            if ((this.value[n] & l) == 0L) {
                return false;
            }
            int n2 = n;
            this.value[n2] = this.value[n2] & (l ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[n] != 0L) {
                return true;
            }
            this.lastPos = -1;
            --this.size;
            this.fixPointers(n);
            this.shiftKeys(n);
            if (this.size < this.maxFill / 4 && this.n > 16) {
                this.rehash(this.n / 2);
            }
            return true;
        }

        public long removeFirstBit() {
            if (this.size == 0) {
                throw new NoSuchElementException();
            }
            int n = this.first;
            long l = this.key[n];
            int n2 = Long.numberOfTrailingZeros(this.value[n]);
            int n3 = n;
            this.value[n3] = this.value[n3] & (1L << n2 ^ 0xFFFFFFFFFFFFFFFFL);
            if (this.value[n] == 0L) {
                this.removeFirstLong();
                this.lastPos = -1;
            }
            return InternalMap.getFullKey(l, n2);
        }

        protected void rehash(int n) {
            if (n > this.minSize) {
                super.rehash(n);
            }
        }

        static {
            Z_OFFSET = Y_BITS = 64 - X_BITS - Z_BITS;
            X_OFFSET = Y_BITS + Z_BITS;
            OUTER_MASK = 3L << X_OFFSET | 3L | 3L << Z_OFFSET;
        }
    }
}

