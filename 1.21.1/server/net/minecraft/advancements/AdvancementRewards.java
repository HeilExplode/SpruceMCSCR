/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record AdvancementRewards(int experience, List<ResourceKey<LootTable>> loot, List<ResourceLocation> recipes, Optional<CacheableFunction> function) {
    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.optionalFieldOf("experience", (Object)0).forGetter(AdvancementRewards::experience), (App)ResourceKey.codec(Registries.LOOT_TABLE).listOf().optionalFieldOf("loot", List.of()).forGetter(AdvancementRewards::loot), (App)ResourceLocation.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(AdvancementRewards::recipes), (App)CacheableFunction.CODEC.optionalFieldOf("function").forGetter(AdvancementRewards::function)).apply((Applicative)instance, AdvancementRewards::new));
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void grant(ServerPlayer serverPlayer) {
        serverPlayer.giveExperiencePoints(this.experience);
        LootParams lootParams = new LootParams.Builder(serverPlayer.serverLevel()).withParameter(LootContextParams.THIS_ENTITY, serverPlayer).withParameter(LootContextParams.ORIGIN, serverPlayer.position()).create(LootContextParamSets.ADVANCEMENT_REWARD);
        boolean bl = false;
        for (ResourceKey<LootTable> resourceKey : this.loot) {
            for (ItemStack itemStack : serverPlayer.server.reloadableRegistries().getLootTable(resourceKey).getRandomItems(lootParams)) {
                if (serverPlayer.addItem(itemStack)) {
                    serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                    bl = true;
                    continue;
                }
                ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
                if (itemEntity == null) continue;
                itemEntity.setNoPickUpDelay();
                itemEntity.setTarget(serverPlayer.getUUID());
            }
        }
        if (bl) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        if (!this.recipes.isEmpty()) {
            serverPlayer.awardRecipesByKey(this.recipes);
        }
        MinecraftServer minecraftServer = serverPlayer.server;
        this.function.flatMap(cacheableFunction -> cacheableFunction.get(minecraftServer.getFunctions())).ifPresent(commandFunction -> minecraftServer.getFunctions().execute((CommandFunction<CommandSourceStack>)commandFunction, serverPlayer.createCommandSourceStack().withSuppressedOutput().withPermission(2)));
    }

    public static class Builder {
        private int experience;
        private final ImmutableList.Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
        private final ImmutableList.Builder<ResourceLocation> recipes = ImmutableList.builder();
        private Optional<ResourceLocation> function = Optional.empty();

        public static Builder experience(int n) {
            return new Builder().addExperience(n);
        }

        public Builder addExperience(int n) {
            this.experience += n;
            return this;
        }

        public static Builder loot(ResourceKey<LootTable> resourceKey) {
            return new Builder().addLootTable(resourceKey);
        }

        public Builder addLootTable(ResourceKey<LootTable> resourceKey) {
            this.loot.add(resourceKey);
            return this;
        }

        public static Builder recipe(ResourceLocation resourceLocation) {
            return new Builder().addRecipe(resourceLocation);
        }

        public Builder addRecipe(ResourceLocation resourceLocation) {
            this.recipes.add((Object)resourceLocation);
            return this;
        }

        public static Builder function(ResourceLocation resourceLocation) {
            return new Builder().runs(resourceLocation);
        }

        public Builder runs(ResourceLocation resourceLocation) {
            this.function = Optional.of(resourceLocation);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, (List<ResourceKey<LootTable>>)this.loot.build(), (List<ResourceLocation>)this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}

