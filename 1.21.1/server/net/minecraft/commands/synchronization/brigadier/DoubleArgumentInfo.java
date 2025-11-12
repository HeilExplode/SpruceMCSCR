/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo
implements ArgumentTypeInfo<DoubleArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != -1.7976931348623157E308;
        boolean bl2 = template.max != Double.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeDouble(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeDouble(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        double d = ArgumentUtils.numberHasMin(by) ? friendlyByteBuf.readDouble() : -1.7976931348623157E308;
        double d2 = ArgumentUtils.numberHasMax(by) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
        return new Template(d, d2);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != -1.7976931348623157E308) {
            jsonObject.addProperty("min", (Number)template.min);
        }
        if (template.max != Double.MAX_VALUE) {
            jsonObject.addProperty("max", (Number)template.max);
        }
    }

    @Override
    public Template unpack(DoubleArgumentType doubleArgumentType) {
        return new Template(doubleArgumentType.getMinimum(), doubleArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<DoubleArgumentType> {
        final double min;
        final double max;

        Template(double d, double d2) {
            this.min = d;
            this.max = d2;
        }

        @Override
        public DoubleArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return DoubleArgumentType.doubleArg((double)this.min, (double)this.max);
        }

        @Override
        public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
            return DoubleArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

