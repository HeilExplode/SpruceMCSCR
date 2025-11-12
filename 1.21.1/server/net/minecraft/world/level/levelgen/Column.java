/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Column {
    public static Range around(int n, int n2) {
        return new Range(n - 1, n2 + 1);
    }

    public static Range inside(int n, int n2) {
        return new Range(n, n2);
    }

    public static Column below(int n) {
        return new Ray(n, false);
    }

    public static Column fromHighest(int n) {
        return new Ray(n + 1, false);
    }

    public static Column above(int n) {
        return new Ray(n, true);
    }

    public static Column fromLowest(int n) {
        return new Ray(n - 1, true);
    }

    public static Column line() {
        return Line.INSTANCE;
    }

    public static Column create(OptionalInt optionalInt, OptionalInt optionalInt2) {
        if (optionalInt.isPresent() && optionalInt2.isPresent()) {
            return Column.inside(optionalInt.getAsInt(), optionalInt2.getAsInt());
        }
        if (optionalInt.isPresent()) {
            return Column.above(optionalInt.getAsInt());
        }
        if (optionalInt2.isPresent()) {
            return Column.below(optionalInt2.getAsInt());
        }
        return Column.line();
    }

    public abstract OptionalInt getCeiling();

    public abstract OptionalInt getFloor();

    public abstract OptionalInt getHeight();

    public Column withFloor(OptionalInt optionalInt) {
        return Column.create(optionalInt, this.getCeiling());
    }

    public Column withCeiling(OptionalInt optionalInt) {
        return Column.create(this.getFloor(), optionalInt);
    }

    public static Optional<Column> scan(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos, int n, Predicate<BlockState> predicate, Predicate<BlockState> predicate2) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        if (!levelSimulatedReader.isStateAtPosition(blockPos, predicate)) {
            return Optional.empty();
        }
        int n2 = blockPos.getY();
        OptionalInt optionalInt = Column.scanDirection(levelSimulatedReader, n, predicate, predicate2, mutableBlockPos, n2, Direction.UP);
        OptionalInt optionalInt2 = Column.scanDirection(levelSimulatedReader, n, predicate, predicate2, mutableBlockPos, n2, Direction.DOWN);
        return Optional.of(Column.create(optionalInt2, optionalInt));
    }

    private static OptionalInt scanDirection(LevelSimulatedReader levelSimulatedReader, int n, Predicate<BlockState> predicate, Predicate<BlockState> predicate2, BlockPos.MutableBlockPos mutableBlockPos, int n2, Direction direction) {
        mutableBlockPos.setY(n2);
        for (int i = 1; i < n && levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate); ++i) {
            mutableBlockPos.move(direction);
        }
        return levelSimulatedReader.isStateAtPosition(mutableBlockPos, predicate2) ? OptionalInt.of(mutableBlockPos.getY()) : OptionalInt.empty();
    }

    public static final class Range
    extends Column {
        private final int floor;
        private final int ceiling;

        protected Range(int n, int n2) {
            this.floor = n;
            this.ceiling = n2;
            if (this.height() < 0) {
                throw new IllegalArgumentException("Column of negative height: " + String.valueOf(this));
            }
        }

        @Override
        public OptionalInt getCeiling() {
            return OptionalInt.of(this.ceiling);
        }

        @Override
        public OptionalInt getFloor() {
            return OptionalInt.of(this.floor);
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.of(this.height());
        }

        public int ceiling() {
            return this.ceiling;
        }

        public int floor() {
            return this.floor;
        }

        public int height() {
            return this.ceiling - this.floor - 1;
        }

        public String toString() {
            return "C(" + this.ceiling + "-" + this.floor + ")";
        }
    }

    public static final class Ray
    extends Column {
        private final int edge;
        private final boolean pointingUp;

        public Ray(int n, boolean bl) {
            this.edge = n;
            this.pointingUp = bl;
        }

        @Override
        public OptionalInt getCeiling() {
            return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
        }

        @Override
        public OptionalInt getFloor() {
            return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.empty();
        }

        public String toString() {
            return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
        }
    }

    public static final class Line
    extends Column {
        static final Line INSTANCE = new Line();

        private Line() {
        }

        @Override
        public OptionalInt getCeiling() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getFloor() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.empty();
        }

        public String toString() {
            return "C(-)";
        }
    }
}

