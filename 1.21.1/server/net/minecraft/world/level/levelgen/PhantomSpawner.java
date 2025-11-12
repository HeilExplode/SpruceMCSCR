/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PhantomSpawner
implements CustomSpawner {
    private int nextTick;

    @Override
    public int tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (!bl) {
            return 0;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOINSOMNIA)) {
            return 0;
        }
        RandomSource randomSource = serverLevel.random;
        --this.nextTick;
        if (this.nextTick > 0) {
            return 0;
        }
        this.nextTick += (60 + randomSource.nextInt(60)) * 20;
        if (serverLevel.getSkyDarken() < 5 && serverLevel.dimensionType().hasSkyLight()) {
            return 0;
        }
        int n = 0;
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            FluidState fluidState;
            BlockState blockState;
            BlockPos blockPos;
            DifficultyInstance difficultyInstance;
            if (serverPlayer.isSpectator()) continue;
            BlockPos blockPos2 = serverPlayer.blockPosition();
            if (serverLevel.dimensionType().hasSkyLight() && (blockPos2.getY() < serverLevel.getSeaLevel() || !serverLevel.canSeeSky(blockPos2)) || !(difficultyInstance = serverLevel.getCurrentDifficultyAt(blockPos2)).isHarderThan(randomSource.nextFloat() * 3.0f)) continue;
            ServerStatsCounter serverStatsCounter = serverPlayer.getStats();
            int n2 = Mth.clamp(serverStatsCounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            int n3 = 24000;
            if (randomSource.nextInt(n2) < 72000 || !NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos = blockPos2.above(20 + randomSource.nextInt(15)).east(-10 + randomSource.nextInt(21)).south(-10 + randomSource.nextInt(21)), blockState = serverLevel.getBlockState(blockPos), fluidState = serverLevel.getFluidState(blockPos), EntityType.PHANTOM)) continue;
            SpawnGroupData spawnGroupData = null;
            int n4 = 1 + randomSource.nextInt(difficultyInstance.getDifficulty().getId() + 1);
            for (int i = 0; i < n4; ++i) {
                Phantom phantom = EntityType.PHANTOM.create(serverLevel);
                if (phantom == null) continue;
                phantom.moveTo(blockPos, 0.0f, 0.0f);
                spawnGroupData = phantom.finalizeSpawn(serverLevel, difficultyInstance, MobSpawnType.NATURAL, spawnGroupData);
                serverLevel.addFreshEntityWithPassengers(phantom);
                ++n;
            }
        }
        return n;
    }
}

