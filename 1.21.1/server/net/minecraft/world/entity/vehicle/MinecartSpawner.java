/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MinecartSpawner
extends AbstractMinecart {
    private final BaseSpawner spawner = new BaseSpawner(){

        @Override
        public void broadcastEvent(Level level, BlockPos blockPos, int n) {
            level.broadcastEntityEvent(MinecartSpawner.this, (byte)n);
        }
    };
    private final Runnable ticker;

    public MinecartSpawner(EntityType<? extends MinecartSpawner> entityType, Level level) {
        super(entityType, level);
        this.ticker = this.createTicker(level);
    }

    public MinecartSpawner(Level level, double d, double d2, double d3) {
        super(EntityType.SPAWNER_MINECART, level, d, d2, d3);
        this.ticker = this.createTicker(level);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    private Runnable createTicker(Level level) {
        return level instanceof ServerLevel ? () -> this.spawner.serverTick((ServerLevel)level, this.blockPosition()) : () -> this.spawner.clientTick(level, this.blockPosition());
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.SPAWNER;
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.SPAWNER.defaultBlockState();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spawner.load(this.level(), this.blockPosition(), compoundTag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.spawner.save(compoundTag);
    }

    @Override
    public void handleEntityEvent(byte by) {
        this.spawner.onEventTriggered(this.level(), by);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticker.run();
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }
}

