/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

public interface SignApplicator {
    public boolean tryApplyToSign(Level var1, SignBlockEntity var2, boolean var3, Player var4);

    default public boolean canApplyToSign(SignText signText, Player player) {
        return signText.hasMessage(player);
    }
}

