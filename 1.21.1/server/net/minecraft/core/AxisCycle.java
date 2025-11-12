/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import net.minecraft.core.Direction;

public enum AxisCycle {
    NONE{

        @Override
        public int cycle(int n, int n2, int n3, Direction.Axis axis) {
            return axis.choose(n, n2, n3);
        }

        @Override
        public double cycle(double d, double d2, double d3, Direction.Axis axis) {
            return axis.choose(d, d2, d3);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return axis;
        }

        @Override
        public AxisCycle inverse() {
            return this;
        }
    }
    ,
    FORWARD{

        @Override
        public int cycle(int n, int n2, int n3, Direction.Axis axis) {
            return axis.choose(n3, n, n2);
        }

        @Override
        public double cycle(double d, double d2, double d3, Direction.Axis axis) {
            return axis.choose(d3, d, d2);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXIS_VALUES[Math.floorMod(axis.ordinal() + 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return BACKWARD;
        }
    }
    ,
    BACKWARD{

        @Override
        public int cycle(int n, int n2, int n3, Direction.Axis axis) {
            return axis.choose(n2, n3, n);
        }

        @Override
        public double cycle(double d, double d2, double d3, Direction.Axis axis) {
            return axis.choose(d2, d3, d);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXIS_VALUES[Math.floorMod(axis.ordinal() - 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return FORWARD;
        }
    };

    public static final Direction.Axis[] AXIS_VALUES;
    public static final AxisCycle[] VALUES;

    public abstract int cycle(int var1, int var2, int var3, Direction.Axis var4);

    public abstract double cycle(double var1, double var3, double var5, Direction.Axis var7);

    public abstract Direction.Axis cycle(Direction.Axis var1);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Direction.Axis axis, Direction.Axis axis2) {
        return VALUES[Math.floorMod(axis2.ordinal() - axis.ordinal(), 3)];
    }

    static {
        AXIS_VALUES = Direction.Axis.values();
        VALUES = AxisCycle.values();
    }
}

