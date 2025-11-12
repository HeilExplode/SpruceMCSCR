/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMaps
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.GameTypePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public record PlayerPredicate(MinMaxBounds.Ints level, GameTypePredicate gameType, List<StatMatcher<?>> stats, Object2BooleanMap<ResourceLocation> recipes, Map<ResourceLocation, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt) implements EntitySubPredicate
{
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("level", (Object)MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level), (App)GameTypePredicate.CODEC.optionalFieldOf("gamemode", (Object)GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType), (App)StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats), (App)ExtraCodecs.object2BooleanMap(ResourceLocation.CODEC).optionalFieldOf("recipes", (Object)Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), (App)Codec.unboundedMap(ResourceLocation.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(PlayerPredicate::advancements), (App)EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt)).apply((Applicative)instance, PlayerPredicate::new));

    @Override
    public boolean matches(Entity entity2, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        Object object;
        Object object22;
        if (!(entity2 instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity2;
        if (!this.level.matches(serverPlayer.experienceLevel)) {
            return false;
        }
        if (!this.gameType.matches(serverPlayer.gameMode.getGameModeForPlayer())) {
            return false;
        }
        ServerStatsCounter serverStatsCounter = serverPlayer.getStats();
        for (StatMatcher<?> object32 : this.stats) {
            if (object32.matches(serverStatsCounter)) continue;
            return false;
        }
        ServerRecipeBook serverRecipeBook = serverPlayer.getRecipeBook();
        for (Object object22 : this.recipes.object2BooleanEntrySet()) {
            if (serverRecipeBook.contains((ResourceLocation)object22.getKey()) == object22.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
            object22 = serverPlayer.getServer().getAdvancements();
            for (Map.Entry<ResourceLocation, AdvancementPredicate> entry : this.advancements.entrySet()) {
                object = ((ServerAdvancementManager)object22).get(entry.getKey());
                if (object != null && entry.getValue().test(playerAdvancements.getOrStartProgress((AdvancementHolder)object))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3 vec32 = serverPlayer.getEyePosition();
            object22 = serverPlayer.getViewVector(1.0f);
            Vec3 vec33 = vec32.add(((Vec3)object22).x * 100.0, ((Vec3)object22).y * 100.0, ((Vec3)object22).z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(serverPlayer.level(), serverPlayer, vec32, vec33, new AABB(vec32, vec33).inflate(1.0), entity -> !entity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            object = entityHitResult.getEntity();
            if (!this.lookingAt.get().matches(serverPlayer, (Entity)object) || !serverPlayer.hasLineOfSight((Entity)object)) {
                return false;
            }
        }
        return true;
    }

    public MapCodec<PlayerPredicate> codec() {
        return EntitySubPredicates.PLAYER;
    }

    record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE.byNameCodec().dispatch(StatMatcher::type, StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> statType, Holder<T> holder, MinMaxBounds.Ints ints) {
            this(statType, holder, ints, (Supplier<Stat<T>>)Suppliers.memoize(() -> statType.get(holder.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createTypedCodec(StatType<T> statType) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group((App)statType.getRegistry().holderByNameCodec().fieldOf("stat").forGetter(StatMatcher::value), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("value", (Object)MinMaxBounds.Ints.ANY).forGetter(StatMatcher::range)).apply((Applicative)instance, (holder, ints) -> new StatMatcher(statType, holder, (MinMaxBounds.Ints)ints)));
        }

        public boolean matches(StatsCounter statsCounter) {
            return this.range.matches(statsCounter.getValue(this.stat.get()));
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(AdvancementDonePredicate.CODEC, AdvancementCriterionsPredicate.CODEC).xmap(Either::unwrap, advancementPredicate -> {
            if (advancementPredicate instanceof AdvancementDonePredicate) {
                AdvancementDonePredicate advancementDonePredicate = (AdvancementDonePredicate)advancementPredicate;
                return Either.left((Object)advancementDonePredicate);
            }
            if (advancementPredicate instanceof AdvancementCriterionsPredicate) {
                AdvancementCriterionsPredicate advancementCriterionsPredicate = (AdvancementCriterionsPredicate)advancementPredicate;
                return Either.right((Object)advancementCriterionsPredicate);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private GameTypePredicate gameType = GameTypePredicate.ANY;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap();
        private final Map<ResourceLocation, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();

        public static Builder player() {
            return new Builder();
        }

        public Builder setLevel(MinMaxBounds.Ints ints) {
            this.level = ints;
            return this;
        }

        public <T> Builder addStat(StatType<T> statType, Holder.Reference<T> reference, MinMaxBounds.Ints ints) {
            this.stats.add(new StatMatcher<T>(statType, reference, ints));
            return this;
        }

        public Builder addRecipe(ResourceLocation resourceLocation, boolean bl) {
            this.recipes.put((Object)resourceLocation, bl);
            return this;
        }

        public Builder setGameType(GameTypePredicate gameTypePredicate) {
            this.gameType = gameTypePredicate;
            return this;
        }

        public Builder setLookingAt(EntityPredicate.Builder builder) {
            this.lookingAt = Optional.of(builder.build());
            return this;
        }

        public Builder checkAdvancementDone(ResourceLocation resourceLocation, boolean bl) {
            this.advancements.put(resourceLocation, new AdvancementDonePredicate(bl));
            return this;
        }

        public Builder checkAdvancementCriterions(ResourceLocation resourceLocation, Map<String, Boolean> map) {
            this.advancements.put(resourceLocation, new AdvancementCriterionsPredicate((Object2BooleanMap<String>)new Object2BooleanOpenHashMap(map)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, (List<StatMatcher<?>>)this.stats.build(), this.recipes, this.advancements, this.lookingAt);
        }
    }

    record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriterionsPredicate::new, AdvancementCriterionsPredicate::criterions);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isDone() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    record AdvancementDonePredicate(boolean state) implements AdvancementPredicate
    {
        public static final Codec<AdvancementDonePredicate> CODEC = Codec.BOOL.xmap(AdvancementDonePredicate::new, AdvancementDonePredicate::state);

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.state;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }
}

