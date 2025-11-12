/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.world.level.block.Rotation;

public class TemplateRotationArgument
extends StringRepresentableArgument<Rotation> {
    private TemplateRotationArgument() {
        super(Rotation.CODEC, Rotation::values);
    }

    public static TemplateRotationArgument templateRotation() {
        return new TemplateRotationArgument();
    }

    public static Rotation getRotation(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Rotation)commandContext.getArgument(string, Rotation.class);
    }
}

