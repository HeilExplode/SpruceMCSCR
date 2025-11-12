/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;

public interface SuspiciousEffectHolder {
    public SuspiciousStewEffects getSuspiciousEffects();

    public static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    public static SuspiciousEffectHolder tryGet(ItemLike itemLike) {
        Object object;
        FeatureElement featureElement = itemLike.asItem();
        if (featureElement instanceof BlockItem && (featureElement = ((BlockItem)(object = (BlockItem)featureElement)).getBlock()) instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciousEffectHolder = (SuspiciousEffectHolder)((Object)featureElement);
            return suspiciousEffectHolder;
        }
        Item item = itemLike.asItem();
        if (item instanceof SuspiciousEffectHolder) {
            object = (SuspiciousEffectHolder)((Object)item);
            return object;
        }
        return null;
    }
}

