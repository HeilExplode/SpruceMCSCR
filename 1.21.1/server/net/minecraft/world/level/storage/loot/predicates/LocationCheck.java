/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.phys.Vec3;

public record LocationCheck(Optional<LocationPredicate> predicate, BlockPos offset) implements LootItemCondition
{
    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.optionalFieldOf("offsetX", (Object)0).forGetter(Vec3i::getX), (App)Codec.INT.optionalFieldOf("offsetY", (Object)0).forGetter(Vec3i::getY), (App)Codec.INT.optionalFieldOf("offsetZ", (Object)0).forGetter(Vec3i::getZ)).apply((Applicative)instance, BlockPos::new));
    public static final MapCodec<LocationCheck> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheck::predicate), (App)OFFSET_CODEC.forGetter(LocationCheck::offset)).apply((Applicative)instance, LocationCheck::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.LOCATION_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        return vec3 != null && (this.predicate.isEmpty() || this.predicate.get().matches(lootContext.getLevel(), vec3.x() + (double)this.offset.getX(), vec3.y() + (double)this.offset.getY(), vec3.z() + (double)this.offset.getZ()));
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
        return () -> new LocationCheck(Optional.of(builder.build()), BlockPos.ZERO);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder, BlockPos blockPos) {
        return () -> new LocationCheck(Optional.of(builder.build()), blockPos);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

