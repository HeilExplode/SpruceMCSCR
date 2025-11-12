/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.Mth;

public record DistancePredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles horizontal, MinMaxBounds.Doubles absolute) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::horizontal), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("absolute", (Object)MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::absolute)).apply((Applicative)instance, DistancePredicate::new));

    public static DistancePredicate horizontal(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles);
    }

    public boolean matches(double d, double d2, double d3, double d4, double d5, double d6) {
        float f = (float)(d - d4);
        float f2 = (float)(d2 - d5);
        float f3 = (float)(d3 - d6);
        if (!(this.x.matches(Mth.abs(f)) && this.y.matches(Mth.abs(f2)) && this.z.matches(Mth.abs(f3)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr(f * f + f3 * f3)) {
            return false;
        }
        return this.absolute.matchesSqr(f * f + f2 * f2 + f3 * f3);
    }
}

