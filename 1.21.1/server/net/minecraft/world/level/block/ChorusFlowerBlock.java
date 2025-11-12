/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChorusFlowerBlock
extends Block {
    public static final MapCodec<ChorusFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter(chorusFlowerBlock -> chorusFlowerBlock.plant), ChorusFlowerBlock.propertiesCodec()).apply((Applicative)instance, ChorusFlowerBlock::new));
    public static final int DEAD_AGE = 5;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    protected static final VoxelShape BLOCK_SUPPORT_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
    private final Block plant;

    public MapCodec<ChorusFlowerBlock> codec() {
        return CODEC;
    }

    protected ChorusFlowerBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.plant = block;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGE) < 5;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return BLOCK_SUPPORT_SHAPE;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int n;
        int n2;
        BlockPos blockPos2 = blockPos.above();
        if (!serverLevel.isEmptyBlock(blockPos2) || blockPos2.getY() >= serverLevel.getMaxBuildHeight()) {
            return;
        }
        int n3 = blockState.getValue(AGE);
        if (n3 >= 5) {
            return;
        }
        boolean bl = false;
        boolean bl2 = false;
        BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
        if (blockState2.is(Blocks.END_STONE)) {
            bl = true;
        } else if (blockState2.is(this.plant)) {
            n2 = 1;
            for (n = 0; n < 4; ++n) {
                BlockState blockState3 = serverLevel.getBlockState(blockPos.below(n2 + 1));
                if (blockState3.is(this.plant)) {
                    ++n2;
                    continue;
                }
                if (!blockState3.is(Blocks.END_STONE)) break;
                bl2 = true;
                break;
            }
            if (n2 < 2 || n2 <= randomSource.nextInt(bl2 ? 5 : 4)) {
                bl = true;
            }
        } else if (blockState2.isAir()) {
            bl = true;
        }
        if (bl && ChorusFlowerBlock.allNeighborsEmpty(serverLevel, blockPos2, null) && serverLevel.isEmptyBlock(blockPos.above(2))) {
            serverLevel.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(serverLevel, blockPos, this.plant.defaultBlockState()), 2);
            this.placeGrownFlower(serverLevel, blockPos2, n3);
        } else if (n3 < 4) {
            n2 = randomSource.nextInt(4);
            if (bl2) {
                ++n2;
            }
            n = 0;
            for (int i = 0; i < n2; ++i) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
                BlockPos blockPos3 = blockPos.relative(direction);
                if (!serverLevel.isEmptyBlock(blockPos3) || !serverLevel.isEmptyBlock(blockPos3.below()) || !ChorusFlowerBlock.allNeighborsEmpty(serverLevel, blockPos3, direction.getOpposite())) continue;
                this.placeGrownFlower(serverLevel, blockPos3, n3 + 1);
                n = 1;
            }
            if (n != 0) {
                serverLevel.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(serverLevel, blockPos, this.plant.defaultBlockState()), 2);
            } else {
                this.placeDeadFlower(serverLevel, blockPos);
            }
        } else {
            this.placeDeadFlower(serverLevel, blockPos);
        }
    }

    private void placeGrownFlower(Level level, BlockPos blockPos, int n) {
        level.setBlock(blockPos, (BlockState)this.defaultBlockState().setValue(AGE, n), 2);
        level.levelEvent(1033, blockPos, 0);
    }

    private void placeDeadFlower(Level level, BlockPos blockPos) {
        level.setBlock(blockPos, (BlockState)this.defaultBlockState().setValue(AGE, 5), 2);
        level.levelEvent(1034, blockPos, 0);
    }

    private static boolean allNeighborsEmpty(LevelReader levelReader, BlockPos blockPos, @Nullable Direction direction) {
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            if (direction2 == direction || levelReader.isEmptyBlock(blockPos.relative(direction2))) continue;
            return false;
        }
        return true;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction != Direction.UP && !blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.scheduleTick(blockPos, this, 1);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.below());
        if (blockState2.is(this.plant) || blockState2.is(Blocks.END_STONE)) {
            return true;
        }
        if (!blockState2.isAir()) {
            return false;
        }
        boolean bl = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState blockState3 = levelReader.getBlockState(blockPos.relative(direction));
            if (blockState3.is(this.plant)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (blockState3.isAir()) continue;
            return false;
        }
        return bl;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public static void generatePlant(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, int n) {
        levelAccessor.setBlock(blockPos, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos, Blocks.CHORUS_PLANT.defaultBlockState()), 2);
        ChorusFlowerBlock.growTreeRecursive(levelAccessor, blockPos, randomSource, blockPos, n, 0);
    }

    private static void growTreeRecursive(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, BlockPos blockPos2, int n, int n2) {
        int n3;
        Block block = Blocks.CHORUS_PLANT;
        int n4 = randomSource.nextInt(4) + 1;
        if (n2 == 0) {
            ++n4;
        }
        for (n3 = 0; n3 < n4; ++n3) {
            BlockPos blockPos3 = blockPos.above(n3 + 1);
            if (!ChorusFlowerBlock.allNeighborsEmpty(levelAccessor, blockPos3, null)) {
                return;
            }
            levelAccessor.setBlock(blockPos3, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos3, block.defaultBlockState()), 2);
            levelAccessor.setBlock(blockPos3.below(), ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos3.below(), block.defaultBlockState()), 2);
        }
        n3 = 0;
        if (n2 < 4) {
            int n5 = randomSource.nextInt(4);
            if (n2 == 0) {
                ++n5;
            }
            for (int i = 0; i < n5; ++i) {
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
                BlockPos blockPos4 = blockPos.above(n4).relative(direction);
                if (Math.abs(blockPos4.getX() - blockPos2.getX()) >= n || Math.abs(blockPos4.getZ() - blockPos2.getZ()) >= n || !levelAccessor.isEmptyBlock(blockPos4) || !levelAccessor.isEmptyBlock(blockPos4.below()) || !ChorusFlowerBlock.allNeighborsEmpty(levelAccessor, blockPos4, direction.getOpposite())) continue;
                n3 = 1;
                levelAccessor.setBlock(blockPos4, ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos4, block.defaultBlockState()), 2);
                levelAccessor.setBlock(blockPos4.relative(direction.getOpposite()), ChorusPlantBlock.getStateWithConnections(levelAccessor, blockPos4.relative(direction.getOpposite()), block.defaultBlockState()), 2);
                ChorusFlowerBlock.growTreeRecursive(levelAccessor, blockPos4, randomSource, blockPos2, n, n2 + 1);
            }
        }
        if (n3 == 0) {
            levelAccessor.setBlock(blockPos.above(n4), (BlockState)Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, 5), 2);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockPos) && projectile.mayBreak(level)) {
            level.destroyBlock(blockPos, true, projectile);
        }
    }
}

