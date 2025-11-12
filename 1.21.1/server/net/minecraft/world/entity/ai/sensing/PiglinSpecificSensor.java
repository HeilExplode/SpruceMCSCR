/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, (Object[])new MemoryModuleType[]{MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT});
    }

    @Override
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity2) {
        AbstractPiglin abstractPiglin;
        LivingEntity livingEntity3;
        Brain<?> brain = livingEntity2.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, PiglinSpecificSensor.findNearestRepellent(serverLevel, livingEntity2));
        Optional<Object> optional = Optional.empty();
        Optional<Object> optional2 = Optional.empty();
        Optional<Object> optional3 = Optional.empty();
        Optional<Object> optional4 = Optional.empty();
        Optional<Object> optional5 = Optional.empty();
        Optional<Object> optional6 = Optional.empty();
        Optional<Object> optional7 = Optional.empty();
        int n = 0;
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        NearestVisibleLivingEntities nearestVisibleLivingEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for (LivingEntity object2 : nearestVisibleLivingEntities.findAll(livingEntity -> true)) {
            if (object2 instanceof Hoglin) {
                livingEntity3 = (Hoglin)object2;
                if (((AgeableMob)livingEntity3).isBaby() && optional3.isEmpty()) {
                    optional3 = Optional.of(livingEntity3);
                    continue;
                }
                if (!((Hoglin)livingEntity3).isAdult()) continue;
                ++n;
                if (!optional2.isEmpty() || !((Hoglin)livingEntity3).canBeHunted()) continue;
                optional2 = Optional.of(livingEntity3);
                continue;
            }
            if (object2 instanceof PiglinBrute) {
                abstractPiglin = (PiglinBrute)object2;
                arrayList.add(abstractPiglin);
                continue;
            }
            if (object2 instanceof Piglin) {
                Piglin piglin = (Piglin)object2;
                if (piglin.isBaby() && optional4.isEmpty()) {
                    optional4 = Optional.of(piglin);
                    continue;
                }
                if (!piglin.isAdult()) continue;
                arrayList.add(piglin);
                continue;
            }
            if (object2 instanceof Player) {
                Player player = (Player)object2;
                if (optional6.isEmpty() && !PiglinAi.isWearingGold(player) && livingEntity2.canAttack(object2)) {
                    optional6 = Optional.of(player);
                }
                if (!optional7.isEmpty() || player.isSpectator() || !PiglinAi.isPlayerHoldingLovedItem(player)) continue;
                optional7 = Optional.of(player);
                continue;
            }
            if (optional.isEmpty() && (object2 instanceof WitherSkeleton || object2 instanceof WitherBoss)) {
                optional = Optional.of((Mob)object2);
                continue;
            }
            if (!optional5.isEmpty() || !PiglinAi.isZombified(object2.getType())) continue;
            optional5 = Optional.of(object2);
        }
        List<LivingEntity> list = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse((List<LivingEntity>)ImmutableList.of());
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            livingEntity3 = (LivingEntity)iterator.next();
            if (!(livingEntity3 instanceof AbstractPiglin) || !(abstractPiglin = (AbstractPiglin)livingEntity3).isAdult()) continue;
            arrayList2.add(abstractPiglin);
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional2);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional3);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional5);
        brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional6);
        brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional7);
        brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, arrayList2);
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, arrayList);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, arrayList.size());
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, n);
    }

    private static Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, LivingEntity livingEntity) {
        return BlockPos.findClosestMatch(livingEntity.blockPosition(), 8, 4, blockPos -> PiglinSpecificSensor.isValidRepellent(serverLevel, blockPos));
    }

    private static boolean isValidRepellent(ServerLevel serverLevel, BlockPos blockPos) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        boolean bl = blockState.is(BlockTags.PIGLIN_REPELLENTS);
        if (bl && blockState.is(Blocks.SOUL_CAMPFIRE)) {
            return CampfireBlock.isLitCampfire(blockState);
        }
        return bl;
    }
}

