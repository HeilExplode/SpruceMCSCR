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
package net.minecraft.network.chat.contents;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public interface PlainTextContents
extends ComponentContents {
    public static final MapCodec<PlainTextContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("text").forGetter(PlainTextContents::text)).apply((Applicative)instance, PlainTextContents::create));
    public static final ComponentContents.Type<PlainTextContents> TYPE = new ComponentContents.Type<PlainTextContents>(CODEC, "text");
    public static final PlainTextContents EMPTY = new PlainTextContents(){

        public String toString() {
            return "empty";
        }

        @Override
        public String text() {
            return "";
        }
    };

    public static PlainTextContents create(String string) {
        return string.isEmpty() ? EMPTY : new LiteralContents(string);
    }

    public String text();

    @Override
    default public ComponentContents.Type<?> type() {
        return TYPE;
    }

    public record LiteralContents(String text) implements PlainTextContents
    {
        @Override
        public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
            return contentConsumer.accept(this.text);
        }

        @Override
        public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
            return styledContentConsumer.accept(style, this.text);
        }

        @Override
        public String toString() {
            return "literal{" + this.text + "}";
        }
    }
}

