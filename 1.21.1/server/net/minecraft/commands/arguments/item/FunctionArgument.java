/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FunctionArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.function.tag.unknown", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.function.unknown", object));

    public static FunctionArgument functions() {
        return new FunctionArgument();
    }

    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        if (stringReader.canRead() && stringReader.peek() == '#') {
            stringReader.skip();
            final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
            return new Result(){

                @Override
                public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return FunctionArgument.getFunctionTag(commandContext, resourceLocation);
                }

                @Override
                public Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return Pair.of((Object)resourceLocation, (Object)Either.right(FunctionArgument.getFunctionTag(commandContext, resourceLocation)));
                }

                @Override
                public Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                    return Pair.of((Object)resourceLocation, FunctionArgument.getFunctionTag(commandContext, resourceLocation));
                }
            };
        }
        final ResourceLocation resourceLocation = ResourceLocation.read(stringReader);
        return new Result(){

            @Override
            public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                return Collections.singleton(FunctionArgument.getFunction(commandContext, resourceLocation));
            }

            @Override
            public Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                return Pair.of((Object)resourceLocation, (Object)Either.left(FunctionArgument.getFunction(commandContext, resourceLocation)));
            }

            @Override
            public Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
                return Pair.of((Object)resourceLocation, Collections.singleton(FunctionArgument.getFunction(commandContext, resourceLocation)));
            }
        };
    }

    static CommandFunction<CommandSourceStack> getFunction(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
        return ((CommandSourceStack)commandContext.getSource()).getServer().getFunctions().get(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_FUNCTION.create((Object)resourceLocation.toString()));
    }

    static Collection<CommandFunction<CommandSourceStack>> getFunctionTag(CommandContext<CommandSourceStack> commandContext, ResourceLocation resourceLocation) throws CommandSyntaxException {
        Collection<CommandFunction<CommandSourceStack>> collection = ((CommandSourceStack)commandContext.getSource()).getServer().getFunctions().getTag(resourceLocation);
        if (collection == null) {
            throw ERROR_UNKNOWN_TAG.create((Object)resourceLocation.toString());
        }
        return collection;
    }

    public static Collection<CommandFunction<CommandSourceStack>> getFunctions(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((Result)commandContext.getArgument(string, Result.class)).create(commandContext);
    }

    public static Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> getFunctionOrTag(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((Result)commandContext.getArgument(string, Result.class)).unwrap(commandContext);
    }

    public static Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> getFunctionCollection(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ((Result)commandContext.getArgument(string, Result.class)).unwrapToCollection(commandContext);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result {
        public Collection<CommandFunction<CommandSourceStack>> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public Pair<ResourceLocation, Either<CommandFunction<CommandSourceStack>, Collection<CommandFunction<CommandSourceStack>>>> unwrap(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public Pair<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> unwrapToCollection(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }
}

