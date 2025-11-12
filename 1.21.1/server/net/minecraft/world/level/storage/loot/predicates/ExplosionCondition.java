/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class ExplosionCondition
implements LootItemCondition {
    private static final ExplosionCondition INSTANCE = new ExplosionCondition();
    public static final MapCodec<ExplosionCondition> CODEC = MapCodec.unit((Object)INSTANCE);

    private ExplosionCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.SURVIVES_EXPLOSION;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
    }

    @Override
    public boolean test(LootContext lootContext) {
        Float f = lootContext.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (f != null) {
            RandomSource randomSource = lootContext.getRandom();
            float f2 = 1.0f / f.floatValue();
            return randomSource.nextFloat() <= f2;
        }
        return true;
    }

    public static LootItemCondition.Builder survivesExplosion() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

