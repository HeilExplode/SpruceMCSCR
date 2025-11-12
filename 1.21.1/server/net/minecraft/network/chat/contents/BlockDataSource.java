/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource
{
    public static final MapCodec<BlockDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply((Applicative)instance, BlockDataSource::new));
    public static final DataSource.Type<BlockDataSource> TYPE = new DataSource.Type<BlockDataSource>(SUB_CODEC, "block");

    public BlockDataSource(String string) {
        this(string, BlockDataSource.compilePos(string));
    }

    @Nullable
    private static Coordinates compilePos(String string) {
        try {
            return BlockPosArgument.blockPos().parse(new StringReader(string));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
        BlockEntity blockEntity;
        BlockPos blockPos;
        ServerLevel serverLevel;
        if (this.compiledPos != null && (serverLevel = commandSourceStack.getLevel()).isLoaded(blockPos = this.compiledPos.getBlockPos(commandSourceStack)) && (blockEntity = serverLevel.getBlockEntity(blockPos)) != null) {
            return Stream.of(blockEntity.saveWithFullMetadata(commandSourceStack.registryAccess()));
        }
        return Stream.empty();
    }

    @Override
    public DataSource.Type<?> type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "block=" + this.posPattern;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof BlockDataSource)) return false;
        BlockDataSource blockDataSource = (BlockDataSource)object;
        if (!this.posPattern.equals(blockDataSource.posPattern)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.posPattern.hashCode();
    }
}

