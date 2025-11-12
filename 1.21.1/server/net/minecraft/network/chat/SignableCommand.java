/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.context.CommandContextBuilder
 *  com.mojang.brigadier.context.ParsedArgument
 *  com.mojang.brigadier.context.ParsedCommandNode
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.SignedArgument;

public record SignableCommand<S>(List<Argument<S>> arguments) {
    public static <S> boolean hasSignableArguments(ParseResults<S> parseResults) {
        return !SignableCommand.of(parseResults).arguments().isEmpty();
    }

    public static <S> SignableCommand<S> of(ParseResults<S> parseResults) {
        CommandContextBuilder commandContextBuilder;
        CommandContextBuilder commandContextBuilder2;
        String string = parseResults.getReader().getString();
        CommandContextBuilder commandContextBuilder3 = commandContextBuilder2 = parseResults.getContext();
        List<Argument<S>> list = SignableCommand.collectArguments(string, commandContextBuilder3);
        while ((commandContextBuilder = commandContextBuilder3.getChild()) != null && commandContextBuilder.getRootNode() != commandContextBuilder2.getRootNode()) {
            list.addAll(SignableCommand.collectArguments(string, commandContextBuilder));
            commandContextBuilder3 = commandContextBuilder;
        }
        return new SignableCommand<S>(list);
    }

    private static <S> List<Argument<S>> collectArguments(String string, CommandContextBuilder<S> commandContextBuilder) {
        ArrayList<Argument<S>> arrayList = new ArrayList<Argument<S>>();
        for (ParsedCommandNode parsedCommandNode : commandContextBuilder.getNodes()) {
            ArgumentCommandNode argumentCommandNode;
            CommandNode commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((argumentCommandNode = (ArgumentCommandNode)commandNode).getType() instanceof SignedArgument) || (commandNode = (ParsedArgument)commandContextBuilder.getArguments().get(argumentCommandNode.getName())) == null) continue;
            String string2 = commandNode.getRange().get(string);
            arrayList.add(new Argument(argumentCommandNode, string2));
        }
        return arrayList;
    }

    @Nullable
    public Argument<S> getArgument(String string) {
        for (Argument<S> argument : this.arguments) {
            if (!string.equals(argument.name())) continue;
            return argument;
        }
        return null;
    }

    public record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
        public String name() {
            return this.node.getName();
        }
    }
}

