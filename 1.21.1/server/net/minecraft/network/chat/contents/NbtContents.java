/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
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
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class NbtContents
implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<NbtContents> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("nbt").forGetter(NbtContents::getNbtPath), (App)Codec.BOOL.lenientOptionalFieldOf("interpret", (Object)false).forGetter(NbtContents::isInterpreting), (App)ComponentSerialization.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtContents::getSeparator), (App)DataSource.CODEC.forGetter(NbtContents::getDataSource)).apply((Applicative)instance, NbtContents::new));
    public static final ComponentContents.Type<NbtContents> TYPE = new ComponentContents.Type<NbtContents>(CODEC, "nbt");
    private final boolean interpreting;
    private final Optional<Component> separator;
    private final String nbtPathPattern;
    private final DataSource dataSource;
    @Nullable
    protected final NbtPathArgument.NbtPath compiledNbtPath;

    public NbtContents(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
        this(string, NbtContents.compileNbtPath(string), bl, optional, dataSource);
    }

    private NbtContents(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, Optional<Component> optional, DataSource dataSource) {
        this.nbtPathPattern = string;
        this.compiledNbtPath = nbtPath;
        this.interpreting = bl;
        this.separator = optional;
        this.dataSource = dataSource;
    }

    @Nullable
    private static NbtPathArgument.NbtPath compileNbtPath(String string) {
        try {
            return new NbtPathArgument().parse(new StringReader(string));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public String getNbtPath() {
        return this.nbtPathPattern;
    }

    public boolean isInterpreting() {
        return this.interpreting;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof NbtContents)) return false;
        NbtContents nbtContents = (NbtContents)object;
        if (!this.dataSource.equals(nbtContents.dataSource)) return false;
        if (!this.separator.equals(nbtContents.separator)) return false;
        if (this.interpreting != nbtContents.interpreting) return false;
        if (!this.nbtPathPattern.equals(nbtContents.nbtPathPattern)) return false;
        return true;
    }

    public int hashCode() {
        int n = this.interpreting ? 1 : 0;
        n = 31 * n + this.separator.hashCode();
        n = 31 * n + this.nbtPathPattern.hashCode();
        n = 31 * n + this.dataSource.hashCode();
        return n;
    }

    public String toString() {
        return "nbt{" + String.valueOf(this.dataSource) + ", interpreting=" + this.interpreting + ", separator=" + String.valueOf(this.separator) + "}";
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        if (commandSourceStack == null || this.compiledNbtPath == null) {
            return Component.empty();
        }
        Stream<String> stream = this.dataSource.getData(commandSourceStack).flatMap(compoundTag -> {
            try {
                return this.compiledNbtPath.get((Tag)compoundTag).stream();
            }
            catch (CommandSyntaxException commandSyntaxException) {
                return Stream.empty();
            }
        }).map(Tag::getAsString);
        if (this.interpreting) {
            Component component = (Component)DataFixUtils.orElse(ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, n), (Object)ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR);
            return stream.flatMap(string -> {
                try {
                    MutableComponent mutableComponent = Component.Serializer.fromJson(string, (HolderLookup.Provider)commandSourceStack.registryAccess());
                    return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, mutableComponent, entity, n));
                }
                catch (Exception exception) {
                    LOGGER.warn("Failed to parse component: {}", string, (Object)exception);
                    return Stream.of(new MutableComponent[0]);
                }
            }).reduce((mutableComponent, mutableComponent2) -> mutableComponent.append(component).append((Component)mutableComponent2)).orElseGet(Component::empty);
        }
        return ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, n).map(mutableComponent -> stream.map(Component::literal).reduce((mutableComponent2, mutableComponent3) -> mutableComponent2.append((Component)mutableComponent).append((Component)mutableComponent3)).orElseGet(Component::empty)).orElseGet(() -> Component.literal(stream.collect(Collectors.joining(", "))));
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }
}

