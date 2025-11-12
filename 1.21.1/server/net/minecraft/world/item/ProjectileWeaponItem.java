/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public abstract class ProjectileWeaponItem
extends Item {
    public static final Predicate<ItemStack> ARROW_ONLY = itemStack -> itemStack.is(ItemTags.ARROWS);
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(itemStack -> itemStack.is(Items.FIREWORK_ROCKET));

    public ProjectileWeaponItem(Item.Properties properties) {
        super(properties);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return this.getAllSupportedProjectiles();
    }

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public static ItemStack getHeldProjectile(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.OFF_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.MAIN_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    public abstract int getDefaultProjectileRange();

    protected void shoot(ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack, List<ItemStack> list, float f, float f2, boolean bl, @Nullable LivingEntity livingEntity2) {
        float f3 = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, livingEntity, 0.0f);
        float f4 = list.size() == 1 ? 0.0f : 2.0f * f3 / (float)(list.size() - 1);
        float f5 = (float)((list.size() - 1) % 2) * f4 / 2.0f;
        float f6 = 1.0f;
        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemStack2 = list.get(i);
            if (itemStack2.isEmpty()) continue;
            float f7 = f5 + f6 * (float)((i + 1) / 2) * f4;
            f6 = -f6;
            Projectile projectile = this.createProjectile(serverLevel, livingEntity, itemStack, itemStack2, bl);
            this.shootProjectile(livingEntity, projectile, i, f, f2, f7, livingEntity2);
            serverLevel.addFreshEntity(projectile);
            itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, LivingEntity.getSlotForHand(interactionHand));
            if (itemStack.isEmpty()) break;
        }
    }

    protected int getDurabilityUse(ItemStack itemStack) {
        return 1;
    }

    protected abstract void shootProjectile(LivingEntity var1, Projectile var2, int var3, float var4, float var5, float var6, @Nullable LivingEntity var7);

    protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
        ArrowItem arrowItem;
        Object object;
        Item item = itemStack2.getItem();
        if (item instanceof ArrowItem) {
            object = (ArrowItem)item;
            arrowItem = object;
        } else {
            arrowItem = (ArrowItem)Items.ARROW;
        }
        ArrowItem arrowItem2 = arrowItem;
        object = arrowItem2.createArrow(level, itemStack2, livingEntity, itemStack);
        if (bl) {
            ((AbstractArrow)object).setCritArrow(true);
        }
        return object;
    }

    protected static List<ItemStack> draw(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity) {
        int n;
        Object object;
        if (itemStack2.isEmpty()) {
            return List.of();
        }
        Object object2 = livingEntity.level();
        if (object2 instanceof ServerLevel) {
            object = (ServerLevel)object2;
            n = EnchantmentHelper.processProjectileCount((ServerLevel)object, itemStack, livingEntity, 1);
        } else {
            n = 1;
        }
        int n2 = n;
        object = new ArrayList(n2);
        object2 = itemStack2.copy();
        for (int i = 0; i < n2; ++i) {
            ItemStack itemStack3 = ProjectileWeaponItem.useAmmo(itemStack, (ItemStack)(i == 0 ? itemStack2 : object2), livingEntity, i > 0);
            if (itemStack3.isEmpty()) continue;
            object.add(itemStack3);
        }
        return object;
    }

    protected static ItemStack useAmmo(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity, boolean bl) {
        int n;
        Object object;
        Object object2;
        if (!bl && !livingEntity.hasInfiniteMaterials() && (object2 = livingEntity.level()) instanceof ServerLevel) {
            object = (ServerLevel)object2;
            v0 = EnchantmentHelper.processAmmoUse((ServerLevel)object, itemStack, itemStack2, 1);
        } else {
            v0 = n = 0;
        }
        if (n > itemStack2.getCount()) {
            return ItemStack.EMPTY;
        }
        if (n == 0) {
            object = itemStack2.copyWithCount(1);
            ((ItemStack)object).set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            return object;
        }
        object = itemStack2.split(n);
        if (itemStack2.isEmpty() && livingEntity instanceof Player) {
            object2 = (Player)livingEntity;
            ((Player)object2).getInventory().removeItem(itemStack2);
        }
        return object;
    }
}

