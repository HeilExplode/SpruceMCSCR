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
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction
extends LootItemConditionalFunction {
    public static final MapCodec<CopyNameFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyNameFunction.commonFields(instance).and((App)NameSource.CODEC.fieldOf("source").forGetter(copyNameFunction -> copyNameFunction.source)).apply((Applicative)instance, CopyNameFunction::new));
    private final NameSource source;

    private CopyNameFunction(List<LootItemCondition> list, NameSource nameSource) {
        super(list);
        this.source = nameSource;
    }

    public LootItemFunctionType<CopyNameFunction> getType() {
        return LootItemFunctions.COPY_NAME;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Object obj = lootContext.getParamOrNull(this.source.param);
        if (obj instanceof Nameable) {
            Nameable nameable = (Nameable)obj;
            itemStack.set(DataComponents.CUSTOM_NAME, nameable.getCustomName());
        }
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> copyName(NameSource nameSource) {
        return CopyNameFunction.simpleBuilder(list -> new CopyNameFunction((List<LootItemCondition>)list, nameSource));
    }

    public static enum NameSource implements StringRepresentable
    {
        THIS("this", LootContextParams.THIS_ENTITY),
        ATTACKING_ENTITY("attacking_entity", LootContextParams.ATTACKING_ENTITY),
        LAST_DAMAGE_PLAYER("last_damage_player", LootContextParams.LAST_DAMAGE_PLAYER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY);

        public static final Codec<NameSource> CODEC;
        private final String name;
        final LootContextParam<?> param;

        private NameSource(String string2, LootContextParam<?> lootContextParam) {
            this.name = string2;
            this.param = lootContextParam;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(NameSource::values);
        }
    }
}

