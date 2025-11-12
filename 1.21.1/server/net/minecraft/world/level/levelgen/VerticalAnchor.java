/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public interface VerticalAnchor {
    public static final Codec<VerticalAnchor> CODEC = Codec.xor(Absolute.CODEC, (Codec)Codec.xor(AboveBottom.CODEC, BelowTop.CODEC)).xmap(VerticalAnchor::merge, VerticalAnchor::split);
    public static final VerticalAnchor BOTTOM = VerticalAnchor.aboveBottom(0);
    public static final VerticalAnchor TOP = VerticalAnchor.belowTop(0);

    public static VerticalAnchor absolute(int n) {
        return new Absolute(n);
    }

    public static VerticalAnchor aboveBottom(int n) {
        return new AboveBottom(n);
    }

    public static VerticalAnchor belowTop(int n) {
        return new BelowTop(n);
    }

    public static VerticalAnchor bottom() {
        return BOTTOM;
    }

    public static VerticalAnchor top() {
        return TOP;
    }

    private static VerticalAnchor merge(Either<Absolute, Either<AboveBottom, BelowTop>> either) {
        return (VerticalAnchor)either.map(Function.identity(), Either::unwrap);
    }

    private static Either<Absolute, Either<AboveBottom, BelowTop>> split(VerticalAnchor verticalAnchor) {
        if (verticalAnchor instanceof Absolute) {
            return Either.left((Object)((Absolute)verticalAnchor));
        }
        return Either.right((Object)(verticalAnchor instanceof AboveBottom ? Either.left((Object)((AboveBottom)verticalAnchor)) : Either.right((Object)((BelowTop)verticalAnchor))));
    }

    public int resolveY(WorldGenerationContext var1);

    public record Absolute(int y) implements VerticalAnchor
    {
        public static final Codec<Absolute> CODEC = Codec.intRange((int)DimensionType.MIN_Y, (int)DimensionType.MAX_Y).fieldOf("absolute").xmap(Absolute::new, Absolute::y).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return this.y;
        }

        @Override
        public String toString() {
            return this.y + " absolute";
        }
    }

    public record AboveBottom(int offset) implements VerticalAnchor
    {
        public static final Codec<AboveBottom> CODEC = Codec.intRange((int)DimensionType.MIN_Y, (int)DimensionType.MAX_Y).fieldOf("above_bottom").xmap(AboveBottom::new, AboveBottom::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getMinGenY() + this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " above bottom";
        }
    }

    public record BelowTop(int offset) implements VerticalAnchor
    {
        public static final Codec<BelowTop> CODEC = Codec.intRange((int)DimensionType.MIN_Y, (int)DimensionType.MAX_Y).fieldOf("below_top").xmap(BelowTop::new, BelowTop::offset).codec();

        @Override
        public int resolveY(WorldGenerationContext worldGenerationContext) {
            return worldGenerationContext.getGenDepth() - 1 + worldGenerationContext.getMinGenY() - this.offset;
        }

        @Override
        public String toString() {
            return this.offset + " below top";
        }
    }
}

