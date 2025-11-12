/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ProjectileUtil {
    private static final float DEFAULT_ENTITY_HIT_RESULT_MARGIN = 0.3f;

    public static HitResult getHitResultOnMoveVector(Entity entity, Predicate<Entity> predicate) {
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level();
        Vec3 vec32 = entity.position();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, 0.3f, ClipContext.Block.COLLIDER);
    }

    public static HitResult getHitResultOnMoveVector(Entity entity, Predicate<Entity> predicate, ClipContext.Block block) {
        Vec3 vec3 = entity.getDeltaMovement();
        Level level = entity.level();
        Vec3 vec32 = entity.position();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, 0.3f, block);
    }

    public static HitResult getHitResultOnViewVector(Entity entity, Predicate<Entity> predicate, double d) {
        Vec3 vec3 = entity.getViewVector(0.0f).scale(d);
        Level level = entity.level();
        Vec3 vec32 = entity.getEyePosition();
        return ProjectileUtil.getHitResult(vec32, entity, predicate, vec3, level, 0.0f, ClipContext.Block.COLLIDER);
    }

    private static HitResult getHitResult(Vec3 vec3, Entity entity, Predicate<Entity> predicate, Vec3 vec32, Level level, float f, ClipContext.Block block) {
        EntityHitResult entityHitResult;
        Vec3 vec33 = vec3.add(vec32);
        HitResult hitResult = level.clip(new ClipContext(vec3, vec33, block, ClipContext.Fluid.NONE, entity));
        if (((HitResult)hitResult).getType() != HitResult.Type.MISS) {
            vec33 = hitResult.getLocation();
        }
        if ((entityHitResult = ProjectileUtil.getEntityHitResult(level, entity, vec3, vec33, entity.getBoundingBox().expandTowards(vec32).inflate(1.0), predicate, f)) != null) {
            hitResult = entityHitResult;
        }
        return hitResult;
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, double d) {
        Level level = entity.level();
        double d2 = d;
        Entity entity2 = null;
        Vec3 vec33 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            Vec3 vec34;
            double d3;
            AABB aABB2 = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (aABB2.contains(vec3)) {
                if (!(d2 >= 0.0)) continue;
                entity2 = entity3;
                vec33 = optional.orElse(vec3);
                d2 = 0.0;
                continue;
            }
            if (!optional.isPresent() || !((d3 = vec3.distanceToSqr(vec34 = optional.get())) < d2) && d2 != 0.0) continue;
            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (d2 != 0.0) continue;
                entity2 = entity3;
                vec33 = vec34;
                continue;
            }
            entity2 = entity3;
            vec33 = vec34;
            d2 = d3;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec33);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate) {
        return ProjectileUtil.getEntityHitResult(level, entity, vec3, vec32, aABB, predicate, 0.3f);
    }

    @Nullable
    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate, float f) {
        double d = Double.MAX_VALUE;
        Entity entity2 = null;
        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            double d2;
            AABB aABB2 = entity3.getBoundingBox().inflate(f);
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (!optional.isPresent() || !((d2 = vec3.distanceToSqr(optional.get())) < d)) continue;
            entity2 = entity3;
            d = d2;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2);
    }

    public static void rotateTowardsMovement(Entity entity, float f) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.lengthSqr() == 0.0) {
            return;
        }
        double d = vec3.horizontalDistance();
        entity.setYRot((float)(Mth.atan2(vec3.z, vec3.x) * 57.2957763671875) + 90.0f);
        entity.setXRot((float)(Mth.atan2(d, vec3.y) * 57.2957763671875) - 90.0f);
        while (entity.getXRot() - entity.xRotO < -180.0f) {
            entity.xRotO -= 360.0f;
        }
        while (entity.getXRot() - entity.xRotO >= 180.0f) {
            entity.xRotO += 360.0f;
        }
        while (entity.getYRot() - entity.yRotO < -180.0f) {
            entity.yRotO -= 360.0f;
        }
        while (entity.getYRot() - entity.yRotO >= 180.0f) {
            entity.yRotO += 360.0f;
        }
        entity.setXRot(Mth.lerp(f, entity.xRotO, entity.getXRot()));
        entity.setYRot(Mth.lerp(f, entity.yRotO, entity.getYRot()));
    }

    public static InteractionHand getWeaponHoldingHand(LivingEntity livingEntity, Item item) {
        return livingEntity.getMainHandItem().is(item) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static AbstractArrow getMobArrow(LivingEntity livingEntity, ItemStack itemStack, float f, @Nullable ItemStack itemStack2) {
        ArrowItem arrowItem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);
        AbstractArrow abstractArrow = arrowItem.createArrow(livingEntity.level(), itemStack, livingEntity, itemStack2);
        abstractArrow.setBaseDamageFromMob(f);
        return abstractArrow;
    }
}

