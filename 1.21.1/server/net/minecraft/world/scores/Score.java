/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.ReadOnlyScoreInfo;

public class Score
implements ReadOnlyScoreInfo {
    private static final String TAG_SCORE = "Score";
    private static final String TAG_LOCKED = "Locked";
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_FORMAT = "format";
    private int value;
    private boolean locked = true;
    @Nullable
    private Component display;
    @Nullable
    private NumberFormat numberFormat;

    @Override
    public int value() {
        return this.value;
    }

    public void value(int n) {
        this.value = n;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean bl) {
        this.locked = bl;
    }

    @Nullable
    public Component display() {
        return this.display;
    }

    public void display(@Nullable Component component) {
        this.display = component;
    }

    @Override
    @Nullable
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public CompoundTag write(HolderLookup.Provider provider) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt(TAG_SCORE, this.value);
        compoundTag.putBoolean(TAG_LOCKED, this.locked);
        if (this.display != null) {
            compoundTag.putString(TAG_DISPLAY, Component.Serializer.toJson(this.display, provider));
        }
        if (this.numberFormat != null) {
            NumberFormatTypes.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), (Object)this.numberFormat).ifSuccess(tag -> compoundTag.put(TAG_FORMAT, (Tag)tag));
        }
        return compoundTag;
    }

    public static Score read(CompoundTag compoundTag, HolderLookup.Provider provider) {
        Score score = new Score();
        score.value = compoundTag.getInt(TAG_SCORE);
        score.locked = compoundTag.getBoolean(TAG_LOCKED);
        if (compoundTag.contains(TAG_DISPLAY, 8)) {
            score.display = Component.Serializer.fromJson(compoundTag.getString(TAG_DISPLAY), provider);
        }
        if (compoundTag.contains(TAG_FORMAT, 10)) {
            NumberFormatTypes.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), (Object)compoundTag.get(TAG_FORMAT)).ifSuccess(numberFormat -> {
                score.numberFormat = numberFormat;
            });
        }
        return score;
    }
}

