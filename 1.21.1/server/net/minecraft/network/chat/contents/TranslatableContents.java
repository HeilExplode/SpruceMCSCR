/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableFormatException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

public class TranslatableContents
implements ComponentContents {
    public static final Object[] NO_ARGS = new Object[0];
    private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.JAVA.validate(TranslatableContents::filterAllowedArguments);
    private static final Codec<Object> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC).xmap(either -> either.map(object -> object, component -> Objects.requireNonNullElse(component.tryCollapseToString(), component)), object -> {
        Either either;
        if (object instanceof Component) {
            Component component = (Component)object;
            either = Either.right((Object)component);
        } else {
            either = Either.left((Object)object);
        }
        return either;
    });
    public static final MapCodec<TranslatableContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("translate").forGetter(translatableContents -> translatableContents.key), (App)Codec.STRING.lenientOptionalFieldOf("fallback").forGetter(translatableContents -> Optional.ofNullable(translatableContents.fallback)), (App)ARG_CODEC.listOf().optionalFieldOf("with").forGetter(translatableContents -> TranslatableContents.adjustArgs(translatableContents.args))).apply((Applicative)instance, TranslatableContents::create));
    public static final ComponentContents.Type<TranslatableContents> TYPE = new ComponentContents.Type<TranslatableContents>(CODEC, "translatable");
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    @Nullable
    private final String fallback;
    private final Object[] args;
    @Nullable
    private Language decomposedWith;
    private List<FormattedText> decomposedParts = ImmutableList.of();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static DataResult<Object> filterAllowedArguments(@Nullable Object object) {
        if (!TranslatableContents.isAllowedPrimitiveArgument(object)) {
            return DataResult.error(() -> "This value needs to be parsed as component");
        }
        return DataResult.success((Object)object);
    }

    public static boolean isAllowedPrimitiveArgument(@Nullable Object object) {
        return object instanceof Number || object instanceof Boolean || object instanceof String;
    }

    private static Optional<List<Object>> adjustArgs(Object[] objectArray) {
        return objectArray.length == 0 ? Optional.empty() : Optional.of(Arrays.asList(objectArray));
    }

    private static Object[] adjustArgs(Optional<List<Object>> optional) {
        return optional.map(list -> list.isEmpty() ? NO_ARGS : list.toArray()).orElse(NO_ARGS);
    }

    private static TranslatableContents create(String string, Optional<String> optional, Optional<List<Object>> optional2) {
        return new TranslatableContents(string, optional.orElse(null), TranslatableContents.adjustArgs(optional2));
    }

    public TranslatableContents(String string, @Nullable String string2, Object[] objectArray) {
        this.key = string;
        this.fallback = string2;
        this.args = objectArray;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    private void decompose() {
        Language language = Language.getInstance();
        if (language == this.decomposedWith) {
            return;
        }
        this.decomposedWith = language;
        String string = this.fallback != null ? language.getOrDefault(this.key, this.fallback) : language.getOrDefault(this.key);
        try {
            ImmutableList.Builder builder = ImmutableList.builder();
            this.decomposeTemplate(string, arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
            this.decomposedParts = builder.build();
        }
        catch (TranslatableFormatException translatableFormatException) {
            this.decomposedParts = ImmutableList.of((Object)FormattedText.of(string));
        }
    }

    private void decomposeTemplate(String string, Consumer<FormattedText> consumer) {
        Matcher matcher = FORMAT_PATTERN.matcher(string);
        try {
            int n = 0;
            int n2 = 0;
            while (matcher.find(n2)) {
                String string2;
                int n3 = matcher.start();
                int n4 = matcher.end();
                if (n3 > n2) {
                    string2 = string.substring(n2, n3);
                    if (string2.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    consumer.accept(FormattedText.of(string2));
                }
                string2 = matcher.group(2);
                String string3 = string.substring(n3, n4);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    consumer.accept(TEXT_PERCENT);
                } else if ("s".equals(string2)) {
                    String string4 = matcher.group(1);
                    int n5 = string4 != null ? Integer.parseInt(string4) - 1 : n++;
                    consumer.accept(this.getArgument(n5));
                } else {
                    throw new TranslatableFormatException(this, "Unsupported format: '" + string3 + "'");
                }
                n2 = n4;
            }
            if (n2 < string.length()) {
                String string5 = string.substring(n2);
                if (string5.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                consumer.accept(FormattedText.of(string5));
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            throw new TranslatableFormatException(this, (Throwable)illegalArgumentException);
        }
    }

    private FormattedText getArgument(int n) {
        if (n < 0 || n >= this.args.length) {
            throw new TranslatableFormatException(this, n);
        }
        Object object = this.args[n];
        if (object instanceof Component) {
            Component component = (Component)object;
            return component;
        }
        return object == null ? TEXT_NULL : FormattedText.of(object.toString());
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(styledContentConsumer, style);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(contentConsumer);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        Object[] objectArray = new Object[this.args.length];
        for (int i = 0; i < objectArray.length; ++i) {
            Object object = this.args[i];
            if (object instanceof Component) {
                Component component = (Component)object;
                objectArray[i] = ComponentUtils.updateForEntity(commandSourceStack, component, entity, n);
                continue;
            }
            objectArray[i] = object;
        }
        return MutableComponent.create(new TranslatableContents(this.key, this.fallback, objectArray));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TranslatableContents)) return false;
        TranslatableContents translatableContents = (TranslatableContents)object;
        if (!Objects.equals(this.key, translatableContents.key)) return false;
        if (!Objects.equals(this.fallback, translatableContents.fallback)) return false;
        if (!Arrays.equals(this.args, translatableContents.args)) return false;
        return true;
    }

    public int hashCode() {
        int n = Objects.hashCode(this.key);
        n = 31 * n + Objects.hashCode(this.fallback);
        n = 31 * n + Arrays.hashCode(this.args);
        return n;
    }

    public String toString() {
        return "translation{key='" + this.key + "'" + (String)(this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
    }

    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }
}

