/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.apache.commons.lang3.mutable.MutableObject;

public class CopyCustomDataFunction
extends LootItemConditionalFunction {
    public static final MapCodec<CopyCustomDataFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> CopyCustomDataFunction.commonFields(instance).and(instance.group((App)NbtProviders.CODEC.fieldOf("source").forGetter(copyCustomDataFunction -> copyCustomDataFunction.source), (App)CopyOperation.CODEC.listOf().fieldOf("ops").forGetter(copyCustomDataFunction -> copyCustomDataFunction.operations))).apply((Applicative)instance, CopyCustomDataFunction::new));
    private final NbtProvider source;
    private final List<CopyOperation> operations;

    CopyCustomDataFunction(List<LootItemCondition> list, NbtProvider nbtProvider, List<CopyOperation> list2) {
        super(list);
        this.source = nbtProvider;
        this.operations = List.copyOf(list2);
    }

    public LootItemFunctionType<CopyCustomDataFunction> getType() {
        return LootItemFunctions.COPY_CUSTOM_DATA;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.source.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Tag tag = this.source.get(lootContext);
        if (tag == null) {
            return itemStack;
        }
        MutableObject mutableObject = new MutableObject();
        Supplier<Tag> supplier = () -> {
            if (mutableObject.getValue() == null) {
                mutableObject.setValue((Object)itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            }
            return (Tag)mutableObject.getValue();
        };
        this.operations.forEach(copyOperation -> copyOperation.apply(supplier, tag));
        CompoundTag compoundTag = (CompoundTag)mutableObject.getValue();
        if (compoundTag != null) {
            CustomData.set(DataComponents.CUSTOM_DATA, itemStack, compoundTag);
        }
        return itemStack;
    }

    @Deprecated
    public static Builder copyData(NbtProvider nbtProvider) {
        return new Builder(nbtProvider);
    }

    public static Builder copyData(LootContext.EntityTarget entityTarget) {
        return new Builder(ContextNbtProvider.forContextEntity(entityTarget));
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final NbtProvider source;
        private final List<CopyOperation> ops = Lists.newArrayList();

        Builder(NbtProvider nbtProvider) {
            this.source = nbtProvider;
        }

        public Builder copy(String string, String string2, MergeStrategy mergeStrategy) {
            try {
                this.ops.add(new CopyOperation(NbtPathArgument.NbtPath.of(string), NbtPathArgument.NbtPath.of(string2), mergeStrategy));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException(commandSyntaxException);
            }
            return this;
        }

        public Builder copy(String string, String string2) {
            return this.copy(string, string2, MergeStrategy.REPLACE);
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyCustomDataFunction(this.getConditions(), this.source, this.ops);
        }

        @Override
        protected /* synthetic */ LootItemConditionalFunction.Builder getThis() {
            return this.getThis();
        }
    }

    record CopyOperation(NbtPathArgument.NbtPath sourcePath, NbtPathArgument.NbtPath targetPath, MergeStrategy op) {
        public static final Codec<CopyOperation> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NbtPathArgument.NbtPath.CODEC.fieldOf("source").forGetter(CopyOperation::sourcePath), (App)NbtPathArgument.NbtPath.CODEC.fieldOf("target").forGetter(CopyOperation::targetPath), (App)MergeStrategy.CODEC.fieldOf("op").forGetter(CopyOperation::op)).apply((Applicative)instance, CopyOperation::new));

        public void apply(Supplier<Tag> supplier, Tag tag) {
            try {
                List<Tag> list = this.sourcePath.get(tag);
                if (!list.isEmpty()) {
                    this.op.merge(supplier.get(), this.targetPath, list);
                }
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
    }

    public static enum MergeStrategy implements StringRepresentable
    {
        REPLACE("replace"){

            @Override
            public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                nbtPath.set(tag, (Tag)Iterables.getLast(list));
            }
        }
        ,
        APPEND("append"){

            @Override
            public void merge(Tag tag2, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                List<Tag> list2 = nbtPath.getOrCreate(tag2, ListTag::new);
                list2.forEach(tag -> {
                    if (tag instanceof ListTag) {
                        list.forEach(tag2 -> ((ListTag)tag).add(tag2.copy()));
                    }
                });
            }
        }
        ,
        MERGE("merge"){

            @Override
            public void merge(Tag tag2, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                List<Tag> list2 = nbtPath.getOrCreate(tag2, CompoundTag::new);
                list2.forEach(tag -> {
                    if (tag instanceof CompoundTag) {
                        list.forEach(tag2 -> {
                            if (tag2 instanceof CompoundTag) {
                                ((CompoundTag)tag).merge((CompoundTag)tag2);
                            }
                        });
                    }
                });
            }
        };

        public static final Codec<MergeStrategy> CODEC;
        private final String name;

        public abstract void merge(Tag var1, NbtPathArgument.NbtPath var2, List<Tag> var3) throws CommandSyntaxException;

        MergeStrategy(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(MergeStrategy::values);
        }
    }
}

