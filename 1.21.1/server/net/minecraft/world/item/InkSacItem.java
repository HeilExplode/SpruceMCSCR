/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class InkSacItem
extends Item
implements SignApplicator {
    public InkSacItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean tryApplyToSign(Level level, SignBlockEntity signBlockEntity, boolean bl, Player player) {
        if (signBlockEntity.updateText(signText -> signText.setHasGlowingText(false), bl)) {
            level.playSound(null, signBlockEntity.getBlockPos(), SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}

