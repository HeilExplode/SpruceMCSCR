/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.LongArgumentType
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo
implements ArgumentTypeInfo<LongArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != Long.MIN_VALUE;
        boolean bl2 = template.max != Long.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeLong(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeLong(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        long l = ArgumentUtils.numberHasMin(by) ? friendlyByteBuf.readLong() : Long.MIN_VALUE;
        long l2 = ArgumentUtils.numberHasMax(by) ? friendlyByteBuf.readLong() : Long.MAX_VALUE;
        return new Template(l, l2);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != Long.MIN_VALUE) {
            jsonObject.addProperty("min", (Number)template.min);
        }
        if (template.max != Long.MAX_VALUE) {
            jsonObject.addProperty("max", (Number)template.max);
        }
    }

    @Override
    public Template unpack(LongArgumentType longArgumentType) {
        return new Template(longArgumentType.getMinimum(), longArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<LongArgumentType> {
        final long min;
        final long max;

        Template(long l, long l2) {
            this.min = l;
            this.max = l2;
        }

        @Override
        public LongArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return LongArgumentType.longArg((long)this.min, (long)this.max);
        }

        @Override
        public ArgumentTypeInfo<LongArgumentType, ?> type() {
            return LongArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

