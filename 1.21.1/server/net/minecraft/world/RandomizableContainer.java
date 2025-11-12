/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface RandomizableContainer
extends Container {
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    @Nullable
    public ResourceKey<LootTable> getLootTable();

    public void setLootTable(@Nullable ResourceKey<LootTable> var1);

    default public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
        this.setLootTable(resourceKey);
        this.setLootTableSeed(l);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getBlockPos();

    @Nullable
    public Level getLevel();

    public static void setBlockEntityLootTable(BlockGetter blockGetter, RandomSource randomSource, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof RandomizableContainer) {
            RandomizableContainer randomizableContainer = (RandomizableContainer)((Object)blockEntity);
            randomizableContainer.setLootTable(resourceKey, randomSource.nextLong());
        }
    }

    default public boolean tryLoadLootTable(CompoundTag compoundTag) {
        if (compoundTag.contains(LOOT_TABLE_TAG, 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(compoundTag.getString(LOOT_TABLE_TAG))));
            if (compoundTag.contains(LOOT_TABLE_SEED_TAG, 4)) {
                this.setLootTableSeed(compoundTag.getLong(LOOT_TABLE_SEED_TAG));
            } else {
                this.setLootTableSeed(0L);
            }
            return true;
        }
        return false;
    }

    default public boolean trySaveLootTable(CompoundTag compoundTag) {
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey == null) {
            return false;
        }
        compoundTag.putString(LOOT_TABLE_TAG, resourceKey.location().toString());
        long l = this.getLootTableSeed();
        if (l != 0L) {
            compoundTag.putLong(LOOT_TABLE_SEED_TAG, l);
        }
        return true;
    }

    default public void unpackLootTable(@Nullable Player player) {
        Level level = this.getLevel();
        BlockPos blockPos = this.getBlockPos();
        ResourceKey<LootTable> resourceKey = this.getLootTable();
        if (resourceKey != null && level != null && level.getServer() != null) {
            LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(resourceKey);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, resourceKey);
            }
            this.setLootTable(null);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel)level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }
            lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }
    }
}

