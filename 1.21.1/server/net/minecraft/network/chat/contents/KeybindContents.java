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
package net.minecraft.network.chat.contents;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindResolver;

public class KeybindContents
implements ComponentContents {
    public static final MapCodec<KeybindContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("keybind").forGetter(keybindContents -> keybindContents.name)).apply((Applicative)instance, KeybindContents::new));
    public static final ComponentContents.Type<KeybindContents> TYPE = new ComponentContents.Type<KeybindContents>(CODEC, "keybind");
    private final String name;
    @Nullable
    private Supplier<Component> nameResolver;

    public KeybindContents(String string) {
        this.name = string;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
        }
        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return this.getNestedComponent().visit(contentConsumer);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return this.getNestedComponent().visit(styledContentConsumer, style);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof KeybindContents)) return false;
        KeybindContents keybindContents = (KeybindContents)object;
        if (!this.name.equals(keybindContents.name)) return false;
        return true;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return "keybind{" + this.name + "}";
    }

    public String getName() {
        return this.name;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }
}

