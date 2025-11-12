/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;

public record RaiderPredicate(boolean hasRaid, boolean isCaptain) implements EntitySubPredicate
{
    public static final MapCodec<RaiderPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("has_raid", (Object)false).forGetter(RaiderPredicate::hasRaid), (App)Codec.BOOL.optionalFieldOf("is_captain", (Object)false).forGetter(RaiderPredicate::isCaptain)).apply((Applicative)instance, RaiderPredicate::new));
    public static final RaiderPredicate CAPTAIN_WITHOUT_RAID = new RaiderPredicate(false, true);

    public MapCodec<RaiderPredicate> codec() {
        return EntitySubPredicates.RAIDER;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof Raider) {
            Raider raider = (Raider)entity;
            return raider.hasRaid() == this.hasRaid && raider.isCaptain() == this.isCaptain;
        }
        return false;
    }
}

