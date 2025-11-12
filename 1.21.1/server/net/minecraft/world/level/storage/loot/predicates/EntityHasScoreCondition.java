/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

public record EntityHasScoreCondition(Map<String, IntRange> scores, LootContext.EntityTarget entityTarget) implements LootItemCondition
{
    public static final MapCodec<EntityHasScoreCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap((Codec)Codec.STRING, IntRange.CODEC).fieldOf("scores").forGetter(EntityHasScoreCondition::scores), (App)LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(EntityHasScoreCondition::entityTarget)).apply((Applicative)instance, EntityHasScoreCondition::new));

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return (Set)Stream.concat(Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap(intRange -> intRange.getReferencedContextParams().stream())).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        if (entity == null) {
            return false;
        }
        ServerScoreboard serverScoreboard = lootContext.getLevel().getScoreboard();
        for (Map.Entry<String, IntRange> entry : this.scores.entrySet()) {
            if (this.hasScore(lootContext, entity, serverScoreboard, entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    protected boolean hasScore(LootContext lootContext, Entity entity, Scoreboard scoreboard, String string, IntRange intRange) {
        Objective objective = scoreboard.getObjective(string);
        if (objective == null) {
            return false;
        }
        ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(entity, objective);
        if (readOnlyScoreInfo == null) {
            return false;
        }
        return intRange.test(lootContext, readOnlyScoreInfo.value());
    }

    public static Builder hasScores(LootContext.EntityTarget entityTarget) {
        return new Builder(entityTarget);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private final ImmutableMap.Builder<String, IntRange> scores = ImmutableMap.builder();
        private final LootContext.EntityTarget entityTarget;

        public Builder(LootContext.EntityTarget entityTarget) {
            this.entityTarget = entityTarget;
        }

        public Builder withScore(String string, IntRange intRange) {
            this.scores.put((Object)string, (Object)intRange);
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new EntityHasScoreCondition((Map<String, IntRange>)this.scores.build(), this.entityTarget);
        }
    }
}

