/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ParticleArgument
implements ArgumentType<ParticleOptions> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "particle{foo:bar}");
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_PARTICLE = new DynamicCommandExceptionType(object -> Component.translatableEscape("particle.notFound", object));
    public static final DynamicCommandExceptionType ERROR_INVALID_OPTIONS = new DynamicCommandExceptionType(object -> Component.translatableEscape("particle.invalidOptions", object));
    private final HolderLookup.Provider registries;

    public ParticleArgument(CommandBuildContext commandBuildContext) {
        this.registries = commandBuildContext;
    }

    public static ParticleArgument particle(CommandBuildContext commandBuildContext) {
        return new ParticleArgument(commandBuildContext);
    }

    public static ParticleOptions getParticle(CommandContext<CommandSourceStack> commandContext, String string) {
        return (ParticleOptions)commandContext.getArgument(string, ParticleOptions.class);
    }

    public ParticleOptions parse(StringReader stringReader) throws CommandSyntaxException {
        return ParticleArgument.readParticle(stringReader, this.registries);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static ParticleOptions readParticle(StringReader stringReader, HolderLookup.Provider provider) throws CommandSyntaxException {
        ParticleType<?> particleType = ParticleArgument.readParticleType(stringReader, provider.lookupOrThrow(Registries.PARTICLE_TYPE));
        return ParticleArgument.readParticle(stringReader, particleType, provider);
    }

    private static ParticleType<?> readParticleType(StringReader stringReader, HolderLookup<ParticleType<?>> holderLookup) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        ResourceKey<ParticleType<?>> resourceKey = ResourceKey.create(Registries.PARTICLE_TYPE, resourceLocation);
        return holderLookup.get(resourceKey).orElseThrow(() -> ERROR_UNKNOWN_PARTICLE.createWithContext((ImmutableStringReader)stringReader, (Object)resourceLocation)).value();
    }

    private static <T extends ParticleOptions> T readParticle(StringReader stringReader, ParticleType<T> particleType, HolderLookup.Provider provider) throws CommandSyntaxException {
        CompoundTag compoundTag = stringReader.canRead() && stringReader.peek() == '{' ? new TagParser(stringReader).readStruct() : new CompoundTag();
        return (T)((ParticleOptions)particleType.codec().codec().parse(provider.createSerializationContext(NbtOps.INSTANCE), (Object)compoundTag).getOrThrow(arg_0 -> ((DynamicCommandExceptionType)ERROR_INVALID_OPTIONS).create(arg_0)));
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        HolderLookup.RegistryLookup<ParticleType<?>> registryLookup = this.registries.lookupOrThrow(Registries.PARTICLE_TYPE);
        return SharedSuggestionProvider.suggestResource(registryLookup.listElementIds().map(ResourceKey::location), suggestionsBuilder);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

