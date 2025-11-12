/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class CrossbowItem
extends ProjectileWeaponItem {
    private static final float MAX_CHARGE_DURATION = 1.25f;
    public static final int DEFAULT_RANGE = 8;
    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;
    private static final float START_SOUND_PERCENT = 0.2f;
    private static final float MID_SOUND_PERCENT = 0.5f;
    private static final float ARROW_POWER = 3.15f;
    private static final float FIREWORK_POWER = 1.6f;
    public static final float MOB_ARROW_POWER = 1.6f;
    private static final ChargingSounds DEFAULT_SOUNDS = new ChargingSounds(Optional.of(SoundEvents.CROSSBOW_LOADING_START), Optional.of(SoundEvents.CROSSBOW_LOADING_MIDDLE), Optional.of(SoundEvents.CROSSBOW_LOADING_END));

    public CrossbowItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ARROW_OR_FIREWORK;
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            this.performShooting(level, player, interactionHand, itemStack, CrossbowItem.getShootingPower(chargedProjectiles), 1.0f, null);
            return InteractionResultHolder.consume(itemStack);
        }
        if (!player.getProjectile(itemStack).isEmpty()) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemStack);
        }
        return InteractionResultHolder.fail(itemStack);
    }

    private static float getShootingPower(ChargedProjectiles chargedProjectiles) {
        if (chargedProjectiles.contains(Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        int n2 = this.getUseDuration(itemStack, livingEntity) - n;
        float f = CrossbowItem.getPowerForTime(n2, itemStack, livingEntity);
        if (f >= 1.0f && !CrossbowItem.isCharged(itemStack) && CrossbowItem.tryLoadProjectiles(livingEntity, itemStack)) {
            ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
            chargingSounds.end().ifPresent(holder -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)holder.value(), livingEntity.getSoundSource(), 1.0f, 1.0f / (level.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f));
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity livingEntity, ItemStack itemStack) {
        List<ItemStack> list = CrossbowItem.draw(itemStack, livingEntity.getProjectile(itemStack), livingEntity);
        if (!list.isEmpty()) {
            itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));
            return true;
        }
        return false;
    }

    public static boolean isCharged(ItemStack itemStack) {
        ChargedProjectiles chargedProjectiles = itemStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        return !chargedProjectiles.isEmpty();
    }

    @Override
    protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int n, float f, float f2, float f3, @Nullable LivingEntity livingEntity2) {
        Vector3f vector3f;
        if (livingEntity2 != null) {
            double d = livingEntity2.getX() - livingEntity.getX();
            double d2 = livingEntity2.getZ() - livingEntity.getZ();
            double d3 = Math.sqrt(d * d + d2 * d2);
            double d4 = livingEntity2.getY(0.3333333333333333) - projectile.getY() + d3 * (double)0.2f;
            vector3f = CrossbowItem.getProjectileShotVector(livingEntity, new Vec3(d, d4, d2), f3);
        } else {
            Vec3 vec3 = livingEntity.getUpVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(f3 * ((float)Math.PI / 180)), vec3.x, vec3.y, vec3.z);
            Vec3 vec32 = livingEntity.getViewVector(1.0f);
            vector3f = vec32.toVector3f().rotate((Quaternionfc)quaternionf);
        }
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), f, f2);
        float f4 = CrossbowItem.getShotPitch(livingEntity.getRandom(), n);
        livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.CROSSBOW_SHOOT, livingEntity.getSoundSource(), 1.0f, f4);
    }

    private static Vector3f getProjectileShotVector(LivingEntity livingEntity, Vec3 vec3, float f) {
        Vec3 vec32;
        Vector3f vector3f = vec3.toVector3f().normalize();
        Vector3f vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f));
        if ((double)vector3f2.lengthSquared() <= 1.0E-7) {
            vec32 = livingEntity.getUpVector(1.0f);
            vector3f2 = new Vector3f((Vector3fc)vector3f).cross((Vector3fc)vec32.toVector3f());
        }
        vec32 = new Vector3f((Vector3fc)vector3f).rotateAxis(1.5707964f, vector3f2.x, vector3f2.y, vector3f2.z);
        return new Vector3f((Vector3fc)vector3f).rotateAxis(f * ((float)Math.PI / 180), ((Vector3f)vec32).x, ((Vector3f)vec32).y, ((Vector3f)vec32).z);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        if (itemStack2.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, itemStack2, livingEntity, livingEntity.getX(), livingEntity.getEyeY() - (double)0.15f, livingEntity.getZ(), true);
        }
        Projectile projectile = super.createProjectile(level, livingEntity, itemStack, itemStack2, bl);
        if (projectile instanceof AbstractArrow) {
            AbstractArrow abstractArrow = (AbstractArrow)projectile;
            abstractArrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        }
        return projectile;
    }

    @Override
    protected int getDurabilityUse(ItemStack itemStack) {
        return itemStack.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    public void performShooting(Level level, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, float f, float f2, @Nullable LivingEntity livingEntity2) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        ChargedProjectiles chargedProjectiles = itemStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) {
            return;
        }
        this.shoot(serverLevel, livingEntity, interactionHand, itemStack, chargedProjectiles.getItems(), f, f2, livingEntity instanceof Player, livingEntity2);
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, itemStack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
    }

    private static float getShotPitch(RandomSource randomSource, int n) {
        if (n == 0) {
            return 1.0f;
        }
        return CrossbowItem.getRandomShotPitch((n & 1) == 1, randomSource);
    }

    private static float getRandomShotPitch(boolean bl, RandomSource randomSource) {
        float f = bl ? 0.63f : 0.43f;
        return 1.0f / (randomSource.nextFloat() * 0.5f + 1.8f) + f;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int n) {
        if (!level.isClientSide) {
            ChargingSounds chargingSounds = this.getChargingSounds(itemStack);
            float f = (float)(itemStack.getUseDuration(livingEntity) - n) / (float)CrossbowItem.getChargeDuration(itemStack, livingEntity);
            if (f < 0.2f) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
            }
            if (f >= 0.2f && !this.startSoundPlayed) {
                this.startSoundPlayed = true;
                chargingSounds.start().ifPresent(holder -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)holder.value(), SoundSource.PLAYERS, 0.5f, 1.0f));
            }
            if (f >= 0.5f && !this.midLoadSoundPlayed) {
                this.midLoadSoundPlayed = true;
                chargingSounds.mid().ifPresent(holder -> level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (SoundEvent)holder.value(), SoundSource.PLAYERS, 0.5f, 1.0f));
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return CrossbowItem.getChargeDuration(itemStack, livingEntity) + 3;
    }

    public static int getChargeDuration(ItemStack itemStack, LivingEntity livingEntity) {
        float f = EnchantmentHelper.modifyCrossbowChargingTime(itemStack, livingEntity, 1.25f);
        return Mth.floor(f * 20.0f);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.CROSSBOW;
    }

    ChargingSounds getChargingSounds(ItemStack itemStack) {
        return EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.CROSSBOW_CHARGING_SOUNDS).orElse(DEFAULT_SOUNDS);
    }

    private static float getPowerForTime(int n, ItemStack itemStack, LivingEntity livingEntity) {
        float f = (float)n / (float)CrossbowItem.getChargeDuration(itemStack, livingEntity);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        ChargedProjectiles chargedProjectiles = itemStack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles == null || chargedProjectiles.isEmpty()) {
            return;
        }
        ItemStack itemStack2 = chargedProjectiles.getItems().get(0);
        list.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack2.getDisplayName()));
        if (tooltipFlag.isAdvanced() && itemStack2.is(Items.FIREWORK_ROCKET)) {
            ArrayList arrayList = Lists.newArrayList();
            Items.FIREWORK_ROCKET.appendHoverText(itemStack2, tooltipContext, arrayList, tooltipFlag);
            if (!arrayList.isEmpty()) {
                for (int i = 0; i < arrayList.size(); ++i) {
                    arrayList.set(i, Component.literal("  ").append((Component)arrayList.get(i)).withStyle(ChatFormatting.GRAY));
                }
                list.addAll(arrayList);
            }
        }
    }

    @Override
    public boolean useOnRelease(ItemStack itemStack) {
        return itemStack.is(this);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 8;
    }

    public record ChargingSounds(Optional<Holder<SoundEvent>> start, Optional<Holder<SoundEvent>> mid, Optional<Holder<SoundEvent>> end) {
        public static final Codec<ChargingSounds> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.CODEC.optionalFieldOf("start").forGetter(ChargingSounds::start), (App)SoundEvent.CODEC.optionalFieldOf("mid").forGetter(ChargingSounds::mid), (App)SoundEvent.CODEC.optionalFieldOf("end").forGetter(ChargingSounds::end)).apply((Applicative)instance, ChargingSounds::new));
    }
}

