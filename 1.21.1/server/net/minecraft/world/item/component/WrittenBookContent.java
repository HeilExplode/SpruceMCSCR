/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BookContent;

public record WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Component>> pages, boolean resolved) implements BookContent<Component, WrittenBookContent>
{
    public static final WrittenBookContent EMPTY = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), true);
    public static final int PAGE_LENGTH = Short.MAX_VALUE;
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int MAX_GENERATION = 3;
    public static final int MAX_CRAFTABLE_GENERATION = 2;
    public static final Codec<Component> CONTENT_CODEC = ComponentSerialization.flatCodec(Short.MAX_VALUE);
    public static final Codec<List<Filterable<Component>>> PAGES_CODEC = WrittenBookContent.pagesCodec(CONTENT_CODEC);
    public static final Codec<WrittenBookContent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Filterable.codec(Codec.string((int)0, (int)32)).fieldOf("title").forGetter(WrittenBookContent::title), (App)Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author), (App)ExtraCodecs.intRange(0, 3).optionalFieldOf("generation", (Object)0).forGetter(WrittenBookContent::generation), (App)PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WrittenBookContent::pages), (App)Codec.BOOL.optionalFieldOf("resolved", (Object)false).forGetter(WrittenBookContent::resolved)).apply((Applicative)instance, WrittenBookContent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WrittenBookContent> STREAM_CODEC = StreamCodec.composite(Filterable.streamCodec(ByteBufCodecs.stringUtf8(32)), WrittenBookContent::title, ByteBufCodecs.STRING_UTF8, WrittenBookContent::author, ByteBufCodecs.VAR_INT, WrittenBookContent::generation, Filterable.streamCodec(ComponentSerialization.STREAM_CODEC).apply(ByteBufCodecs.list()), WrittenBookContent::pages, ByteBufCodecs.BOOL, WrittenBookContent::resolved, WrittenBookContent::new);

    public WrittenBookContent {
        if (n < 0 || n > 3) {
            throw new IllegalArgumentException("Generation was " + n + ", but must be between 0 and 3");
        }
    }

    private static Codec<Filterable<Component>> pageCodec(Codec<Component> codec) {
        return Filterable.codec(codec);
    }

    public static Codec<List<Filterable<Component>>> pagesCodec(Codec<Component> codec) {
        return WrittenBookContent.pageCodec(codec).listOf();
    }

    @Nullable
    public WrittenBookContent tryCraftCopy() {
        if (this.generation >= 2) {
            return null;
        }
        return new WrittenBookContent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
    }

    @Nullable
    public WrittenBookContent resolve(CommandSourceStack commandSourceStack, @Nullable Player player) {
        if (this.resolved) {
            return null;
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)this.pages.size());
        for (Filterable<Component> filterable : this.pages) {
            Optional<Filterable<Component>> optional = WrittenBookContent.resolvePage(commandSourceStack, player, filterable);
            if (optional.isEmpty()) {
                return null;
            }
            builder.add(optional.get());
        }
        return new WrittenBookContent(this.title, this.author, this.generation, (List<Filterable<Component>>)builder.build(), true);
    }

    public WrittenBookContent markResolved() {
        return new WrittenBookContent(this.title, this.author, this.generation, this.pages, true);
    }

    private static Optional<Filterable<Component>> resolvePage(CommandSourceStack commandSourceStack, @Nullable Player player, Filterable<Component> filterable) {
        return filterable.resolve(component -> {
            try {
                MutableComponent mutableComponent = ComponentUtils.updateForEntity(commandSourceStack, component, (Entity)player, 0);
                if (WrittenBookContent.isPageTooLarge(mutableComponent, commandSourceStack.registryAccess())) {
                    return Optional.empty();
                }
                return Optional.of(mutableComponent);
            }
            catch (Exception exception) {
                return Optional.of(component);
            }
        });
    }

    private static boolean isPageTooLarge(Component component, HolderLookup.Provider provider) {
        return Component.Serializer.toJson(component, provider).length() > Short.MAX_VALUE;
    }

    public List<Component> getPages(boolean bl) {
        return Lists.transform(this.pages, filterable -> (Component)filterable.get(bl));
    }

    @Override
    public WrittenBookContent withReplacedPages(List<Filterable<Component>> list) {
        return new WrittenBookContent(this.title, this.author, this.generation, list, false);
    }

    @Override
    public /* synthetic */ Object withReplacedPages(List list) {
        return this.withReplacedPages(list);
    }
}

