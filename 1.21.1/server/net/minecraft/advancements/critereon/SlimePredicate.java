/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public record SlimePredicate(MinMaxBounds.Ints size) implements EntitySubPredicate
{
    public static final MapCodec<SlimePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("size", (Object)MinMaxBounds.Ints.ANY).forGetter(SlimePredicate::size)).apply((Applicative)instance, SlimePredicate::new));

    public static SlimePredicate sized(MinMaxBounds.Ints ints) {
        return new SlimePredicate(ints);
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof Slime) {
            Slime slime = (Slime)entity;
            return this.size.matches(slime.getSize());
        }
        return false;
    }

    public MapCodec<SlimePredicate> codec() {
        return EntitySubPredicates.SLIME;
    }
}

