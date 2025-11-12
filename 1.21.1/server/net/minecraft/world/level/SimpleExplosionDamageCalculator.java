/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SimpleExplosionDamageCalculator
extends ExplosionDamageCalculator {
    private final boolean explodesBlocks;
    private final boolean damagesEntities;
    private final Optional<Float> knockbackMultiplier;
    private final Optional<HolderSet<Block>> immuneBlocks;

    public SimpleExplosionDamageCalculator(boolean bl, boolean bl2, Optional<Float> optional, Optional<HolderSet<Block>> optional2) {
        this.explodesBlocks = bl;
        this.damagesEntities = bl2;
        this.knockbackMultiplier = optional;
        this.immuneBlocks = optional2;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (this.immuneBlocks.isPresent()) {
            if (blockState.is(this.immuneBlocks.get())) {
                return Optional.of(Float.valueOf(3600000.0f));
            }
            return Optional.empty();
        }
        return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        return this.explodesBlocks;
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        return this.damagesEntities;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public float getKnockbackMultiplier(Entity entity) {
        boolean bl;
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getAbilities().flying) {
                return 0.0f;
            }
        }
        boolean bl2 = bl = false;
        if (bl) {
            return 0.0f;
        }
        float f = this.knockbackMultiplier.orElseGet(() -> Float.valueOf(super.getKnockbackMultiplier(entity))).floatValue();
        return f;
    }
}

