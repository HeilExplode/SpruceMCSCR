/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.phys.shapes;

import java.util.BitSet;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.IndexMerger;

public final class BitSetDiscreteVoxelShape
extends DiscreteVoxelShape {
    private final BitSet storage;
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public BitSetDiscreteVoxelShape(int n, int n2, int n3) {
        super(n, n2, n3);
        this.storage = new BitSet(n * n2 * n3);
        this.xMin = n;
        this.yMin = n2;
        this.zMin = n3;
    }

    public static BitSetDiscreteVoxelShape withFilledBounds(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(n, n2, n3);
        bitSetDiscreteVoxelShape.xMin = n4;
        bitSetDiscreteVoxelShape.yMin = n5;
        bitSetDiscreteVoxelShape.zMin = n6;
        bitSetDiscreteVoxelShape.xMax = n7;
        bitSetDiscreteVoxelShape.yMax = n8;
        bitSetDiscreteVoxelShape.zMax = n9;
        for (int i = n4; i < n7; ++i) {
            for (int j = n5; j < n8; ++j) {
                for (int k = n6; k < n9; ++k) {
                    bitSetDiscreteVoxelShape.fillUpdateBounds(i, j, k, false);
                }
            }
        }
        return bitSetDiscreteVoxelShape;
    }

    public BitSetDiscreteVoxelShape(DiscreteVoxelShape discreteVoxelShape) {
        super(discreteVoxelShape.xSize, discreteVoxelShape.ySize, discreteVoxelShape.zSize);
        if (discreteVoxelShape instanceof BitSetDiscreteVoxelShape) {
            this.storage = (BitSet)((BitSetDiscreteVoxelShape)discreteVoxelShape).storage.clone();
        } else {
            this.storage = new BitSet(this.xSize * this.ySize * this.zSize);
            for (int i = 0; i < this.xSize; ++i) {
                for (int j = 0; j < this.ySize; ++j) {
                    for (int k = 0; k < this.zSize; ++k) {
                        if (!discreteVoxelShape.isFull(i, j, k)) continue;
                        this.storage.set(this.getIndex(i, j, k));
                    }
                }
            }
        }
        this.xMin = discreteVoxelShape.firstFull(Direction.Axis.X);
        this.yMin = discreteVoxelShape.firstFull(Direction.Axis.Y);
        this.zMin = discreteVoxelShape.firstFull(Direction.Axis.Z);
        this.xMax = discreteVoxelShape.lastFull(Direction.Axis.X);
        this.yMax = discreteVoxelShape.lastFull(Direction.Axis.Y);
        this.zMax = discreteVoxelShape.lastFull(Direction.Axis.Z);
    }

    protected int getIndex(int n, int n2, int n3) {
        return (n * this.ySize + n2) * this.zSize + n3;
    }

    @Override
    public boolean isFull(int n, int n2, int n3) {
        return this.storage.get(this.getIndex(n, n2, n3));
    }

    private void fillUpdateBounds(int n, int n2, int n3, boolean bl) {
        this.storage.set(this.getIndex(n, n2, n3));
        if (bl) {
            this.xMin = Math.min(this.xMin, n);
            this.yMin = Math.min(this.yMin, n2);
            this.zMin = Math.min(this.zMin, n3);
            this.xMax = Math.max(this.xMax, n + 1);
            this.yMax = Math.max(this.yMax, n2 + 1);
            this.zMax = Math.max(this.zMax, n3 + 1);
        }
    }

    @Override
    public void fill(int n, int n2, int n3) {
        this.fillUpdateBounds(n, n2, n3, true);
    }

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Override
    public int firstFull(Direction.Axis axis) {
        return axis.choose(this.xMin, this.yMin, this.zMin);
    }

    @Override
    public int lastFull(Direction.Axis axis) {
        return axis.choose(this.xMax, this.yMax, this.zMax);
    }

    static BitSetDiscreteVoxelShape join(DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape discreteVoxelShape2, IndexMerger indexMerger, IndexMerger indexMerger2, IndexMerger indexMerger3, BooleanOp booleanOp) {
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(indexMerger.size() - 1, indexMerger2.size() - 1, indexMerger3.size() - 1);
        int[] nArray = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        indexMerger.forMergedIndexes((n, n2, n3) -> {
            boolean[] blArray = new boolean[]{false};
            indexMerger2.forMergedIndexes((n4, n5, n6) -> {
                boolean[] blArray2 = new boolean[]{false};
                indexMerger3.forMergedIndexes((n7, n8, n9) -> {
                    if (booleanOp.apply(discreteVoxelShape.isFullWide(n, n4, n7), discreteVoxelShape2.isFullWide(n2, n5, n8))) {
                        bitSetDiscreteVoxelShape.storage.set(bitSetDiscreteVoxelShape.getIndex(n3, n6, n9));
                        nArray[2] = Math.min(nArray[2], n9);
                        nArray[5] = Math.max(nArray[5], n9);
                        blArray[0] = true;
                    }
                    return true;
                });
                if (blArray2[0]) {
                    nArray[1] = Math.min(nArray[1], n6);
                    nArray[4] = Math.max(nArray[4], n6);
                    blArray[0] = true;
                }
                return true;
            });
            if (blArray[0]) {
                nArray[0] = Math.min(nArray[0], n3);
                nArray[3] = Math.max(nArray[3], n3);
            }
            return true;
        });
        bitSetDiscreteVoxelShape.xMin = nArray[0];
        bitSetDiscreteVoxelShape.yMin = nArray[1];
        bitSetDiscreteVoxelShape.zMin = nArray[2];
        bitSetDiscreteVoxelShape.xMax = nArray[3] + 1;
        bitSetDiscreteVoxelShape.yMax = nArray[4] + 1;
        bitSetDiscreteVoxelShape.zMax = nArray[5] + 1;
        return bitSetDiscreteVoxelShape;
    }

    protected static void forAllBoxes(DiscreteVoxelShape discreteVoxelShape, DiscreteVoxelShape.IntLineConsumer intLineConsumer, boolean bl) {
        BitSetDiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(discreteVoxelShape);
        for (int i = 0; i < bitSetDiscreteVoxelShape.ySize; ++i) {
            for (int j = 0; j < bitSetDiscreteVoxelShape.xSize; ++j) {
                int n = -1;
                for (int k = 0; k <= bitSetDiscreteVoxelShape.zSize; ++k) {
                    if (bitSetDiscreteVoxelShape.isFullWide(j, i, k)) {
                        if (bl) {
                            if (n != -1) continue;
                            n = k;
                            continue;
                        }
                        intLineConsumer.consume(j, i, k, j + 1, i + 1, k + 1);
                        continue;
                    }
                    if (n == -1) continue;
                    int n2 = j;
                    int n3 = i;
                    bitSetDiscreteVoxelShape.clearZStrip(n, k, j, i);
                    while (bitSetDiscreteVoxelShape.isZStripFull(n, k, n2 + 1, i)) {
                        bitSetDiscreteVoxelShape.clearZStrip(n, k, n2 + 1, i);
                        ++n2;
                    }
                    while (bitSetDiscreteVoxelShape.isXZRectangleFull(j, n2 + 1, n, k, n3 + 1)) {
                        for (int i2 = j; i2 <= n2; ++i2) {
                            bitSetDiscreteVoxelShape.clearZStrip(n, k, i2, n3 + 1);
                        }
                        ++n3;
                    }
                    intLineConsumer.consume(j, i, n, n2 + 1, n3 + 1, k);
                    n = -1;
                }
            }
        }
    }

    private boolean isZStripFull(int n, int n2, int n3, int n4) {
        if (n3 >= this.xSize || n4 >= this.ySize) {
            return false;
        }
        return this.storage.nextClearBit(this.getIndex(n3, n4, n)) >= this.getIndex(n3, n4, n2);
    }

    private boolean isXZRectangleFull(int n, int n2, int n3, int n4, int n5) {
        for (int i = n; i < n2; ++i) {
            if (this.isZStripFull(n3, n4, i, n5)) continue;
            return false;
        }
        return true;
    }

    private void clearZStrip(int n, int n2, int n3, int n4) {
        this.storage.clear(this.getIndex(n3, n4, n), this.getIndex(n3, n4, n2));
    }

    public boolean isInterior(int n, int n2, int n3) {
        boolean bl = n > 0 && n < this.xSize - 1 && n2 > 0 && n2 < this.ySize - 1 && n3 > 0 && n3 < this.zSize - 1;
        return bl && this.isFull(n, n2, n3) && this.isFull(n - 1, n2, n3) && this.isFull(n + 1, n2, n3) && this.isFull(n, n2 - 1, n3) && this.isFull(n, n2 + 1, n3) && this.isFull(n, n2, n3 - 1) && this.isFull(n, n2, n3 + 1);
    }
}

