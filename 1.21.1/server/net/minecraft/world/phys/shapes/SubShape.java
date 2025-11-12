/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public final class SubShape
extends DiscreteVoxelShape {
    private final DiscreteVoxelShape parent;
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;

    protected SubShape(DiscreteVoxelShape discreteVoxelShape, int n, int n2, int n3, int n4, int n5, int n6) {
        super(n4 - n, n5 - n2, n6 - n3);
        this.parent = discreteVoxelShape;
        this.startX = n;
        this.startY = n2;
        this.startZ = n3;
        this.endX = n4;
        this.endY = n5;
        this.endZ = n6;
    }

    @Override
    public boolean isFull(int n, int n2, int n3) {
        return this.parent.isFull(this.startX + n, this.startY + n2, this.startZ + n3);
    }

    @Override
    public void fill(int n, int n2, int n3) {
        this.parent.fill(this.startX + n, this.startY + n2, this.startZ + n3);
    }

    @Override
    public int firstFull(Direction.Axis axis) {
        return this.clampToShape(axis, this.parent.firstFull(axis));
    }

    @Override
    public int lastFull(Direction.Axis axis) {
        return this.clampToShape(axis, this.parent.lastFull(axis));
    }

    private int clampToShape(Direction.Axis axis, int n) {
        int n2 = axis.choose(this.startX, this.startY, this.startZ);
        int n3 = axis.choose(this.endX, this.endY, this.endZ);
        return Mth.clamp(n, n2, n3) - n2;
    }
}

