/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class PlayerHeadItem
extends StandingAndWallBlockItem {
    public PlayerHeadItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties, Direction.DOWN);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
        if (resolvableProfile != null && resolvableProfile.name().isPresent()) {
            return Component.translatable(this.getDescriptionId() + ".named", resolvableProfile.name().get());
        }
        return super.getName(itemStack);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack itemStack) {
        ResolvableProfile resolvableProfile2 = itemStack.get(DataComponents.PROFILE);
        if (resolvableProfile2 != null && !resolvableProfile2.isResolved()) {
            resolvableProfile2.resolve().thenAcceptAsync(resolvableProfile -> itemStack.set(DataComponents.PROFILE, resolvableProfile), SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
        }
    }
}

