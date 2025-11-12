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

public record MovementPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles speed, MinMaxBounds.Doubles horizontalSpeed, MinMaxBounds.Doubles verticalSpeed, MinMaxBounds.Doubles fallDistance) {
    public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed), (App)MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", (Object)MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)).apply((Applicative)instance, MovementPredicate::new));

    public static MovementPredicate speed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY);
    }

    public static MovementPredicate fallDistance(MinMaxBounds.Doubles doubles) {
        return new MovementPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles);
    }

    public boolean matches(double d, double d2, double d3, double d4) {
        if (!(this.x.matches(d) && this.y.matches(d2) && this.z.matches(d3))) {
            return false;
        }
        double d5 = Mth.lengthSquared(d, d2, d3);
        if (!this.speed.matchesSqr(d5)) {
            return false;
        }
        double d6 = Mth.lengthSquared(d, d3);
        if (!this.horizontalSpeed.matchesSqr(d6)) {
            return false;
        }
        double d7 = Math.abs(d2);
        if (!this.verticalSpeed.matches(d7)) {
            return false;
        }
        return this.fallDistance.matches(d4);
    }
}

