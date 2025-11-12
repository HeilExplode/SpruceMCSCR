/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DataResult$Error
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai.attributes;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public record AttributeModifier(ResourceLocation id, double amount, Operation operation) {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("id").forGetter(AttributeModifier::id), (App)Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount), (App)Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)).apply((Applicative)instance, AttributeModifier::new));
    public static final Codec<AttributeModifier> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, AttributeModifier::id, ByteBufCodecs.DOUBLE, AttributeModifier::amount, Operation.STREAM_CODEC, AttributeModifier::operation, AttributeModifier::new);

    public CompoundTag save() {
        DataResult dataResult = CODEC.encode((Object)this, (DynamicOps)NbtOps.INSTANCE, (Object)new CompoundTag());
        return (CompoundTag)dataResult.getOrThrow();
    }

    @Nullable
    public static AttributeModifier load(CompoundTag compoundTag) {
        DataResult dataResult = CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag);
        if (dataResult.isSuccess()) {
            return (AttributeModifier)dataResult.getOrThrow();
        }
        LOGGER.warn("Unable to create attribute: {}", (Object)((DataResult.Error)dataResult.error().get()).message());
        return null;
    }

    public boolean is(ResourceLocation resourceLocation) {
        return resourceLocation.equals(this.id);
    }

    public static enum Operation implements StringRepresentable
    {
        ADD_VALUE("add_value", 0),
        ADD_MULTIPLIED_BASE("add_multiplied_base", 1),
        ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

        public static final IntFunction<Operation> BY_ID;
        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC;
        public static final Codec<Operation> CODEC;
        private final String name;
        private final int id;

        private Operation(String string2, int n2) {
            this.name = string2;
            this.id = n2;
        }

        public int id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            BY_ID = ByIdMap.continuous(Operation::id, Operation.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Operation::id);
            CODEC = StringRepresentable.fromEnum(Operation::values);
        }
    }
}

