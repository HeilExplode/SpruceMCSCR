/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

public class ExplorationMapFunction
extends LootItemConditionalFunction {
    public static final TagKey<Structure> DEFAULT_DESTINATION = StructureTags.ON_TREASURE_MAPS;
    public static final Holder<MapDecorationType> DEFAULT_DECORATION = MapDecorationTypes.WOODLAND_MANSION;
    public static final byte DEFAULT_ZOOM = 2;
    public static final int DEFAULT_SEARCH_RADIUS = 50;
    public static final boolean DEFAULT_SKIP_EXISTING = true;
    public static final MapCodec<ExplorationMapFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> ExplorationMapFunction.commonFields(instance).and(instance.group((App)TagKey.codec(Registries.STRUCTURE).optionalFieldOf("destination", DEFAULT_DESTINATION).forGetter(explorationMapFunction -> explorationMapFunction.destination), (App)MapDecorationType.CODEC.optionalFieldOf("decoration", DEFAULT_DECORATION).forGetter(explorationMapFunction -> explorationMapFunction.mapDecoration), (App)Codec.BYTE.optionalFieldOf("zoom", (Object)2).forGetter(explorationMapFunction -> explorationMapFunction.zoom), (App)Codec.INT.optionalFieldOf("search_radius", (Object)50).forGetter(explorationMapFunction -> explorationMapFunction.searchRadius), (App)Codec.BOOL.optionalFieldOf("skip_existing_chunks", (Object)true).forGetter(explorationMapFunction -> explorationMapFunction.skipKnownStructures))).apply((Applicative)instance, ExplorationMapFunction::new));
    private final TagKey<Structure> destination;
    private final Holder<MapDecorationType> mapDecoration;
    private final byte zoom;
    private final int searchRadius;
    private final boolean skipKnownStructures;

    ExplorationMapFunction(List<LootItemCondition> list, TagKey<Structure> tagKey, Holder<MapDecorationType> holder, byte by, int n, boolean bl) {
        super(list);
        this.destination = tagKey;
        this.mapDecoration = holder;
        this.zoom = by;
        this.searchRadius = n;
        this.skipKnownStructures = bl;
    }

    public LootItemFunctionType<ExplorationMapFunction> getType() {
        return LootItemFunctions.EXPLORATION_MAP;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        ServerLevel serverLevel;
        BlockPos blockPos;
        if (!itemStack.is(Items.MAP)) {
            return itemStack;
        }
        Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (vec3 != null && (blockPos = (serverLevel = lootContext.getLevel()).findNearestMapStructure(this.destination, BlockPos.containing(vec3), this.searchRadius, this.skipKnownStructures)) != null) {
            ItemStack itemStack2 = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), this.zoom, true, true);
            MapItem.renderBiomePreviewMap(serverLevel, itemStack2);
            MapItemSavedData.addTargetDecoration(itemStack2, blockPos, "+", this.mapDecoration);
            return itemStack2;
        }
        return itemStack;
    }

    public static Builder makeExplorationMap() {
        return new Builder();
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private TagKey<Structure> destination = DEFAULT_DESTINATION;
        private Holder<MapDecorationType> mapDecoration = DEFAULT_DECORATION;
        private byte zoom = (byte)2;
        private int searchRadius = 50;
        private boolean skipKnownStructures = true;

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder setDestination(TagKey<Structure> tagKey) {
            this.destination = tagKey;
            return this;
        }

        public Builder setMapDecoration(Holder<MapDecorationType> holder) {
            this.mapDecoration = holder;
            return this;
        }

        public Builder setZoom(byte by) {
            this.zoom = by;
            return this;
        }

        public Builder setSearchRadius(int n) {
            this.searchRadius = n;
            return this;
        }

        public Builder setSkipKnownStructures(boolean bl) {
            this.skipKnownStructures = bl;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }
}

