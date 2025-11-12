/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.util.StringRepresentable;

public interface DataSource {
    public static final MapCodec<DataSource> CODEC = ComponentSerialization.createLegacyComponentMatcher((StringRepresentable[])new Type[]{EntityDataSource.TYPE, BlockDataSource.TYPE, StorageDataSource.TYPE}, Type::codec, DataSource::type, (String)"source");

    public Stream<CompoundTag> getData(CommandSourceStack var1) throws CommandSyntaxException;

    public Type<?> type();

    public record Type<T extends DataSource>(MapCodec<T> codec, String id) implements StringRepresentable
    {
        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}

