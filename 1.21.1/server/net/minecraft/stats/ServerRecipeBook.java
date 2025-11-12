/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();

    public int addRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        ArrayList arrayList = Lists.newArrayList();
        int n = 0;
        for (RecipeHolder<?> recipeHolder : collection) {
            ResourceLocation resourceLocation = recipeHolder.id();
            if (this.known.contains(resourceLocation) || recipeHolder.value().isSpecial()) continue;
            this.add(resourceLocation);
            this.addHighlight(resourceLocation);
            arrayList.add(resourceLocation);
            CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, recipeHolder);
            ++n;
        }
        if (arrayList.size() > 0) {
            this.sendRecipes(ClientboundRecipePacket.State.ADD, serverPlayer, arrayList);
        }
        return n;
    }

    public int removeRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        ArrayList arrayList = Lists.newArrayList();
        int n = 0;
        for (RecipeHolder<?> recipeHolder : collection) {
            ResourceLocation resourceLocation = recipeHolder.id();
            if (!this.known.contains(resourceLocation)) continue;
            this.remove(resourceLocation);
            arrayList.add(resourceLocation);
            ++n;
        }
        this.sendRecipes(ClientboundRecipePacket.State.REMOVE, serverPlayer, arrayList);
        return n;
    }

    private void sendRecipes(ClientboundRecipePacket.State state, ServerPlayer serverPlayer, List<ResourceLocation> list) {
        serverPlayer.connection.send(new ClientboundRecipePacket(state, list, Collections.emptyList(), this.getBookSettings()));
    }

    public CompoundTag toNbt() {
        CompoundTag compoundTag = new CompoundTag();
        this.getBookSettings().write(compoundTag);
        ListTag listTag = new ListTag();
        for (Object object : this.known) {
            listTag.add(StringTag.valueOf(((ResourceLocation)object).toString()));
        }
        compoundTag.put("recipes", listTag);
        ListTag listTag2 = new ListTag();
        for (ResourceLocation resourceLocation : this.highlight) {
            listTag2.add(StringTag.valueOf(resourceLocation.toString()));
        }
        compoundTag.put("toBeDisplayed", listTag2);
        return compoundTag;
    }

    public void fromNbt(CompoundTag compoundTag, RecipeManager recipeManager) {
        this.setBookSettings(RecipeBookSettings.read(compoundTag));
        ListTag listTag = compoundTag.getList("recipes", 8);
        this.loadRecipes(listTag, this::add, recipeManager);
        ListTag listTag2 = compoundTag.getList("toBeDisplayed", 8);
        this.loadRecipes(listTag2, this::addHighlight, recipeManager);
    }

    private void loadRecipes(ListTag listTag, Consumer<RecipeHolder<?>> consumer, RecipeManager recipeManager) {
        for (int i = 0; i < listTag.size(); ++i) {
            String string = listTag.getString(i);
            try {
                ResourceLocation resourceLocation = ResourceLocation.parse(string);
                Optional<RecipeHolder<?>> optional = recipeManager.byKey(resourceLocation);
                if (optional.isEmpty()) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)resourceLocation);
                    continue;
                }
                consumer.accept(optional.get());
                continue;
            }
            catch (ResourceLocationException resourceLocationException) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", (Object)string);
            }
        }
    }

    public void sendInitialRecipeBook(ServerPlayer serverPlayer) {
        serverPlayer.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
    }
}

