/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class WindChargeItem
extends Item
implements ProjectileItem {
    private static final int COOLDOWN = 10;

    public WindChargeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        Object object;
        if (!level.isClientSide()) {
            object = new WindCharge(player, level, player.position().x(), player.getEyePosition().y(), player.position().z());
            ((Projectile)object).shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 1.0f);
            level.addFreshEntity((Entity)object);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
        object = player.getItemInHand(interactionHand);
        player.getCooldowns().addCooldown(this, 10);
        player.awardStat(Stats.ITEM_USED.get(this));
        ((ItemStack)object).consume(1, player);
        return InteractionResultHolder.sidedSuccess(object, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        RandomSource randomSource = level.getRandom();
        double d = randomSource.triangle(direction.getStepX(), 0.11485000000000001);
        double d2 = randomSource.triangle(direction.getStepY(), 0.11485000000000001);
        double d3 = randomSource.triangle(direction.getStepZ(), 0.11485000000000001);
        Vec3 vec3 = new Vec3(d, d2, d3);
        WindCharge windCharge = new WindCharge(level, position.x(), position.y(), position.z(), vec3);
        windCharge.setDeltaMovement(vec3);
        return windCharge;
    }

    @Override
    public void shoot(Projectile projectile, double d, double d2, double d3, float f, float f2) {
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1051).build();
    }
}

