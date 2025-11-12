/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class FireChargeItem
extends Item
implements ProjectileItem {
    public FireChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        boolean bl = false;
        if (CampfireBlock.canLight(blockState) || CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState)) {
            this.playSound(level, blockPos);
            level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(BlockStateProperties.LIT, true));
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            bl = true;
        } else if (BaseFireBlock.canBePlacedAt(level, blockPos = blockPos.relative(useOnContext.getClickedFace()), useOnContext.getHorizontalDirection())) {
            this.playSound(level, blockPos);
            level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
            level.gameEvent((Entity)useOnContext.getPlayer(), GameEvent.BLOCK_PLACE, blockPos);
            bl = true;
        }
        if (bl) {
            useOnContext.getItemInHand().shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    private void playSound(Level level, BlockPos blockPos) {
        RandomSource randomSource = level.getRandom();
        level.playSound(null, blockPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, (randomSource.nextFloat() - randomSource.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        RandomSource randomSource = level.getRandom();
        double d = randomSource.triangle(direction.getStepX(), 0.11485000000000001);
        double d2 = randomSource.triangle(direction.getStepY(), 0.11485000000000001);
        double d3 = randomSource.triangle(direction.getStepZ(), 0.11485000000000001);
        Vec3 vec3 = new Vec3(d, d2, d3);
        SmallFireball smallFireball = new SmallFireball(level, position.x(), position.y(), position.z(), vec3.normalize());
        smallFireball.setItem(itemStack);
        return smallFireball;
    }

    @Override
    public void shoot(Projectile projectile, double d, double d2, double d3, float f, float f2) {
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1018).build();
    }
}

