/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownEgg
extends ThrowableItemProjectile {
    private static final EntityDimensions ZERO_SIZED_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);

    public ThrownEgg(EntityType<? extends ThrownEgg> entityType, Level level) {
        super((EntityType<? extends ThrowableItemProjectile>)entityType, level);
    }

    public ThrownEgg(Level level, LivingEntity livingEntity) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.EGG, livingEntity, level);
    }

    public ThrownEgg(Level level, double d, double d2, double d3) {
        super((EntityType<? extends ThrowableItemProjectile>)EntityType.EGG, d, d2, d3, level);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 3) {
            double d = 0.08;
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        entityHitResult.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int n = 1;
                if (this.random.nextInt(32) == 0) {
                    n = 4;
                }
                for (int i = 0; i < n; ++i) {
                    Chicken chicken = EntityType.CHICKEN.create(this.level());
                    if (chicken == null) continue;
                    chicken.setAge(-24000);
                    chicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
                    if (!chicken.fudgePositionAfterSizeChange(ZERO_SIZED_DIMENSIONS)) break;
                    this.level().addFreshEntity(chicken);
                }
            }
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}

