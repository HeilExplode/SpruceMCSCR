/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock
extends Block {
    public static final MapCodec<WebBlock> CODEC = WebBlock.simpleCodec(WebBlock::new);

    public MapCodec<WebBlock> codec() {
        return CODEC;
    }

    public WebBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        LivingEntity livingEntity;
        Vec3 vec3 = new Vec3(0.25, 0.05f, 0.25);
        if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).hasEffect(MobEffects.WEAVING)) {
            vec3 = new Vec3(0.5, 0.25, 0.5);
        }
        entity.makeStuckInBlock(blockState, vec3);
    }
}

