/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<Parameter> parameters, Style style) {
    public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey), (App)Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters), (App)Style.Serializer.CODEC.optionalFieldOf("style", (Object)Style.EMPTY).forGetter(ChatTypeDecoration::style)).apply((Applicative)instance, ChatTypeDecoration::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatTypeDecoration> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ChatTypeDecoration::translationKey, Parameter.STREAM_CODEC.apply(ByteBufCodecs.list()), ChatTypeDecoration::parameters, Style.Serializer.TRUSTED_STREAM_CODEC, ChatTypeDecoration::style, ChatTypeDecoration::new);

    public static ChatTypeDecoration withSender(String string) {
        return new ChatTypeDecoration(string, List.of(Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public static ChatTypeDecoration incomingDirectMessage(String string) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration(string, List.of(Parameter.SENDER, Parameter.CONTENT), style);
    }

    public static ChatTypeDecoration outgoingDirectMessage(String string) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration(string, List.of(Parameter.TARGET, Parameter.CONTENT), style);
    }

    public static ChatTypeDecoration teamMessage(String string) {
        return new ChatTypeDecoration(string, List.of(Parameter.TARGET, Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public Component decorate(Component component, ChatType.Bound bound) {
        Object[] objectArray = this.resolveParameters(component, bound);
        return Component.translatable(this.translationKey, objectArray).withStyle(this.style);
    }

    private Component[] resolveParameters(Component component, ChatType.Bound bound) {
        Component[] componentArray = new Component[this.parameters.size()];
        for (int i = 0; i < componentArray.length; ++i) {
            Parameter parameter = this.parameters.get(i);
            componentArray[i] = parameter.select(component, bound);
        }
        return componentArray;
    }

    public static enum Parameter implements StringRepresentable
    {
        SENDER(0, "sender", (component, bound) -> bound.name()),
        TARGET(1, "target", (component, bound) -> bound.targetName().orElse(CommonComponents.EMPTY)),
        CONTENT(2, "content", (component, bound) -> component);

        private static final IntFunction<Parameter> BY_ID;
        public static final Codec<Parameter> CODEC;
        public static final StreamCodec<ByteBuf, Parameter> STREAM_CODEC;
        private final int id;
        private final String name;
        private final Selector selector;

        private Parameter(int n2, String string2, Selector selector) {
            this.id = n2;
            this.name = string2;
            this.selector = selector;
        }

        public Component select(Component component, ChatType.Bound bound) {
            return this.selector.select(component, bound);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            BY_ID = ByIdMap.continuous(parameter -> parameter.id, Parameter.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            CODEC = StringRepresentable.fromEnum(Parameter::values);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, parameter -> parameter.id);
        }

        public static interface Selector {
            public Component select(Component var1, ChatType.Bound var2);
        }
    }
}

