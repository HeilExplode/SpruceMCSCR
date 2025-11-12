/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

public interface ComponentContents {
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return Optional.empty();
    }

    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return Optional.empty();
    }

    default public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        return MutableComponent.create(this);
    }

    public Type<?> type();

    public record Type<T extends ComponentContents>(MapCodec<T> codec, String id) implements StringRepresentable
    {
        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}

