/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(object -> Component.translatableEscape("commands.attribute.failed.entity", object));
    private static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ATTRIBUTE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("commands.attribute.failed.no_attribute", object, object2));
    private static final Dynamic3CommandExceptionType ERROR_NO_SUCH_MODIFIER = new Dynamic3CommandExceptionType((object, object2, object3) -> Component.translatableEscape("commands.attribute.failed.no_modifier", object2, object, object3));
    private static final Dynamic3CommandExceptionType ERROR_MODIFIER_ALREADY_PRESENT = new Dynamic3CommandExceptionType((object, object2, object3) -> Component.translatableEscape("commands.attribute.failed.modifier_already_present", object3, object2, object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("attribute").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("target", EntityArgument.entity()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("attribute", ResourceArgument.resource(commandBuildContext, Registries.ATTRIBUTE)).then(((LiteralArgumentBuilder)Commands.literal("get").executes(commandContext -> AttributeCommand.getAttributeValue((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeValue((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale")))))).then(((LiteralArgumentBuilder)Commands.literal("base").then(Commands.literal("set").then(Commands.argument("value", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.setAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"value")))))).then(((LiteralArgumentBuilder)Commands.literal("get").executes(commandContext -> AttributeCommand.getAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeBase((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("modifier").then(Commands.literal("add").then(Commands.argument("id", ResourceLocationArgument.id()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("value", DoubleArgumentType.doubleArg()).then(Commands.literal("add_value").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"value"), AttributeModifier.Operation.ADD_VALUE)))).then(Commands.literal("add_multiplied_base").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"value"), AttributeModifier.Operation.ADD_MULTIPLIED_BASE)))).then(Commands.literal("add_multiplied_total").executes(commandContext -> AttributeCommand.addModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"value"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))))))).then(Commands.literal("remove").then(Commands.argument("id", ResourceLocationArgument.id()).executes(commandContext -> AttributeCommand.removeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id")))))).then(Commands.literal("value").then(Commands.literal("get").then(((RequiredArgumentBuilder)Commands.argument("id", ResourceLocationArgument.id()).executes(commandContext -> AttributeCommand.getAttributeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), 1.0))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(commandContext -> AttributeCommand.getAttributeModifier((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceArgument.getAttribute((CommandContext<CommandSourceStack>)commandContext, "attribute"), ResourceLocationArgument.getId((CommandContext<CommandSourceStack>)commandContext, "id"), DoubleArgumentType.getDouble((CommandContext)commandContext, (String)"scale")))))))))));
    }

    private static AttributeInstance getAttributeInstance(Entity entity, Holder<Attribute> holder) throws CommandSyntaxException {
        AttributeInstance attributeInstance = AttributeCommand.getLivingEntity(entity).getAttributes().getInstance(holder);
        if (attributeInstance == null) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create((Object)entity.getName(), (Object)AttributeCommand.getAttributeDescription(holder));
        }
        return attributeInstance;
    }

    private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING_ENTITY.create((Object)entity.getName());
        }
        return (LivingEntity)entity;
    }

    private static LivingEntity getEntityWithAttribute(Entity entity, Holder<Attribute> holder) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntity(entity);
        if (!livingEntity.getAttributes().hasAttribute(holder)) {
            throw ERROR_NO_SUCH_ATTRIBUTE.create((Object)entity.getName(), (Object)AttributeCommand.getAttributeDescription(holder));
        }
        return livingEntity;
    }

    private static int getAttributeValue(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, holder);
        double d2 = livingEntity.getAttributeValue(holder);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.value.get.success", AttributeCommand.getAttributeDescription(holder), entity.getName(), d2), false);
        return (int)(d2 * d);
    }

    private static int getAttributeBase(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, holder);
        double d2 = livingEntity.getAttributeBaseValue(holder);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.base_value.get.success", AttributeCommand.getAttributeDescription(holder), entity.getName(), d2), false);
        return (int)(d2 * d);
    }

    private static int getAttributeModifier(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, ResourceLocation resourceLocation, double d) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getEntityWithAttribute(entity, holder);
        AttributeMap attributeMap = livingEntity.getAttributes();
        if (!attributeMap.hasModifier(holder, resourceLocation)) {
            throw ERROR_NO_SUCH_MODIFIER.create((Object)entity.getName(), (Object)AttributeCommand.getAttributeDescription(holder), (Object)resourceLocation);
        }
        double d2 = attributeMap.getModifierValue(holder, resourceLocation);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.value.get.success", Component.translationArg(resourceLocation), AttributeCommand.getAttributeDescription(holder), entity.getName(), d2), false);
        return (int)(d2 * d);
    }

    private static int setAttributeBase(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, double d) throws CommandSyntaxException {
        AttributeCommand.getAttributeInstance(entity, holder).setBaseValue(d);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.base_value.set.success", AttributeCommand.getAttributeDescription(holder), entity.getName(), d), false);
        return 1;
    }

    private static int addModifier(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, ResourceLocation resourceLocation, double d, AttributeModifier.Operation operation) throws CommandSyntaxException {
        AttributeInstance attributeInstance = AttributeCommand.getAttributeInstance(entity, holder);
        AttributeModifier attributeModifier = new AttributeModifier(resourceLocation, d, operation);
        if (attributeInstance.hasModifier(resourceLocation)) {
            throw ERROR_MODIFIER_ALREADY_PRESENT.create((Object)entity.getName(), (Object)AttributeCommand.getAttributeDescription(holder), (Object)resourceLocation);
        }
        attributeInstance.addPermanentModifier(attributeModifier);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.add.success", Component.translationArg(resourceLocation), AttributeCommand.getAttributeDescription(holder), entity.getName()), false);
        return 1;
    }

    private static int removeModifier(CommandSourceStack commandSourceStack, Entity entity, Holder<Attribute> holder, ResourceLocation resourceLocation) throws CommandSyntaxException {
        AttributeInstance attributeInstance = AttributeCommand.getAttributeInstance(entity, holder);
        if (attributeInstance.removeModifier(resourceLocation)) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.attribute.modifier.remove.success", Component.translationArg(resourceLocation), AttributeCommand.getAttributeDescription(holder), entity.getName()), false);
            return 1;
        }
        throw ERROR_NO_SUCH_MODIFIER.create((Object)entity.getName(), (Object)AttributeCommand.getAttributeDescription(holder), (Object)resourceLocation);
    }

    private static Component getAttributeDescription(Holder<Attribute> holder) {
        return Component.translatable(holder.value().getDescriptionId());
    }
}

