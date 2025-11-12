/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot
extends Slot {
    private final Player player;
    private int removeCount;

    public FurnaceResultSlot(Player player, Container container, int n, int n2, int n3) {
        super(container, n, n2, n3);
        this.player = player;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return false;
    }

    @Override
    public ItemStack remove(int n) {
        if (this.hasItem()) {
            this.removeCount += Math.min(n, this.getItem().getCount());
        }
        return super.remove(n);
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);
        super.onTake(player, itemStack);
    }

    @Override
    protected void onQuickCraft(ItemStack itemStack, int n) {
        this.removeCount += n;
        this.checkTakeAchievements(itemStack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack itemStack) {
        itemStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        Object object = this.player;
        if (object instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)object;
            object = this.container;
            if (object instanceof AbstractFurnaceBlockEntity) {
                AbstractFurnaceBlockEntity abstractFurnaceBlockEntity = (AbstractFurnaceBlockEntity)object;
                abstractFurnaceBlockEntity.awardUsedRecipesAndPopExperience(serverPlayer);
            }
        }
        this.removeCount = 0;
    }
}

