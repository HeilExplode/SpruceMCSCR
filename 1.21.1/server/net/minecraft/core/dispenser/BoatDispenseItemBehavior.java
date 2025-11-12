/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class BoatDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final Boat.Type type;
    private final boolean isChestBoat;

    public BoatDispenseItemBehavior(Boat.Type type) {
        this(type, false);
    }

    public BoatDispenseItemBehavior(Boat.Type type, boolean bl) {
        this.type = type;
        this.isChestBoat = bl;
    }

    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        double d;
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        ServerLevel serverLevel = blockSource.level();
        Vec3 vec3 = blockSource.center();
        double d2 = 0.5625 + (double)EntityType.BOAT.getWidth() / 2.0;
        double d3 = vec3.x() + (double)direction.getStepX() * d2;
        double d4 = vec3.y() + (double)((float)direction.getStepY() * 1.125f);
        double d5 = vec3.z() + (double)direction.getStepZ() * d2;
        BlockPos blockPos = blockSource.pos().relative(direction);
        if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
            d = 1.0;
        } else if (serverLevel.getBlockState(blockPos).isAir() && serverLevel.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
            d = 0.0;
        } else {
            return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
        }
        Boat boat = this.isChestBoat ? new ChestBoat(serverLevel, d3, d4 + d, d5) : new Boat(serverLevel, d3, d4 + d, d5);
        EntityType.createDefaultStackConfig(serverLevel, itemStack, null).accept(boat);
        boat.setVariant(this.type);
        boat.setYRot(direction.toYRot());
        serverLevel.addFreshEntity(boat);
        itemStack.shrink(1);
        return itemStack;
    }

    @Override
    protected void playSound(BlockSource blockSource) {
        blockSource.level().levelEvent(1000, blockSource.pos(), 0);
    }
}

