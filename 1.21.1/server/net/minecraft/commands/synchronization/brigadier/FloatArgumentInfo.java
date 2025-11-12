/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.FloatArgumentType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo
implements ArgumentTypeInfo<FloatArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != -3.4028235E38f;
        boolean bl2 = template.max != Float.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeFloat(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeFloat(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        float f = ArgumentUtils.numberHasMin(by) ? friendlyByteBuf.readFloat() : -3.4028235E38f;
        float f2 = ArgumentUtils.numberHasMax(by) ? friendlyByteBuf.readFloat() : Float.MAX_VALUE;
        return new Template(f, f2);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != -3.4028235E38f) {
            jsonObject.addProperty("min", (Number)Float.valueOf(template.min));
        }
        if (template.max != Float.MAX_VALUE) {
            jsonObject.addProperty("max", (Number)Float.valueOf(template.max));
        }
    }

    @Override
    public Template unpack(FloatArgumentType floatArgumentType) {
        return new Template(floatArgumentType.getMinimum(), floatArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<FloatArgumentType> {
        final float min;
        final float max;

        Template(float f, float f2) {
            this.min = f;
            this.max = f2;
        }

        @Override
        public FloatArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return FloatArgumentType.floatArg((float)this.min, (float)this.max);
        }

        @Override
        public ArgumentTypeInfo<FloatArgumentType, ?> type() {
            return FloatArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

