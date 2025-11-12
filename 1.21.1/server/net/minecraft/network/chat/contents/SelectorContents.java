/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class SelectorContents
implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SelectorContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("selector").forGetter(SelectorContents::getPattern), (App)ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::getSeparator)).apply((Applicative)instance, SelectorContents::new));
    public static final ComponentContents.Type<SelectorContents> TYPE = new ComponentContents.Type<SelectorContents>(CODEC, "selector");
    private final String pattern;
    @Nullable
    private final EntitySelector selector;
    protected final Optional<Component> separator;

    public SelectorContents(String string, Optional<Component> optional) {
        this.pattern = string;
        this.separator = optional;
        this.selector = SelectorContents.parseSelector(string);
    }

    @Nullable
    private static EntitySelector parseSelector(String string) {
        EntitySelector entitySelector = null;
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string), true);
            entitySelector = entitySelectorParser.parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.warn("Invalid selector component: {}: {}", (Object)string, (Object)commandSyntaxException.getMessage());
        }
        return entitySelector;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        if (commandSourceStack == null || this.selector == null) {
            return Component.empty();
        }
        Optional<MutableComponent> optional = ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, n);
        return ComponentUtils.formatList(this.selector.findEntities(commandSourceStack), optional, Entity::getDisplayName);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style, this.pattern);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(this.pattern);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SelectorContents)) return false;
        SelectorContents selectorContents = (SelectorContents)object;
        if (!this.pattern.equals(selectorContents.pattern)) return false;
        if (!this.separator.equals(selectorContents.separator)) return false;
        return true;
    }

    public int hashCode() {
        int n = this.pattern.hashCode();
        n = 31 * n + this.separator.hashCode();
        return n;
    }

    public String toString() {
        return "pattern{" + this.pattern + "}";
    }
}

