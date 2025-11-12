/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public interface AdvancementSubProvider {
    public void generate(HolderLookup.Provider var1, Consumer<AdvancementHolder> var2);

    public static AdvancementHolder createPlaceholder(String string) {
        return Advancement.Builder.advancement().build(ResourceLocation.parse(string));
    }
}

