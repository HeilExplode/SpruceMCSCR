/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class ThrowablePotionItem
extends PotionItem
implements ProjectileItem {
    public ThrowablePotionItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide) {
            ThrownPotion thrownPotion = new ThrownPotion(level, player);
            thrownPotion.setItem(itemStack);
            thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0f, 0.5f, 1.0f);
            level.addFreshEntity(thrownPotion);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownPotion thrownPotion = new ThrownPotion(level, position.x(), position.y(), position.z());
        thrownPotion.setItem(itemStack);
        return thrownPotion;
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().uncertainty(ProjectileItem.DispenseConfig.DEFAULT.uncertainty() * 0.5f).power(ProjectileItem.DispenseConfig.DEFAULT.power() * 1.25f).build();
    }
}

