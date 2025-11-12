/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock
extends BaseEntityBlock {
    public static final MapCodec<CrafterBlock> CODEC = CrafterBlock.simpleCodec(CrafterBlock::new);
    public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
    private static final int MAX_CRAFTING_TICKS = 6;
    private static final int CRAFTING_TICK_DELAY = 4;
    private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);
    private static final int CRAFTER_ADVANCEMENT_DIAMETER = 17;

    public CrafterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(ORIENTATION, FrontAndTop.NORTH_UP)).setValue(TRIGGERED, false)).setValue(CRAFTING, false));
    }

    protected MapCodec<CrafterBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)blockEntity;
            return crafterBlockEntity.getRedstoneSignal();
        }
        return 0;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        boolean bl2 = level.hasNeighborSignal(blockPos);
        boolean bl3 = blockState.getValue(TRIGGERED);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (bl2 && !bl3) {
            level.scheduleTick(blockPos, this, 4);
            level.setBlock(blockPos, (BlockState)blockState.setValue(TRIGGERED, true), 2);
            this.setBlockEntityTriggered(blockEntity, true);
        } else if (!bl2 && bl3) {
            level.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(TRIGGERED, false)).setValue(CRAFTING, false), 2);
            this.setBlockEntityTriggered(blockEntity, false);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        this.dispenseFrom(blockState, serverLevel, blockPos);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : CrafterBlock.createTickerHelper(blockEntityType, BlockEntityType.CRAFTER, CrafterBlockEntity::serverTick);
    }

    private void setBlockEntityTriggered(@Nullable BlockEntity blockEntity, boolean bl) {
        if (blockEntity instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)blockEntity;
            crafterBlockEntity.setTriggered(bl);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        CrafterBlockEntity crafterBlockEntity = new CrafterBlockEntity(blockPos, blockState);
        crafterBlockEntity.setTriggered(blockState.hasProperty(TRIGGERED) && blockState.getValue(TRIGGERED) != false);
        return crafterBlockEntity;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getNearestLookingDirection().getOpposite();
        Direction direction2 = switch (direction) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> blockPlaceContext.getHorizontalDirection().getOpposite();
            case Direction.UP -> blockPlaceContext.getHorizontalDirection();
            case Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST -> Direction.UP;
        };
        return (BlockState)((BlockState)this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2))).setValue(TRIGGERED, blockPlaceContext.getLevel().hasNeighborSignal(blockPlaceContext.getClickedPos()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (blockState.getValue(TRIGGERED).booleanValue()) {
            level.scheduleTick(blockPos, this, 4);
        }
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CrafterBlockEntity) {
            player.openMenu((CrafterBlockEntity)blockEntity);
        }
        return InteractionResult.CONSUME;
    }

    protected void dispenseFrom(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos) {
        Object object = serverLevel.getBlockEntity(blockPos);
        if (!(object instanceof CrafterBlockEntity)) {
            return;
        }
        CrafterBlockEntity crafterBlockEntity = (CrafterBlockEntity)object;
        object = crafterBlockEntity.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> optional = CrafterBlock.getPotentialResults(serverLevel, (CraftingInput)object);
        if (optional.isEmpty()) {
            serverLevel.levelEvent(1050, blockPos, 0);
            return;
        }
        RecipeHolder<CraftingRecipe> recipeHolder = optional.get();
        ItemStack itemStack2 = recipeHolder.value().assemble(object, serverLevel.registryAccess());
        if (itemStack2.isEmpty()) {
            serverLevel.levelEvent(1050, blockPos, 0);
            return;
        }
        crafterBlockEntity.setCraftingTicksRemaining(6);
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(CRAFTING, true), 2);
        itemStack2.onCraftedBySystem(serverLevel);
        this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStack2, blockState, recipeHolder);
        for (ItemStack itemStack3 : recipeHolder.value().getRemainingItems(object)) {
            if (itemStack3.isEmpty()) continue;
            this.dispenseItem(serverLevel, blockPos, crafterBlockEntity, itemStack3, blockState, recipeHolder);
        }
        crafterBlockEntity.getItems().forEach(itemStack -> {
            if (itemStack.isEmpty()) {
                return;
            }
            itemStack.shrink(1);
        });
        crafterBlockEntity.setChanged();
    }

    public static Optional<RecipeHolder<CraftingRecipe>> getPotentialResults(Level level, CraftingInput craftingInput) {
        return RECIPE_CACHE.get(level, craftingInput);
    }

    private void dispenseItem(ServerLevel serverLevel, BlockPos blockPos, CrafterBlockEntity crafterBlockEntity, ItemStack itemStack, BlockState blockState, RecipeHolder<CraftingRecipe> recipeHolder) {
        Object object;
        Object object2;
        Direction direction = blockState.getValue(ORIENTATION).front();
        Container container = HopperBlockEntity.getContainerAt(serverLevel, blockPos.relative(direction));
        ItemStack itemStack2 = itemStack.copy();
        if (container != null && (container instanceof CrafterBlockEntity || itemStack.getCount() > container.getMaxStackSize(itemStack))) {
            while (!itemStack2.isEmpty() && ((ItemStack)(object2 = HopperBlockEntity.addItem(crafterBlockEntity, container, (ItemStack)(object = itemStack2.copyWithCount(1)), direction.getOpposite()))).isEmpty()) {
                itemStack2.shrink(1);
            }
        } else if (container != null) {
            int n;
            while (!itemStack2.isEmpty() && (n = itemStack2.getCount()) != (itemStack2 = HopperBlockEntity.addItem(crafterBlockEntity, container, itemStack2, direction.getOpposite())).getCount()) {
            }
        }
        if (!itemStack2.isEmpty()) {
            object = Vec3.atCenterOf(blockPos);
            object2 = ((Vec3)object).relative(direction, 0.7);
            DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack2, 6, direction, (Position)object2);
            for (ServerPlayer serverPlayer : serverLevel.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize((Vec3)object, 17.0, 17.0, 17.0))) {
                CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.trigger(serverPlayer, recipeHolder.id(), crafterBlockEntity.getItems());
            }
            serverLevel.levelEvent(1049, blockPos, 0);
            serverLevel.levelEvent(2010, blockPos, direction.get3DDataValue());
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION, TRIGGERED, CRAFTING);
    }
}

