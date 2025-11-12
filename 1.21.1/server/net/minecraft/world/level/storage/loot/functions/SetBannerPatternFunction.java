/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetBannerPatternFunction
extends LootItemConditionalFunction {
    public static final MapCodec<SetBannerPatternFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> SetBannerPatternFunction.commonFields(instance).and(instance.group((App)BannerPatternLayers.CODEC.fieldOf("patterns").forGetter(setBannerPatternFunction -> setBannerPatternFunction.patterns), (App)Codec.BOOL.fieldOf("append").forGetter(setBannerPatternFunction -> setBannerPatternFunction.append))).apply((Applicative)instance, SetBannerPatternFunction::new));
    private final BannerPatternLayers patterns;
    private final boolean append;

    SetBannerPatternFunction(List<LootItemCondition> list, BannerPatternLayers bannerPatternLayers, boolean bl) {
        super(list);
        this.patterns = bannerPatternLayers;
        this.append = bl;
    }

    @Override
    protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
        if (this.append) {
            itemStack.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, this.patterns, (bannerPatternLayers, bannerPatternLayers2) -> new BannerPatternLayers.Builder().addAll((BannerPatternLayers)bannerPatternLayers).addAll((BannerPatternLayers)bannerPatternLayers2).build());
        } else {
            itemStack.set(DataComponents.BANNER_PATTERNS, this.patterns);
        }
        return itemStack;
    }

    public LootItemFunctionType<SetBannerPatternFunction> getType() {
        return LootItemFunctions.SET_BANNER_PATTERN;
    }

    public static Builder setBannerPattern(boolean bl) {
        return new Builder(bl);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final BannerPatternLayers.Builder patterns = new BannerPatternLayers.Builder();
        private final boolean append;

        Builder(boolean bl) {
            this.append = bl;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetBannerPatternFunction(this.getConditions(), this.patterns.build(), this.append);
        }

        public Builder addPattern(Holder<BannerPattern> holder, DyeColor dyeColor) {
            this.patterns.add(holder, dyeColor);
            return this;
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

