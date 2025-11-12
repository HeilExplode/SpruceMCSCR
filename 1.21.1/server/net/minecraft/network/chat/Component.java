/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonSerializationContext
 *  com.google.gson.JsonSerializer
 *  com.google.gson.stream.JsonReader
 *  com.mojang.brigadier.Message
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public ComponentContents getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int n2 = n - stringBuilder.length();
            if (n2 <= 0) {
                return STOP_ITERATION;
            }
            stringBuilder.append(string.length() <= n2 ? string : string.substring(0, n2));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Component> getSiblings();

    @Nullable
    default public String tryCollapseToString() {
        ComponentContents componentContents = this.getContents();
        if (componentContents instanceof PlainTextContents) {
            PlainTextContents plainTextContents = (PlainTextContents)componentContents;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return plainTextContents.text();
            }
        }
        return null;
    }

    default public MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default public MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<Component>(this.getSiblings()), this.getStyle());
    }

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.getContents().visit(styledContentConsumer, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(styledContentConsumer, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        Optional<T> optional = this.getContents().visit(contentConsumer);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(contentConsumer);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default public List<Component> toFlatList(Style style2) {
        ArrayList arrayList = Lists.newArrayList();
        this.visit((style, string) -> {
            if (!string.isEmpty()) {
                arrayList.add(Component.literal(string).withStyle(style));
            }
            return Optional.empty();
        }, style2);
        return arrayList;
    }

    default public boolean contains(Component component) {
        List<Component> list;
        if (this.equals(component)) {
            return true;
        }
        List<Component> list2 = this.toFlatList();
        return Collections.indexOfSubList(list2, list = component.toFlatList(this.getStyle())) != -1;
    }

    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? Component.literal(string) : CommonComponents.EMPTY;
    }

    public static MutableComponent literal(String string) {
        return MutableComponent.create(PlainTextContents.create(string));
    }

    public static MutableComponent translatable(String string) {
        return MutableComponent.create(new TranslatableContents(string, null, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatable(String string, Object ... objectArray) {
        return MutableComponent.create(new TranslatableContents(string, null, objectArray));
    }

    public static MutableComponent translatableEscape(String string, Object ... objectArray) {
        for (int i = 0; i < objectArray.length; ++i) {
            Object object = objectArray[i];
            if (TranslatableContents.isAllowedPrimitiveArgument(object) || object instanceof Component) continue;
            objectArray[i] = String.valueOf(object);
        }
        return Component.translatable(string, objectArray);
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2) {
        return MutableComponent.create(new TranslatableContents(string, string2, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2, Object ... objectArray) {
        return MutableComponent.create(new TranslatableContents(string, string2, objectArray));
    }

    public static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
    }

    public static MutableComponent keybind(String string) {
        return MutableComponent.create(new KeybindContents(string));
    }

    public static MutableComponent nbt(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
        return MutableComponent.create(new NbtContents(string, bl, optional, dataSource));
    }

    public static MutableComponent score(String string, String string2) {
        return MutableComponent.create(new ScoreContents(string, string2));
    }

    public static MutableComponent selector(String string, Optional<Component> optional) {
        return MutableComponent.create(new SelectorContents(string, optional));
    }

    public static Component translationArg(Date date) {
        return Component.literal(date.toString());
    }

    public static Component translationArg(Message message) {
        Component component;
        if (message instanceof Component) {
            Component component2 = (Component)message;
            component = component2;
        } else {
            component = Component.literal(message.getString());
        }
        return component;
    }

    public static Component translationArg(UUID uUID) {
        return Component.literal(uUID.toString());
    }

    public static Component translationArg(ResourceLocation resourceLocation) {
        return Component.literal(resourceLocation.toString());
    }

    public static Component translationArg(ChunkPos chunkPos) {
        return Component.literal(chunkPos.toString());
    }

    public static Component translationArg(URI uRI) {
        return Component.literal(uRI.toString());
    }

    public static class SerializerAdapter
    implements JsonDeserializer<MutableComponent>,
    JsonSerializer<Component> {
        private final HolderLookup.Provider registries;

        public SerializerAdapter(HolderLookup.Provider provider) {
            this.registries = provider;
        }

        public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Serializer.deserialize(jsonElement, this.registries);
        }

        public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
            return Serializer.serialize(component, this.registries);
        }

        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((Component)object, type, jsonSerializationContext);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }

    public static class Serializer {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        private Serializer() {
        }

        static MutableComponent deserialize(JsonElement jsonElement, HolderLookup.Provider provider) {
            return (MutableComponent)ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), (Object)jsonElement).getOrThrow(JsonParseException::new);
        }

        static JsonElement serialize(Component component, HolderLookup.Provider provider) {
            return (JsonElement)ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), (Object)component).getOrThrow(JsonParseException::new);
        }

        public static String toJson(Component component, HolderLookup.Provider provider) {
            return GSON.toJson(Serializer.serialize(component, provider));
        }

        @Nullable
        public static MutableComponent fromJson(String string, HolderLookup.Provider provider) {
            JsonElement jsonElement = JsonParser.parseString((String)string);
            if (jsonElement == null) {
                return null;
            }
            return Serializer.deserialize(jsonElement, provider);
        }

        @Nullable
        public static MutableComponent fromJson(@Nullable JsonElement jsonElement, HolderLookup.Provider provider) {
            if (jsonElement == null) {
                return null;
            }
            return Serializer.deserialize(jsonElement, provider);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String string, HolderLookup.Provider provider) {
            JsonReader jsonReader = new JsonReader((Reader)new StringReader(string));
            jsonReader.setLenient(true);
            JsonElement jsonElement = JsonParser.parseReader((JsonReader)jsonReader);
            if (jsonElement == null) {
                return null;
            }
            return Serializer.deserialize(jsonElement, provider);
        }
    }
}

