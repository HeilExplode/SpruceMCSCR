/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Object2FloatMap
 *  it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ComposterBlock
extends Block
implements WorldlyContainerHolder {
    public static final MapCodec<ComposterBlock> CODEC = ComposterBlock.simpleCodec(ComposterBlock::new);
    public static final int READY = 8;
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 7;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
    public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap();
    private static final int AABB_SIDE_THICKNESS = 2;
    private static final VoxelShape OUTER_SHAPE = Shapes.block();
    private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], voxelShapeArray -> {
        for (int i = 0; i < 8; ++i) {
            voxelShapeArray[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0, Math.max(2, 1 + i * 2), 2.0, 14.0, 16.0, 14.0), BooleanOp.ONLY_FIRST);
        }
        voxelShapeArray[8] = voxelShapeArray[7];
    });

    public MapCodec<ComposterBlock> codec() {
        return CODEC;
    }

    public static void bootStrap() {
        COMPOSTABLES.defaultReturnValue(-1.0f);
        float f = 0.3f;
        float f2 = 0.5f;
        float f3 = 0.65f;
        float f4 = 0.85f;
        float f5 = 1.0f;
        ComposterBlock.add(0.3f, Items.JUNGLE_LEAVES);
        ComposterBlock.add(0.3f, Items.OAK_LEAVES);
        ComposterBlock.add(0.3f, Items.SPRUCE_LEAVES);
        ComposterBlock.add(0.3f, Items.DARK_OAK_LEAVES);
        ComposterBlock.add(0.3f, Items.ACACIA_LEAVES);
        ComposterBlock.add(0.3f, Items.CHERRY_LEAVES);
        ComposterBlock.add(0.3f, Items.BIRCH_LEAVES);
        ComposterBlock.add(0.3f, Items.AZALEA_LEAVES);
        ComposterBlock.add(0.3f, Items.MANGROVE_LEAVES);
        ComposterBlock.add(0.3f, Items.OAK_SAPLING);
        ComposterBlock.add(0.3f, Items.SPRUCE_SAPLING);
        ComposterBlock.add(0.3f, Items.BIRCH_SAPLING);
        ComposterBlock.add(0.3f, Items.JUNGLE_SAPLING);
        ComposterBlock.add(0.3f, Items.ACACIA_SAPLING);
        ComposterBlock.add(0.3f, Items.CHERRY_SAPLING);
        ComposterBlock.add(0.3f, Items.DARK_OAK_SAPLING);
        ComposterBlock.add(0.3f, Items.MANGROVE_PROPAGULE);
        ComposterBlock.add(0.3f, Items.BEETROOT_SEEDS);
        ComposterBlock.add(0.3f, Items.DRIED_KELP);
        ComposterBlock.add(0.3f, Items.SHORT_GRASS);
        ComposterBlock.add(0.3f, Items.KELP);
        ComposterBlock.add(0.3f, Items.MELON_SEEDS);
        ComposterBlock.add(0.3f, Items.PUMPKIN_SEEDS);
        ComposterBlock.add(0.3f, Items.SEAGRASS);
        ComposterBlock.add(0.3f, Items.SWEET_BERRIES);
        ComposterBlock.add(0.3f, Items.GLOW_BERRIES);
        ComposterBlock.add(0.3f, Items.WHEAT_SEEDS);
        ComposterBlock.add(0.3f, Items.MOSS_CARPET);
        ComposterBlock.add(0.3f, Items.PINK_PETALS);
        ComposterBlock.add(0.3f, Items.SMALL_DRIPLEAF);
        ComposterBlock.add(0.3f, Items.HANGING_ROOTS);
        ComposterBlock.add(0.3f, Items.MANGROVE_ROOTS);
        ComposterBlock.add(0.3f, Items.TORCHFLOWER_SEEDS);
        ComposterBlock.add(0.3f, Items.PITCHER_POD);
        ComposterBlock.add(0.5f, Items.DRIED_KELP_BLOCK);
        ComposterBlock.add(0.5f, Items.TALL_GRASS);
        ComposterBlock.add(0.5f, Items.FLOWERING_AZALEA_LEAVES);
        ComposterBlock.add(0.5f, Items.CACTUS);
        ComposterBlock.add(0.5f, Items.SUGAR_CANE);
        ComposterBlock.add(0.5f, Items.VINE);
        ComposterBlock.add(0.5f, Items.NETHER_SPROUTS);
        ComposterBlock.add(0.5f, Items.WEEPING_VINES);
        ComposterBlock.add(0.5f, Items.TWISTING_VINES);
        ComposterBlock.add(0.5f, Items.MELON_SLICE);
        ComposterBlock.add(0.5f, Items.GLOW_LICHEN);
        ComposterBlock.add(0.65f, Items.SEA_PICKLE);
        ComposterBlock.add(0.65f, Items.LILY_PAD);
        ComposterBlock.add(0.65f, Items.PUMPKIN);
        ComposterBlock.add(0.65f, Items.CARVED_PUMPKIN);
        ComposterBlock.add(0.65f, Items.MELON);
        ComposterBlock.add(0.65f, Items.APPLE);
        ComposterBlock.add(0.65f, Items.BEETROOT);
        ComposterBlock.add(0.65f, Items.CARROT);
        ComposterBlock.add(0.65f, Items.COCOA_BEANS);
        ComposterBlock.add(0.65f, Items.POTATO);
        ComposterBlock.add(0.65f, Items.WHEAT);
        ComposterBlock.add(0.65f, Items.BROWN_MUSHROOM);
        ComposterBlock.add(0.65f, Items.RED_MUSHROOM);
        ComposterBlock.add(0.65f, Items.MUSHROOM_STEM);
        ComposterBlock.add(0.65f, Items.CRIMSON_FUNGUS);
        ComposterBlock.add(0.65f, Items.WARPED_FUNGUS);
        ComposterBlock.add(0.65f, Items.NETHER_WART);
        ComposterBlock.add(0.65f, Items.CRIMSON_ROOTS);
        ComposterBlock.add(0.65f, Items.WARPED_ROOTS);
        ComposterBlock.add(0.65f, Items.SHROOMLIGHT);
        ComposterBlock.add(0.65f, Items.DANDELION);
        ComposterBlock.add(0.65f, Items.POPPY);
        ComposterBlock.add(0.65f, Items.BLUE_ORCHID);
        ComposterBlock.add(0.65f, Items.ALLIUM);
        ComposterBlock.add(0.65f, Items.AZURE_BLUET);
        ComposterBlock.add(0.65f, Items.RED_TULIP);
        ComposterBlock.add(0.65f, Items.ORANGE_TULIP);
        ComposterBlock.add(0.65f, Items.WHITE_TULIP);
        ComposterBlock.add(0.65f, Items.PINK_TULIP);
        ComposterBlock.add(0.65f, Items.OXEYE_DAISY);
        ComposterBlock.add(0.65f, Items.CORNFLOWER);
        ComposterBlock.add(0.65f, Items.LILY_OF_THE_VALLEY);
        ComposterBlock.add(0.65f, Items.WITHER_ROSE);
        ComposterBlock.add(0.65f, Items.FERN);
        ComposterBlock.add(0.65f, Items.SUNFLOWER);
        ComposterBlock.add(0.65f, Items.LILAC);
        ComposterBlock.add(0.65f, Items.ROSE_BUSH);
        ComposterBlock.add(0.65f, Items.PEONY);
        ComposterBlock.add(0.65f, Items.LARGE_FERN);
        ComposterBlock.add(0.65f, Items.SPORE_BLOSSOM);
        ComposterBlock.add(0.65f, Items.AZALEA);
        ComposterBlock.add(0.65f, Items.MOSS_BLOCK);
        ComposterBlock.add(0.65f, Items.BIG_DRIPLEAF);
        ComposterBlock.add(0.85f, Items.HAY_BLOCK);
        ComposterBlock.add(0.85f, Items.BROWN_MUSHROOM_BLOCK);
        ComposterBlock.add(0.85f, Items.RED_MUSHROOM_BLOCK);
        ComposterBlock.add(0.85f, Items.NETHER_WART_BLOCK);
        ComposterBlock.add(0.85f, Items.WARPED_WART_BLOCK);
        ComposterBlock.add(0.85f, Items.FLOWERING_AZALEA);
        ComposterBlock.add(0.85f, Items.BREAD);
        ComposterBlock.add(0.85f, Items.BAKED_POTATO);
        ComposterBlock.add(0.85f, Items.COOKIE);
        ComposterBlock.add(0.85f, Items.TORCHFLOWER);
        ComposterBlock.add(0.85f, Items.PITCHER_PLANT);
        ComposterBlock.add(1.0f, Items.CAKE);
        ComposterBlock.add(1.0f, Items.PUMPKIN_PIE);
    }

    private static void add(float f, ItemLike itemLike) {
        COMPOSTABLES.put((Object)itemLike.asItem(), f);
    }

    public ComposterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LEVEL, 0));
    }

    public static void handleFill(Level level, BlockPos blockPos, boolean bl) {
        BlockState blockState = level.getBlockState(blockPos);
        level.playLocalSound(blockPos, bl ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        double d = blockState.getShape(level, blockPos).max(Direction.Axis.Y, 0.5, 0.5) + 0.03125;
        double d2 = 0.13125f;
        double d3 = 0.7375f;
        RandomSource randomSource = level.getRandom();
        for (int i = 0; i < 10; ++i) {
            double d4 = randomSource.nextGaussian() * 0.02;
            double d5 = randomSource.nextGaussian() * 0.02;
            double d6 = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.COMPOSTER, (double)blockPos.getX() + (double)0.13125f + (double)0.7375f * (double)randomSource.nextFloat(), (double)blockPos.getY() + d + (double)randomSource.nextFloat() * (1.0 - d), (double)blockPos.getZ() + (double)0.13125f + (double)0.7375f * (double)randomSource.nextFloat(), d4, d5, d6);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[blockState.getValue(LEVEL)];
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return OUTER_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[0];
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getValue(LEVEL) == 7) {
            level.scheduleTick(blockPos, blockState.getBlock(), 20);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int n = blockState.getValue(LEVEL);
        if (n < 8 && COMPOSTABLES.containsKey((Object)itemStack.getItem())) {
            if (n < 7 && !level.isClientSide) {
                BlockState blockState2 = ComposterBlock.addItem(player, blockState, level, blockPos, itemStack);
                level.levelEvent(1500, blockPos, blockState != blockState2 ? 1 : 0);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                itemStack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        int n = blockState.getValue(LEVEL);
        if (n == 8) {
            ComposterBlock.extractProduce(player, blockState, level, blockPos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static BlockState insertItem(Entity entity, BlockState blockState, ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        int n = blockState.getValue(LEVEL);
        if (n < 7 && COMPOSTABLES.containsKey((Object)itemStack.getItem())) {
            BlockState blockState2 = ComposterBlock.addItem(entity, blockState, serverLevel, blockPos, itemStack);
            itemStack.shrink(1);
            return blockState2;
        }
        return blockState;
    }

    public static BlockState extractProduce(Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        Object object;
        if (!level.isClientSide) {
            object = Vec3.atLowerCornerWithOffset(blockPos, 0.5, 1.01, 0.5).offsetRandom(level.random, 0.7f);
            ItemEntity itemEntity = new ItemEntity(level, ((Vec3)object).x(), ((Vec3)object).y(), ((Vec3)object).z(), new ItemStack(Items.BONE_MEAL));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        object = ComposterBlock.empty(entity, blockState, level, blockPos);
        level.playSound(null, blockPos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        return object;
    }

    static BlockState empty(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = (BlockState)blockState.setValue(LEVEL, 0);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
        return blockState2;
    }

    static BlockState addItem(@Nullable Entity entity, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
        int n = blockState.getValue(LEVEL);
        float f = COMPOSTABLES.getFloat((Object)itemStack.getItem());
        if (n == 0 && f > 0.0f || levelAccessor.getRandom().nextDouble() < (double)f) {
            int n2 = n + 1;
            BlockState blockState2 = (BlockState)blockState.setValue(LEVEL, n2);
            levelAccessor.setBlock(blockPos, blockState2, 3);
            levelAccessor.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
            if (n2 == 7) {
                levelAccessor.scheduleTick(blockPos, blockState.getBlock(), 20);
            }
            return blockState2;
        }
        return blockState;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LEVEL) == 7) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.cycle(LEVEL), 3);
            serverLevel.playSound(null, blockPos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return blockState.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int n = blockState.getValue(LEVEL);
        if (n == 8) {
            return new OutputContainer(blockState, levelAccessor, blockPos, new ItemStack(Items.BONE_MEAL));
        }
        if (n < 7) {
            return new InputContainer(blockState, levelAccessor, blockPos);
        }
        return new EmptyContainer();
    }

    static class OutputContainer
    extends SimpleContainer
    implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public OutputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
            super(itemStack);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            int[] nArray;
            if (direction == Direction.DOWN) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
            return !this.changed && direction == Direction.DOWN && itemStack.is(Items.BONE_MEAL);
        }

        @Override
        public void setChanged() {
            ComposterBlock.empty(null, this.state, this.level, this.pos);
            this.changed = true;
        }
    }

    static class InputContainer
    extends SimpleContainer
    implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public InputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
            super(1);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            int[] nArray;
            if (direction == Direction.UP) {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 0;
            } else {
                nArray = new int[]{};
            }
            return nArray;
        }

        @Override
        public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
            return !this.changed && direction == Direction.UP && COMPOSTABLES.containsKey((Object)itemStack.getItem());
        }

        @Override
        public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
            return false;
        }

        @Override
        public void setChanged() {
            ItemStack itemStack = this.getItem(0);
            if (!itemStack.isEmpty()) {
                this.changed = true;
                BlockState blockState = ComposterBlock.addItem(null, this.state, this.level, this.pos, itemStack);
                this.level.levelEvent(1500, this.pos, blockState != this.state ? 1 : 0);
                this.removeItemNoUpdate(0);
            }
        }
    }

    static class EmptyContainer
    extends SimpleContainer
    implements WorldlyContainer {
        public EmptyContainer() {
            super(0);
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override
        public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
            return false;
        }
    }
}

