/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo
implements ArgumentTypeInfo<IntegerArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != Integer.MIN_VALUE;
        boolean bl2 = template.max != Integer.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeInt(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeInt(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        int n = ArgumentUtils.numberHasMin(by) ? friendlyByteBuf.readInt() : Integer.MIN_VALUE;
        int n2 = ArgumentUtils.numberHasMax(by) ? friendlyByteBuf.readInt() : Integer.MAX_VALUE;
        return new Template(n, n2);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != Integer.MIN_VALUE) {
            jsonObject.addProperty("min", (Number)template.min);
        }
        if (template.max != Integer.MAX_VALUE) {
            jsonObject.addProperty("max", (Number)template.max);
        }
    }

    @Override
    public Template unpack(IntegerArgumentType integerArgumentType) {
        return new Template(integerArgumentType.getMinimum(), integerArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<IntegerArgumentType> {
        final int min;
        final int max;

        Template(int n, int n2) {
            this.min = n;
            this.max = n2;
        }

        @Override
        public IntegerArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return IntegerArgumentType.integer((int)this.min, (int)this.max);
        }

        @Override
        public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
            return IntegerArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

