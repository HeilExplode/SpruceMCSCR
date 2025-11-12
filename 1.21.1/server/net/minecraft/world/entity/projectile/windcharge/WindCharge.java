/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile.windcharge;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

public class WindCharge
extends AbstractWindCharge {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new SimpleExplosionDamageCalculator(true, false, Optional.of(Float.valueOf(1.22f)), BuiltInRegistries.BLOCK.getTag(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity()));
    private static final float RADIUS = 1.2f;
    private int noDeflectTicks = 5;

    public WindCharge(EntityType<? extends AbstractWindCharge> entityType, Level level) {
        super(entityType, level);
    }

    public WindCharge(Player player, Level level, double d, double d2, double d3) {
        super(EntityType.WIND_CHARGE, level, player, d, d2, d3);
    }

    public WindCharge(Level level, double d, double d2, double d3, Vec3 vec3) {
        super((EntityType<? extends AbstractWindCharge>)EntityType.WIND_CHARGE, d, d2, d3, vec3, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.noDeflectTicks > 0) {
            --this.noDeflectTicks;
        }
    }

    @Override
    public boolean deflect(ProjectileDeflection projectileDeflection, @Nullable Entity entity, @Nullable Entity entity2, boolean bl) {
        if (this.noDeflectTicks > 0) {
            return false;
        }
        return super.deflect(projectileDeflection, entity, entity2, bl);
    }

    @Override
    protected void explode(Vec3 vec3) {
        this.level().explode(this, null, EXPLOSION_DAMAGE_CALCULATOR, vec3.x(), vec3.y(), vec3.z(), 1.2f, false, Level.ExplosionInteraction.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.WIND_CHARGE_BURST);
    }
}

