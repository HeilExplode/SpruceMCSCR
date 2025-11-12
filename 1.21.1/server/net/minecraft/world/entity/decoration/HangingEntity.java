/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public abstract class HangingEntity
extends BlockAttachedEntity {
    protected static final Predicate<Entity> HANGING_ENTITY = entity -> entity instanceof HangingEntity;
    protected Direction direction = Direction.SOUTH;

    protected HangingEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super((EntityType<? extends BlockAttachedEntity>)entityType, level);
    }

    protected HangingEntity(EntityType<? extends HangingEntity> entityType, Level level, BlockPos blockPos) {
        this(entityType, level);
        this.pos = blockPos;
    }

    protected void setDirection(Direction direction) {
        Objects.requireNonNull(direction);
        Validate.isTrue((boolean)direction.getAxis().isHorizontal());
        this.direction = direction;
        this.setYRot(this.direction.get2DDataValue() * 90);
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        if (this.direction == null) {
            return;
        }
        AABB aABB = this.calculateBoundingBox(this.pos, this.direction);
        Vec3 vec3 = aABB.getCenter();
        this.setPosRaw(vec3.x, vec3.y, vec3.z);
        this.setBoundingBox(aABB);
    }

    protected abstract AABB calculateBoundingBox(BlockPos var1, Direction var2);

    @Override
    public boolean survives() {
        if (!this.level().noCollision(this)) {
            return false;
        }
        boolean bl = BlockPos.betweenClosedStream(this.calculateSupportBox()).allMatch(blockPos -> {
            BlockState blockState = this.level().getBlockState((BlockPos)blockPos);
            return blockState.isSolid() || DiodeBlock.isDiode(blockState);
        });
        if (!bl) {
            return false;
        }
        return this.level().getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    protected AABB calculateSupportBox() {
        return this.getBoundingBox().move(this.direction.step().mul(-0.5f)).deflate(1.0E-7);
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    public abstract void playPlacementSound();

    @Override
    public ItemEntity spawnAtLocation(ItemStack itemStack, float f) {
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX() + (double)((float)this.direction.getStepX() * 0.15f), this.getY() + (double)f, this.getZ() + (double)((float)this.direction.getStepZ() * 0.15f), itemStack);
        itemEntity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itemEntity);
        return itemEntity;
    }

    @Override
    public float rotate(Rotation rotation) {
        if (this.direction.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180: {
                    this.direction = this.direction.getOpposite();
                    break;
                }
                case COUNTERCLOCKWISE_90: {
                    this.direction = this.direction.getCounterClockWise();
                    break;
                }
                case CLOCKWISE_90: {
                    this.direction = this.direction.getClockWise();
                    break;
                }
            }
        }
        float f = Mth.wrapDegrees(this.getYRot());
        return switch (rotation) {
            case Rotation.CLOCKWISE_180 -> f + 180.0f;
            case Rotation.COUNTERCLOCKWISE_90 -> f + 90.0f;
            case Rotation.CLOCKWISE_90 -> f + 270.0f;
            default -> f;
        };
    }

    @Override
    public float mirror(Mirror mirror) {
        return this.rotate(mirror.getRotation(this.direction));
    }
}

