/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BrushableBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOOT_TABLE_TAG = "LootTable";
    private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    private static final String HIT_DIRECTION_TAG = "hit_direction";
    private static final String ITEM_TAG = "item";
    private static final int BRUSH_COOLDOWN_TICKS = 10;
    private static final int BRUSH_RESET_TICKS = 40;
    private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
    private int brushCount;
    private long brushCountResetsAtTick;
    private long coolDownEndsAtTick;
    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Direction hitDirection;
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    public BrushableBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BRUSHABLE_BLOCK, blockPos, blockState);
    }

    public boolean brush(long l, Player player, Direction direction) {
        if (this.hitDirection == null) {
            this.hitDirection = direction;
        }
        this.brushCountResetsAtTick = l + 40L;
        if (l < this.coolDownEndsAtTick || !(this.level instanceof ServerLevel)) {
            return false;
        }
        this.coolDownEndsAtTick = l + 10L;
        this.unpackLootTable(player);
        int n = this.getCompletionState();
        if (++this.brushCount >= 10) {
            this.brushingCompleted(player);
            return true;
        }
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
        int n2 = this.getCompletionState();
        if (n != n2) {
            BlockState blockState = this.getBlockState();
            BlockState blockState2 = (BlockState)blockState.setValue(BlockStateProperties.DUSTED, n2);
            this.level.setBlock(this.getBlockPos(), blockState2, 3);
        }
        return false;
    }

    public void unpackLootTable(Player player) {
        Object object;
        if (this.lootTable == null || this.level == null || this.level.isClientSide() || this.level.getServer() == null) {
            return;
        }
        LootTable lootTable = this.level.getServer().reloadableRegistries().getLootTable(this.lootTable);
        if (player instanceof ServerPlayer) {
            object = (ServerPlayer)player;
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)object, this.lootTable);
        }
        object = new LootParams.Builder((ServerLevel)this.level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.CHEST);
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems((LootParams)object, this.lootTableSeed);
        this.item = switch (objectArrayList.size()) {
            case 0 -> ItemStack.EMPTY;
            case 1 -> (ItemStack)objectArrayList.get(0);
            default -> {
                LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", (Object)this.lootTable.location(), (Object)objectArrayList.size());
                yield (ItemStack)objectArrayList.get(0);
            }
        };
        this.lootTable = null;
        this.setChanged();
    }

    private void brushingCompleted(Player player) {
        Block block;
        if (this.level == null || this.level.getServer() == null) {
            return;
        }
        this.dropContent(player);
        BlockState blockState = this.getBlockState();
        this.level.levelEvent(3008, this.getBlockPos(), Block.getId(blockState));
        Block block2 = this.getBlockState().getBlock();
        if (block2 instanceof BrushableBlock) {
            BrushableBlock brushableBlock = (BrushableBlock)block2;
            block = brushableBlock.getTurnsInto();
        } else {
            block = Blocks.AIR;
        }
        this.level.setBlock(this.worldPosition, block.defaultBlockState(), 3);
    }

    private void dropContent(Player player) {
        if (this.level == null || this.level.getServer() == null) {
            return;
        }
        this.unpackLootTable(player);
        if (!this.item.isEmpty()) {
            double d = EntityType.ITEM.getWidth();
            double d2 = 1.0 - d;
            double d3 = d / 2.0;
            Direction direction = Objects.requireNonNullElse(this.hitDirection, Direction.UP);
            BlockPos blockPos = this.worldPosition.relative(direction, 1);
            double d4 = (double)blockPos.getX() + 0.5 * d2 + d3;
            double d5 = (double)blockPos.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0f);
            double d6 = (double)blockPos.getZ() + 0.5 * d2 + d3;
            ItemEntity itemEntity = new ItemEntity(this.level, d4, d5, d6, this.item.split(this.level.random.nextInt(21) + 10));
            itemEntity.setDeltaMovement(Vec3.ZERO);
            this.level.addFreshEntity(itemEntity);
            this.item = ItemStack.EMPTY;
        }
    }

    public void checkReset() {
        if (this.level == null) {
            return;
        }
        if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
            int n = this.getCompletionState();
            this.brushCount = Math.max(0, this.brushCount - 2);
            int n2 = this.getCompletionState();
            if (n != n2) {
                this.level.setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(BlockStateProperties.DUSTED, n2), 3);
            }
            int n3 = 4;
            this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
        }
        if (this.brushCount == 0) {
            this.hitDirection = null;
            this.brushCountResetsAtTick = 0L;
            this.coolDownEndsAtTick = 0L;
        } else {
            this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
        }
    }

    private boolean tryLoadLootTable(CompoundTag compoundTag) {
        if (compoundTag.contains(LOOT_TABLE_TAG, 8)) {
            this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(compoundTag.getString(LOOT_TABLE_TAG)));
            this.lootTableSeed = compoundTag.getLong(LOOT_TABLE_SEED_TAG);
            return true;
        }
        return false;
    }

    private boolean trySaveLootTable(CompoundTag compoundTag) {
        if (this.lootTable == null) {
            return false;
        }
        compoundTag.putString(LOOT_TABLE_TAG, this.lootTable.location().toString());
        if (this.lootTableSeed != 0L) {
            compoundTag.putLong(LOOT_TABLE_SEED_TAG, this.lootTableSeed);
        }
        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag compoundTag = super.getUpdateTag(provider);
        if (this.hitDirection != null) {
            compoundTag.putInt(HIT_DIRECTION_TAG, this.hitDirection.ordinal());
        }
        if (!this.item.isEmpty()) {
            compoundTag.put(ITEM_TAG, this.item.save(provider));
        }
        return compoundTag;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.item = !this.tryLoadLootTable(compoundTag) && compoundTag.contains(ITEM_TAG) ? ItemStack.parse(provider, compoundTag.getCompound(ITEM_TAG)).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        if (compoundTag.contains(HIT_DIRECTION_TAG)) {
            this.hitDirection = Direction.values()[compoundTag.getInt(HIT_DIRECTION_TAG)];
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.trySaveLootTable(compoundTag) && !this.item.isEmpty()) {
            compoundTag.put(ITEM_TAG, this.item.save(provider));
        }
    }

    public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
        this.lootTable = resourceKey;
        this.lootTableSeed = l;
    }

    private int getCompletionState() {
        if (this.brushCount == 0) {
            return 0;
        }
        if (this.brushCount < 3) {
            return 1;
        }
        if (this.brushCount < 6) {
            return 2;
        }
        return 3;
    }

    @Nullable
    public Direction getHitDirection() {
        return this.hitDirection;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

