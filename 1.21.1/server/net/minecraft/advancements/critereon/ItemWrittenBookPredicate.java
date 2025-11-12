/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.critereon;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.CollectionPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public record ItemWrittenBookPredicate(Optional<CollectionPredicate<Filterable<Component>, PagePredicate>> pages, Optional<String> author, Optional<String> title, MinMaxBounds.Ints generation, Optional<Boolean> resolved) implements SingleComponentItemPredicate<WrittenBookContent>
{
    public static final Codec<ItemWrittenBookPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CollectionPredicate.codec(PagePredicate.CODEC).optionalFieldOf("pages").forGetter(ItemWrittenBookPredicate::pages), (App)Codec.STRING.optionalFieldOf("author").forGetter(ItemWrittenBookPredicate::author), (App)Codec.STRING.optionalFieldOf("title").forGetter(ItemWrittenBookPredicate::title), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("generation", (Object)MinMaxBounds.Ints.ANY).forGetter(ItemWrittenBookPredicate::generation), (App)Codec.BOOL.optionalFieldOf("resolved").forGetter(ItemWrittenBookPredicate::resolved)).apply((Applicative)instance, ItemWrittenBookPredicate::new));

    @Override
    public DataComponentType<WrittenBookContent> componentType() {
        return DataComponents.WRITTEN_BOOK_CONTENT;
    }

    @Override
    public boolean matches(ItemStack itemStack, WrittenBookContent writtenBookContent) {
        if (this.author.isPresent() && !this.author.get().equals(writtenBookContent.author())) {
            return false;
        }
        if (this.title.isPresent() && !this.title.get().equals(writtenBookContent.title().raw())) {
            return false;
        }
        if (!this.generation.matches(writtenBookContent.generation())) {
            return false;
        }
        if (this.resolved.isPresent() && this.resolved.get().booleanValue() != writtenBookContent.resolved()) {
            return false;
        }
        return !this.pages.isPresent() || this.pages.get().test(writtenBookContent.pages());
    }

    public record PagePredicate(Component contents) implements Predicate<Filterable<Component>>
    {
        public static final Codec<PagePredicate> CODEC = ComponentSerialization.CODEC.xmap(PagePredicate::new, PagePredicate::contents);

        @Override
        public boolean test(Filterable<Component> filterable) {
            return filterable.raw().equals(this.contents);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((Filterable)object);
        }
    }
}

