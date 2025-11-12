/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.commands.synchronization;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

public class ArgumentUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte NUMBER_FLAG_MIN = 1;
    private static final byte NUMBER_FLAG_MAX = 2;

    public static int createNumberFlags(boolean bl, boolean bl2) {
        int n = 0;
        if (bl) {
            n |= 1;
        }
        if (bl2) {
            n |= 2;
        }
        return n;
    }

    public static boolean numberHasMin(byte by) {
        return (by & 1) != 0;
    }

    public static boolean numberHasMax(byte by) {
        return (by & 2) != 0;
    }

    private static <A extends ArgumentType<?>> void serializeCap(JsonObject jsonObject, ArgumentTypeInfo.Template<A> template) {
        ArgumentUtils.serializeCap(jsonObject, template.type(), template);
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(JsonObject jsonObject, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
        argumentTypeInfo.serializeToJson(template, jsonObject);
    }

    private static <T extends ArgumentType<?>> void serializeArgumentToJson(JsonObject jsonObject, T t) {
        ArgumentTypeInfo.Template<T> template = ArgumentTypeInfos.unpack(t);
        jsonObject.addProperty("type", "argument");
        jsonObject.addProperty("parser", BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(template.type()).toString());
        JsonObject jsonObject2 = new JsonObject();
        ArgumentUtils.serializeCap(jsonObject2, template);
        if (jsonObject2.size() > 0) {
            jsonObject.add("properties", (JsonElement)jsonObject2);
        }
    }

    public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> commandDispatcher, CommandNode<S> commandNode) {
        CommandNode commandNode2;
        JsonObject jsonObject;
        JsonObject jsonObject2 = new JsonObject();
        if (commandNode instanceof RootCommandNode) {
            jsonObject2.addProperty("type", "root");
        } else if (commandNode instanceof LiteralCommandNode) {
            jsonObject2.addProperty("type", "literal");
        } else if (commandNode instanceof ArgumentCommandNode) {
            jsonObject = (ArgumentCommandNode)commandNode;
            ArgumentUtils.serializeArgumentToJson(jsonObject2, jsonObject.getType());
        } else {
            LOGGER.error("Could not serialize node {} ({})!", commandNode, commandNode.getClass());
            jsonObject2.addProperty("type", "unknown");
        }
        jsonObject = new JsonObject();
        Object object = commandNode.getChildren().iterator();
        while (object.hasNext()) {
            commandNode2 = (CommandNode)object.next();
            jsonObject.add(commandNode2.getName(), (JsonElement)ArgumentUtils.serializeNodeToJson(commandDispatcher, commandNode2));
        }
        if (jsonObject.size() > 0) {
            jsonObject2.add("children", (JsonElement)jsonObject);
        }
        if (commandNode.getCommand() != null) {
            jsonObject2.addProperty("executable", Boolean.valueOf(true));
        }
        if (commandNode.getRedirect() != null && !(object = commandDispatcher.getPath(commandNode.getRedirect())).isEmpty()) {
            commandNode2 = new JsonArray();
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                String string = (String)iterator.next();
                commandNode2.add(string);
            }
            jsonObject2.add("redirect", (JsonElement)commandNode2);
        }
        return jsonObject2;
    }

    public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> commandNode) {
        Set set = Sets.newIdentityHashSet();
        HashSet hashSet = Sets.newHashSet();
        ArgumentUtils.findUsedArgumentTypes(commandNode, hashSet, set);
        return hashSet;
    }

    private static <T> void findUsedArgumentTypes(CommandNode<T> commandNode2, Set<ArgumentType<?>> set, Set<CommandNode<T>> set2) {
        ArgumentCommandNode argumentCommandNode;
        if (!set2.add(commandNode2)) {
            return;
        }
        if (commandNode2 instanceof ArgumentCommandNode) {
            argumentCommandNode = (ArgumentCommandNode)commandNode2;
            set.add(argumentCommandNode.getType());
        }
        commandNode2.getChildren().forEach(commandNode -> ArgumentUtils.findUsedArgumentTypes(commandNode, set, set2));
        argumentCommandNode = commandNode2.getRedirect();
        if (argumentCommandNode != null) {
            ArgumentUtils.findUsedArgumentTypes(argumentCommandNode, set, set2);
        }
    }
}

