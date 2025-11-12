/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    public static final ResourceLocation DEFAULT_FONT = ResourceLocation.withDefaultNamespace("default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final ResourceLocation font;

    private static Style create(Optional<TextColor> optional, Optional<Boolean> optional2, Optional<Boolean> optional3, Optional<Boolean> optional4, Optional<Boolean> optional5, Optional<Boolean> optional6, Optional<ClickEvent> optional7, Optional<HoverEvent> optional8, Optional<String> optional9, Optional<ResourceLocation> optional10) {
        Style style = new Style(optional.orElse(null), optional2.orElse(null), optional3.orElse(null), optional4.orElse(null), optional5.orElse(null), optional6.orElse(null), optional7.orElse(null), optional8.orElse(null), optional9.orElse(null), optional10.orElse(null));
        if (style.equals(EMPTY)) {
            return EMPTY;
        }
        return style;
    }

    private Style(@Nullable TextColor textColor, @Nullable Boolean bl, @Nullable Boolean bl2, @Nullable Boolean bl3, @Nullable Boolean bl4, @Nullable Boolean bl5, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String string, @Nullable ResourceLocation resourceLocation) {
        this.color = textColor;
        this.bold = bl;
        this.italic = bl2;
        this.underlined = bl3;
        this.strikethrough = bl4;
        this.obfuscated = bl5;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = string;
        this.font = resourceLocation;
    }

    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public ResourceLocation getFont() {
        return this.font != null ? this.font : DEFAULT_FONT;
    }

    private static <T> Style checkEmptyAfterChange(Style style, @Nullable T t, @Nullable T t2) {
        if (t != null && t2 == null && style.equals(EMPTY)) {
            return EMPTY;
        }
        return style;
    }

    public Style withColor(@Nullable TextColor textColor) {
        if (Objects.equals(this.color, textColor)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(textColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.color, textColor);
    }

    public Style withColor(@Nullable ChatFormatting chatFormatting) {
        return this.withColor(chatFormatting != null ? TextColor.fromLegacyFormat(chatFormatting) : null);
    }

    public Style withColor(int n) {
        return this.withColor(TextColor.fromRgb(n));
    }

    public Style withBold(@Nullable Boolean bl) {
        if (Objects.equals(this.bold, bl)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, bl, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.bold, bl);
    }

    public Style withItalic(@Nullable Boolean bl) {
        if (Objects.equals(this.italic, bl)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, bl, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.italic, bl);
    }

    public Style withUnderlined(@Nullable Boolean bl) {
        if (Objects.equals(this.underlined, bl)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, bl, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.underlined, bl);
    }

    public Style withStrikethrough(@Nullable Boolean bl) {
        if (Objects.equals(this.strikethrough, bl)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, bl, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.strikethrough, bl);
    }

    public Style withObfuscated(@Nullable Boolean bl) {
        if (Objects.equals(this.obfuscated, bl)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, bl, this.clickEvent, this.hoverEvent, this.insertion, this.font), this.obfuscated, bl);
    }

    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        if (Objects.equals(this.clickEvent, clickEvent)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font), this.clickEvent, clickEvent);
    }

    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        if (Objects.equals(this.hoverEvent, hoverEvent)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font), this.hoverEvent, hoverEvent);
    }

    public Style withInsertion(@Nullable String string) {
        if (Objects.equals(this.insertion, string)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, string, this.font), this.insertion, string);
    }

    public Style withFont(@Nullable ResourceLocation resourceLocation) {
        if (Objects.equals(this.font, resourceLocation)) {
            return this;
        }
        return Style.checkEmptyAfterChange(new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, resourceLocation), this.font, resourceLocation);
    }

    public Style applyFormat(ChatFormatting chatFormatting) {
        TextColor textColor = this.color;
        Boolean bl = this.bold;
        Boolean bl2 = this.italic;
        Boolean bl3 = this.strikethrough;
        Boolean bl4 = this.underlined;
        Boolean bl5 = this.obfuscated;
        switch (chatFormatting) {
            case OBFUSCATED: {
                bl5 = true;
                break;
            }
            case BOLD: {
                bl = true;
                break;
            }
            case STRIKETHROUGH: {
                bl3 = true;
                break;
            }
            case UNDERLINE: {
                bl4 = true;
                break;
            }
            case ITALIC: {
                bl2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                textColor = TextColor.fromLegacyFormat(chatFormatting);
            }
        }
        return new Style(textColor, bl, bl2, bl4, bl3, bl5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyLegacyFormat(ChatFormatting chatFormatting) {
        TextColor textColor = this.color;
        Boolean bl = this.bold;
        Boolean bl2 = this.italic;
        Boolean bl3 = this.strikethrough;
        Boolean bl4 = this.underlined;
        Boolean bl5 = this.obfuscated;
        switch (chatFormatting) {
            case OBFUSCATED: {
                bl5 = true;
                break;
            }
            case BOLD: {
                bl = true;
                break;
            }
            case STRIKETHROUGH: {
                bl3 = true;
                break;
            }
            case UNDERLINE: {
                bl4 = true;
                break;
            }
            case ITALIC: {
                bl2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                bl5 = false;
                bl = false;
                bl3 = false;
                bl4 = false;
                bl2 = false;
                textColor = TextColor.fromLegacyFormat(chatFormatting);
            }
        }
        return new Style(textColor, bl, bl2, bl4, bl3, bl5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyFormats(ChatFormatting ... chatFormattingArray) {
        TextColor textColor = this.color;
        Boolean bl = this.bold;
        Boolean bl2 = this.italic;
        Boolean bl3 = this.strikethrough;
        Boolean bl4 = this.underlined;
        Boolean bl5 = this.obfuscated;
        block8: for (ChatFormatting chatFormatting : chatFormattingArray) {
            switch (chatFormatting) {
                case OBFUSCATED: {
                    bl5 = true;
                    continue block8;
                }
                case BOLD: {
                    bl = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    bl3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    bl4 = true;
                    continue block8;
                }
                case ITALIC: {
                    bl2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    textColor = TextColor.fromLegacyFormat(chatFormatting);
                }
            }
        }
        return new Style(textColor, bl, bl2, bl4, bl3, bl5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyTo(Style style) {
        if (this == EMPTY) {
            return style;
        }
        if (style == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : style.color, this.bold != null ? this.bold : style.bold, this.italic != null ? this.italic : style.italic, this.underlined != null ? this.underlined : style.underlined, this.strikethrough != null ? this.strikethrough : style.strikethrough, this.obfuscated != null ? this.obfuscated : style.obfuscated, this.clickEvent != null ? this.clickEvent : style.clickEvent, this.hoverEvent != null ? this.hoverEvent : style.hoverEvent, this.insertion != null ? this.insertion : style.insertion, this.font != null ? this.font : style.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Collector {
            private boolean isNotFirst;

            Collector() {
            }

            private void prependSeparator() {
                if (this.isNotFirst) {
                    stringBuilder.append(',');
                }
                this.isNotFirst = true;
            }

            void addFlagString(String string, @Nullable Boolean bl) {
                if (bl != null) {
                    this.prependSeparator();
                    if (!bl.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(string);
                }
            }

            void addValueString(String string, @Nullable Object object) {
                if (object != null) {
                    this.prependSeparator();
                    stringBuilder.append(string);
                    stringBuilder.append('=');
                    stringBuilder.append(object);
                }
            }
        }
        Collector collector = new Collector();
        collector.addValueString("color", this.color);
        collector.addFlagString("bold", this.bold);
        collector.addFlagString("italic", this.italic);
        collector.addFlagString("underlined", this.underlined);
        collector.addFlagString("strikethrough", this.strikethrough);
        collector.addFlagString("obfuscated", this.obfuscated);
        collector.addValueString("clickEvent", this.clickEvent);
        collector.addValueString("hoverEvent", this.hoverEvent);
        collector.addValueString("insertion", this.insertion);
        collector.addValueString("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Style) {
            Style style = (Style)object;
            return this.bold == style.bold && Objects.equals(this.getColor(), style.getColor()) && this.italic == style.italic && this.obfuscated == style.obfuscated && this.strikethrough == style.strikethrough && this.underlined == style.underlined && Objects.equals(this.clickEvent, style.clickEvent) && Objects.equals(this.hoverEvent, style.hoverEvent) && Objects.equals(this.insertion, style.insertion) && Objects.equals(this.font, style.font);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Serializer {
        public static final MapCodec<Style> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TextColor.CODEC.optionalFieldOf("color").forGetter(style -> Optional.ofNullable(style.color)), (App)Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)), (App)Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic)), (App)Codec.BOOL.optionalFieldOf("underlined").forGetter(style -> Optional.ofNullable(style.underlined)), (App)Codec.BOOL.optionalFieldOf("strikethrough").forGetter(style -> Optional.ofNullable(style.strikethrough)), (App)Codec.BOOL.optionalFieldOf("obfuscated").forGetter(style -> Optional.ofNullable(style.obfuscated)), (App)ClickEvent.CODEC.optionalFieldOf("clickEvent").forGetter(style -> Optional.ofNullable(style.clickEvent)), (App)HoverEvent.CODEC.optionalFieldOf("hoverEvent").forGetter(style -> Optional.ofNullable(style.hoverEvent)), (App)Codec.STRING.optionalFieldOf("insertion").forGetter(style -> Optional.ofNullable(style.insertion)), (App)ResourceLocation.CODEC.optionalFieldOf("font").forGetter(style -> Optional.ofNullable(style.font))).apply((Applicative)instance, Style::create));
        public static final Codec<Style> CODEC = MAP_CODEC.codec();
        public static final StreamCodec<RegistryFriendlyByteBuf, Style> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    }
}

