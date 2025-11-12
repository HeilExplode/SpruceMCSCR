/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BucketItem
extends Item
implements DispensibleContainerItem {
    private final Fluid content;

    public BucketItem(Fluid fluid, Item.Properties properties) {
        super(properties);
        this.content = fluid;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        BlockHitResult blockHitResult = BucketItem.getPlayerPOVHitResult(level, player, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }
        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos;
            BlockPos blockPos2 = blockHitResult.getBlockPos();
            Direction direction = blockHitResult.getDirection();
            BlockPos blockPos3 = blockPos2.relative(direction);
            if (!level.mayInteract(player, blockPos2) || !player.mayUseItemAt(blockPos3, direction, itemStack)) {
                return InteractionResultHolder.fail(itemStack);
            }
            if (this.content == Fluids.EMPTY) {
                BucketPickup bucketPickup;
                BlockState blockState = level.getBlockState(blockPos2);
                Object object = blockState.getBlock();
                if (object instanceof BucketPickup && !((ItemStack)(object = (bucketPickup = (BucketPickup)object).pickupBlock(player, level, blockPos2, blockState))).isEmpty()) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    bucketPickup.getPickupSound().ifPresent(soundEvent -> player.playSound((SoundEvent)soundEvent, 1.0f, 1.0f));
                    level.gameEvent((Entity)player, GameEvent.FLUID_PICKUP, blockPos2);
                    ItemStack itemStack2 = ItemUtils.createFilledResult(itemStack, player, (ItemStack)object);
                    if (!level.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, (ItemStack)object);
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack2, level.isClientSide());
                }
                return InteractionResultHolder.fail(itemStack);
            }
            BlockState blockState = level.getBlockState(blockPos2);
            BlockPos blockPos4 = blockPos = blockState.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? blockPos2 : blockPos3;
            if (this.emptyContents(player, level, blockPos, blockHitResult)) {
                this.checkExtraContent(player, level, itemStack, blockPos);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, BucketItem.getEmptySuccessItem(itemStack, player));
                return InteractionResultHolder.sidedSuccess(itemStack3, level.isClientSide());
            }
            return InteractionResultHolder.fail(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    public static ItemStack getEmptySuccessItem(ItemStack itemStack, Player player) {
        if (!player.hasInfiniteMaterials()) {
            return new ItemStack(Items.BUCKET);
        }
        return itemStack;
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack itemStack, BlockPos blockPos) {
    }

    @Override
    public boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult) {
        LiquidBlockContainer liquidBlockContainer;
        boolean bl;
        Object object = this.content;
        if (!(object instanceof FlowingFluid)) {
            return false;
        }
        FlowingFluid flowingFluid = (FlowingFluid)object;
        object = level.getBlockState(blockPos);
        Block block = ((BlockBehaviour.BlockStateBase)object).getBlock();
        boolean bl2 = ((BlockBehaviour.BlockStateBase)object).canBeReplaced(this.content);
        boolean bl3 = bl = ((BlockBehaviour.BlockStateBase)object).isAir() || bl2 || block instanceof LiquidBlockContainer && (liquidBlockContainer = (LiquidBlockContainer)((Object)block)).canPlaceLiquid(player, level, blockPos, (BlockState)object, this.content);
        if (!bl) {
            return blockHitResult != null && this.emptyContents(player, level, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null);
        }
        if (level.dimensionType().ultraWarm() && this.content.is(FluidTags.WATER)) {
            int n = blockPos.getX();
            int n2 = blockPos.getY();
            int n3 = blockPos.getZ();
            level.playSound(player, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);
            for (int i = 0; i < 8; ++i) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, (double)n + Math.random(), (double)n2 + Math.random(), (double)n3 + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if (block instanceof LiquidBlockContainer) {
            liquidBlockContainer = (LiquidBlockContainer)((Object)block);
            if (this.content == Fluids.WATER) {
                liquidBlockContainer.placeLiquid(level, blockPos, (BlockState)object, flowingFluid.getSource(false));
                this.playEmptySound(player, level, blockPos);
                return true;
            }
        }
        if (!level.isClientSide && bl2 && !((BlockBehaviour.BlockStateBase)object).liquid()) {
            level.destroyBlock(blockPos, true);
        }
        if (level.setBlock(blockPos, this.content.defaultFluidState().createLegacyBlock(), 11) || ((BlockBehaviour.BlockStateBase)object).getFluidState().isSource()) {
            this.playEmptySound(player, level, blockPos);
            return true;
        }
        return false;
    }

    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        SoundEvent soundEvent = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        levelAccessor.playSound(player, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
        levelAccessor.gameEvent((Entity)player, GameEvent.FLUID_PLACE, blockPos);
    }
}

