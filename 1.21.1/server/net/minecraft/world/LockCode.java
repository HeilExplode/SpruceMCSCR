/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record LockCode(String key) {
    public static final LockCode NO_LOCK = new LockCode("");
    public static final Codec<LockCode> CODEC = Codec.STRING.xmap(LockCode::new, LockCode::key);
    public static final String TAG_LOCK = "Lock";

    public boolean unlocksWith(ItemStack itemStack) {
        if (this.key.isEmpty()) {
            return true;
        }
        Component component = itemStack.get(DataComponents.CUSTOM_NAME);
        return component != null && this.key.equals(component.getString());
    }

    public void addToTag(CompoundTag compoundTag) {
        if (!this.key.isEmpty()) {
            compoundTag.putString(TAG_LOCK, this.key);
        }
    }

    public static LockCode fromTag(CompoundTag compoundTag) {
        if (compoundTag.contains(TAG_LOCK, 8)) {
            return new LockCode(compoundTag.getString(TAG_LOCK));
        }
        return NO_LOCK;
    }
}

