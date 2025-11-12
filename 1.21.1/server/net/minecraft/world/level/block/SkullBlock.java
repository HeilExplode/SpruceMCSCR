/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SkullBlock
extends AbstractSkullBlock {
    public static final MapCodec<SkullBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Type.CODEC.fieldOf("kind").forGetter(AbstractSkullBlock::getType), SkullBlock.propertiesCodec()).apply((Applicative)instance, SkullBlock::new));
    public static final int MAX = RotationSegment.getMaxSegmentIndex();
    private static final int ROTATIONS = MAX + 1;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    protected static final VoxelShape PIGLIN_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);

    public MapCodec<? extends SkullBlock> codec() {
        return CODEC;
    }

    protected SkullBlock(Type type, BlockBehaviour.Properties properties) {
        super(type, properties);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(ROTATION, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (this.getType() == Types.PIGLIN) {
            return PIGLIN_SHAPE;
        }
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)super.getStateForPlacement(blockPlaceContext).setValue(ROTATION, RotationSegment.convertToSegment(blockPlaceContext.getRotation()));
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(ROTATION, rotation.rotate(blockState.getValue(ROTATION), ROTATIONS));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)blockState.setValue(ROTATION, mirror.mirror(blockState.getValue(ROTATION), ROTATIONS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION);
    }

    public static interface Type
    extends StringRepresentable {
        public static final Map<String, Type> TYPES = new Object2ObjectArrayMap();
        public static final Codec<Type> CODEC = Codec.stringResolver(StringRepresentable::getSerializedName, TYPES::get);
    }

    public static enum Types implements Type
    {
        SKELETON("skeleton"),
        WITHER_SKELETON("wither_skeleton"),
        PLAYER("player"),
        ZOMBIE("zombie"),
        CREEPER("creeper"),
        PIGLIN("piglin"),
        DRAGON("dragon");

        private final String name;

        private Types(String string2) {
            this.name = string2;
            TYPES.put(string2, this);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}

