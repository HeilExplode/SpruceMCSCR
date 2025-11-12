/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookShelfBlock
extends BaseEntityBlock {
    public static final MapCodec<ChiseledBookShelfBlock> CODEC = ChiseledBookShelfBlock.simpleCodec(ChiseledBookShelfBlock::new);
    private static final int MAX_BOOKS_IN_STORAGE = 6;
    public static final int BOOKS_PER_ROW = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED);

    public MapCodec<ChiseledBookShelfBlock> codec() {
        return CODEC;
    }

    public ChiseledBookShelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
        BlockState blockState = (BlockState)((BlockState)this.stateDefinition.any()).setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (BooleanProperty booleanProperty : SLOT_OCCUPIED_PROPERTIES) {
            blockState = (BlockState)blockState.setValue(booleanProperty, false);
        }
        this.registerDefaultState(blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Object object = level.getBlockEntity(blockPos);
        if (!(object instanceof ChiseledBookShelfBlockEntity)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)object;
        if (!itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        object = this.getHitSlot(blockHitResult, blockState);
        if (((OptionalInt)object).isEmpty()) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (((Boolean)blockState.getValue(SLOT_OCCUPIED_PROPERTIES.get(((OptionalInt)object).getAsInt()))).booleanValue()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ChiseledBookShelfBlock.addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, ((OptionalInt)object).getAsInt());
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        Object object = level.getBlockEntity(blockPos);
        if (!(object instanceof ChiseledBookShelfBlockEntity)) {
            return InteractionResult.PASS;
        }
        ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)object;
        object = this.getHitSlot(blockHitResult, blockState);
        if (((OptionalInt)object).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!((Boolean)blockState.getValue(SLOT_OCCUPIED_PROPERTIES.get(((OptionalInt)object).getAsInt()))).booleanValue()) {
            return InteractionResult.CONSUME;
        }
        ChiseledBookShelfBlock.removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, ((OptionalInt)object).getAsInt());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private OptionalInt getHitSlot(BlockHitResult blockHitResult, BlockState blockState) {
        return ChiseledBookShelfBlock.getRelativeHitCoordinatesForBlockFace(blockHitResult, blockState.getValue(HorizontalDirectionalBlock.FACING)).map(vec2 -> {
            int n = vec2.y >= 0.5f ? 0 : 1;
            int n2 = ChiseledBookShelfBlock.getSection(vec2.x);
            return OptionalInt.of(n2 + n * 3);
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction) {
        Direction direction2 = blockHitResult.getDirection();
        if (direction != direction2) {
            return Optional.empty();
        }
        BlockPos blockPos = blockHitResult.getBlockPos().relative(direction2);
        Vec3 vec3 = blockHitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        return switch (direction2) {
            default -> throw new MatchException(null, null);
            case Direction.NORTH -> Optional.of(new Vec2((float)(1.0 - d), (float)d2));
            case Direction.SOUTH -> Optional.of(new Vec2((float)d, (float)d2));
            case Direction.WEST -> Optional.of(new Vec2((float)d3, (float)d2));
            case Direction.EAST -> Optional.of(new Vec2((float)(1.0 - d3), (float)d2));
            case Direction.DOWN, Direction.UP -> Optional.empty();
        };
    }

    private static int getSection(float f) {
        float f2 = 0.0625f;
        float f3 = 0.375f;
        if (f < 0.375f) {
            return 0;
        }
        float f4 = 0.6875f;
        if (f < 0.6875f) {
            return 1;
        }
        return 2;
    }

    private static void addBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int n) {
        if (level.isClientSide) {
            return;
        }
        player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
        chiseledBookShelfBlockEntity.setItem(n, itemStack.consumeAndReturn(1, player));
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private static void removeBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, int n) {
        if (level.isClientSide) {
            return;
        }
        ItemStack itemStack = chiseledBookShelfBlockEntity.removeItem(n, 1);
        SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, false);
        }
        level.gameEvent((Entity)player, GameEvent.BLOCK_CHANGE, blockPos);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChiseledBookShelfBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(property -> builder.add((Property<?>)property));
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        boolean bl2;
        ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity;
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ChiseledBookShelfBlockEntity && !(chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)blockEntity).isEmpty()) {
            for (int i = 0; i < 6; ++i) {
                ItemStack itemStack = chiseledBookShelfBlockEntity.getItem(i);
                if (itemStack.isEmpty()) continue;
                Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
            }
            chiseledBookShelfBlockEntity.clearContent();
            bl2 = true;
        } else {
            bl2 = false;
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
        if (bl2) {
            level.updateNeighbourForOutputSignal(blockPos, this);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.isClientSide()) {
            return 0;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof ChiseledBookShelfBlockEntity) {
            ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity = (ChiseledBookShelfBlockEntity)blockEntity;
            return chiseledBookShelfBlockEntity.getLastInteractedSlot() + 1;
        }
        return 0;
    }
}

