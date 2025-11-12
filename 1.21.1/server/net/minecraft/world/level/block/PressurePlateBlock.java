/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock
extends BasePressurePlateBlock {
    public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockSetType.CODEC.fieldOf("block_set_type").forGetter(pressurePlateBlock -> pressurePlateBlock.type), PressurePlateBlock.propertiesCodec()).apply((Applicative)instance, PressurePlateBlock::new));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MapCodec<PressurePlateBlock> codec() {
        return CODEC;
    }

    protected PressurePlateBlock(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
        super(properties, blockSetType);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWERED, false));
    }

    @Override
    protected int getSignalForState(BlockState blockState) {
        return blockState.getValue(POWERED) != false ? 15 : 0;
    }

    @Override
    protected BlockState setSignalForState(BlockState blockState, int n) {
        return (BlockState)blockState.setValue(POWERED, n > 0);
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos blockPos) {
        Class<Entity> clazz = switch (this.type.pressurePlateSensitivity()) {
            default -> throw new MatchException(null, null);
            case BlockSetType.PressurePlateSensitivity.EVERYTHING -> Entity.class;
            case BlockSetType.PressurePlateSensitivity.MOBS -> LivingEntity.class;
        };
        return PressurePlateBlock.getEntityCount(level, TOUCH_AABB.move(blockPos), clazz) > 0 ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}

