/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class EndGatewayBlock
extends BaseEntityBlock
implements Portal {
    public static final MapCodec<EndGatewayBlock> CODEC = EndGatewayBlock.simpleCodec(EndGatewayBlock::new);

    public MapCodec<EndGatewayBlock> codec() {
        return CODEC;
    }

    protected EndGatewayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TheEndGatewayBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return EndGatewayBlock.createTickerHelper(blockEntityType, BlockEntityType.END_GATEWAY, level.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::portalTick);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TheEndGatewayBlockEntity)) {
            return;
        }
        int n = ((TheEndGatewayBlockEntity)blockEntity).getParticleAmount();
        for (int i = 0; i < n; ++i) {
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double d2 = (double)blockPos.getY() + randomSource.nextDouble();
            double d3 = (double)blockPos.getZ() + randomSource.nextDouble();
            double d4 = (randomSource.nextDouble() - 0.5) * 0.5;
            double d5 = (randomSource.nextDouble() - 0.5) * 0.5;
            double d6 = (randomSource.nextDouble() - 0.5) * 0.5;
            int n2 = randomSource.nextInt(2) * 2 - 1;
            if (randomSource.nextBoolean()) {
                d3 = (double)blockPos.getZ() + 0.5 + 0.25 * (double)n2;
                d6 = randomSource.nextFloat() * 2.0f * (float)n2;
            } else {
                d = (double)blockPos.getX() + 0.5 + 0.25 * (double)n2;
                d4 = randomSource.nextFloat() * 2.0f * (float)n2;
            }
            level.addParticle(ParticleTypes.PORTAL, d, d2, d3, d4, d5, d6);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return false;
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (entity.canUsePortal(false)) {
            TheEndGatewayBlockEntity theEndGatewayBlockEntity;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!level.isClientSide && blockEntity instanceof TheEndGatewayBlockEntity && !(theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity).isCoolingDown()) {
                entity.setAsInsidePortal(this, blockPos);
                TheEndGatewayBlockEntity.triggerCooldown(level, blockPos, blockState, theEndGatewayBlockEntity);
            }
        }
    }

    @Override
    @Nullable
    public DimensionTransition getPortalDestination(ServerLevel serverLevel, Entity entity, BlockPos blockPos) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TheEndGatewayBlockEntity)) {
            return null;
        }
        TheEndGatewayBlockEntity theEndGatewayBlockEntity = (TheEndGatewayBlockEntity)blockEntity;
        Vec3 vec3 = theEndGatewayBlockEntity.getPortalPosition(serverLevel, blockPos);
        if (vec3 != null) {
            return new DimensionTransition(serverLevel, vec3, EndGatewayBlock.calculateExitMovement(entity), entity.getYRot(), entity.getXRot(), DimensionTransition.PLACE_PORTAL_TICKET);
        }
        return null;
    }

    private static Vec3 calculateExitMovement(Entity entity) {
        if (entity instanceof ThrownEnderpearl) {
            return new Vec3(0.0, -1.0, 0.0);
        }
        return entity.getDeltaMovement();
    }
}

