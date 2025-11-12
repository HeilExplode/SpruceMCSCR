/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
    public static final String DEFAULT_SEPARATOR_TEXT = ", ";
    public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
    public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

    public static MutableComponent mergeStyles(MutableComponent mutableComponent, Style style) {
        if (style.isEmpty()) {
            return mutableComponent;
        }
        Style style2 = mutableComponent.getStyle();
        if (style2.isEmpty()) {
            return mutableComponent.setStyle(style);
        }
        if (style2.equals(style)) {
            return mutableComponent;
        }
        return mutableComponent.setStyle(style2.applyTo(style));
    }

    public static Optional<MutableComponent> updateForEntity(@Nullable CommandSourceStack commandSourceStack, Optional<Component> optional, @Nullable Entity entity, int n) throws CommandSyntaxException {
        return optional.isPresent() ? Optional.of(ComponentUtils.updateForEntity(commandSourceStack, optional.get(), entity, n)) : Optional.empty();
    }

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int n) throws CommandSyntaxException {
        if (n > 100) {
            return component.copy();
        }
        MutableComponent mutableComponent = component.getContents().resolve(commandSourceStack, entity, n + 1);
        for (Component component2 : component.getSiblings()) {
            mutableComponent.append(ComponentUtils.updateForEntity(commandSourceStack, component2, entity, n + 1));
        }
        return mutableComponent.withStyle(ComponentUtils.resolveStyle(commandSourceStack, component.getStyle(), entity, n));
    }

    private static Style resolveStyle(@Nullable CommandSourceStack commandSourceStack, Style style, @Nullable Entity entity, int n) throws CommandSyntaxException {
        Component component;
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent != null && (component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT)) != null) {
            HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.updateForEntity(commandSourceStack, component, entity, n + 1));
            return style.withHoverEvent(hoverEvent2);
        }
        return style;
    }

    public static Component formatList(Collection<String> collection) {
        return ComponentUtils.formatAndSortList(collection, string -> Component.literal(string).withStyle(ChatFormatting.GREEN));
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return CommonComponents.EMPTY;
        }
        if (collection.size() == 1) {
            return function.apply((Comparable)collection.iterator().next());
        }
        ArrayList arrayList = Lists.newArrayList(collection);
        arrayList.sort(Comparable::compareTo);
        return ComponentUtils.formatList(arrayList, function);
    }

    public static <T> Component formatList(Collection<? extends T> collection, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, DEFAULT_SEPARATOR, function);
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Optional<? extends Component> optional, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, (Component)DataFixUtils.orElse(optional, (Object)DEFAULT_SEPARATOR), function);
    }

    public static Component formatList(Collection<? extends Component> collection, Component component) {
        return ComponentUtils.formatList(collection, component, Function.identity());
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Component component, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return Component.empty();
        }
        if (collection.size() == 1) {
            return function.apply(collection.iterator().next()).copy();
        }
        MutableComponent mutableComponent = Component.empty();
        boolean bl = true;
        for (T t : collection) {
            if (!bl) {
                mutableComponent.append(component);
            }
            mutableComponent.append(function.apply(t));
            bl = false;
        }
        return mutableComponent;
    }

    public static MutableComponent wrapInSquareBrackets(Component component) {
        return Component.translatable("chat.square_brackets", component);
    }

    public static Component fromMessage(Message message) {
        if (message instanceof Component) {
            Component component = (Component)message;
            return component;
        }
        return Component.literal(message.getString());
    }

    public static boolean isTranslationResolvable(@Nullable Component component) {
        Object object;
        if (component != null && (object = component.getContents()) instanceof TranslatableContents) {
            TranslatableContents translatableContents = (TranslatableContents)object;
            object = translatableContents.getKey();
            String string = translatableContents.getFallback();
            return string != null || Language.getInstance().has((String)object);
        }
        return true;
    }

    public static MutableComponent copyOnClickText(String string) {
        return ComponentUtils.wrapInSquareBrackets(Component.literal(string).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, string)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))).withInsertion(string)));
    }
}

