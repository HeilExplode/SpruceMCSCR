/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public class ResourceLocationArgument
implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("advancement.advancementNotFound", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(object -> Component.translatableEscape("recipe.notFound", object));

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static AdvancementHolder getAdvancement(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        AdvancementHolder advancementHolder = ((CommandSourceStack)commandContext.getSource()).getServer().getAdvancements().get(resourceLocation);
        if (advancementHolder == null) {
            throw ERROR_UNKNOWN_ADVANCEMENT.create((Object)resourceLocation);
        }
        return advancementHolder;
    }

    public static RecipeHolder<?> getRecipe(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        RecipeManager recipeManager = ((CommandSourceStack)commandContext.getSource()).getServer().getRecipeManager();
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        return recipeManager.byKey(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create((Object)resourceLocation));
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> commandContext, String string) {
        return (ResourceLocation)commandContext.getArgument(string, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return ResourceLocation.read(stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

