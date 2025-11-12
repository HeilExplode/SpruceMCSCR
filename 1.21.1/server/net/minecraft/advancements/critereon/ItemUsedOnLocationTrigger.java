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
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
        ServerLevel serverLevel = serverPlayer.serverLevel();
        BlockState blockState = serverLevel.getBlockState(blockPos);
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, blockPos.getCenter()).withParameter(LootContextParams.THIS_ENTITY, serverPlayer).withParameter(LootContextParams.BLOCK_STATE, blockState).withParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location)).apply((Applicative)instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> placedBlock(Block block) {
            ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate)));
        }

        public static Criterion<TriggerInstance> placedBlock(LootItemCondition.Builder ... builderArray) {
            ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create((LootItemCondition[])Arrays.stream(builderArray).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new));
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate)));
        }

        private static TriggerInstance itemUsedOnLocation(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
            ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(LocationCheck.checkLocation(builder).build(), MatchTool.toolMatches(builder2).build());
            return new TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate));
        }

        public static Criterion<TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
            return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation(builder, builder2));
        }

        public static Criterion<TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
            return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation(builder, builder2));
        }

        public boolean matches(LootContext lootContext) {
            return this.location.isEmpty() || this.location.get().matches(lootContext);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
            this.location.ifPresent(contextAwarePredicate -> criterionValidator.validate((ContextAwarePredicate)contextAwarePredicate, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
        }
    }
}

