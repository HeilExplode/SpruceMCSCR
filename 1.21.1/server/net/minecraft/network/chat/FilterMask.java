/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.StringUtils;

public class FilterMask {
    public static final Codec<FilterMask> CODEC = StringRepresentable.fromEnum(Type::values).dispatch(FilterMask::type, Type::codec);
    public static final FilterMask FULLY_FILTERED = new FilterMask(new BitSet(0), Type.FULLY_FILTERED);
    public static final FilterMask PASS_THROUGH = new FilterMask(new BitSet(0), Type.PASS_THROUGH);
    public static final Style FILTERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.filtered")));
    static final MapCodec<FilterMask> PASS_THROUGH_CODEC = MapCodec.unit((Object)PASS_THROUGH);
    static final MapCodec<FilterMask> FULLY_FILTERED_CODEC = MapCodec.unit((Object)FULLY_FILTERED);
    static final MapCodec<FilterMask> PARTIALLY_FILTERED_CODEC = ExtraCodecs.BIT_SET.xmap(FilterMask::new, FilterMask::mask).fieldOf("value");
    private static final char HASH = '#';
    private final BitSet mask;
    private final Type type;

    private FilterMask(BitSet bitSet, Type type) {
        this.mask = bitSet;
        this.type = type;
    }

    private FilterMask(BitSet bitSet) {
        this.mask = bitSet;
        this.type = Type.PARTIALLY_FILTERED;
    }

    public FilterMask(int n) {
        this(new BitSet(n), Type.PARTIALLY_FILTERED);
    }

    private Type type() {
        return this.type;
    }

    private BitSet mask() {
        return this.mask;
    }

    public static FilterMask read(FriendlyByteBuf friendlyByteBuf) {
        Type type = friendlyByteBuf.readEnum(Type.class);
        return switch (type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> PASS_THROUGH;
            case 1 -> FULLY_FILTERED;
            case 2 -> new FilterMask(friendlyByteBuf.readBitSet(), Type.PARTIALLY_FILTERED);
        };
    }

    public static void write(FriendlyByteBuf friendlyByteBuf, FilterMask filterMask) {
        friendlyByteBuf.writeEnum(filterMask.type);
        if (filterMask.type == Type.PARTIALLY_FILTERED) {
            friendlyByteBuf.writeBitSet(filterMask.mask);
        }
    }

    public void setFiltered(int n) {
        this.mask.set(n);
    }

    @Nullable
    public String apply(String string) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> string;
            case 2 -> {
                char[] var2_2 = string.toCharArray();
                for (int var3_3 = 0; var3_3 < var2_2.length && var3_3 < this.mask.length(); ++var3_3) {
                    if (!this.mask.get(var3_3)) continue;
                    var2_2[var3_3] = 35;
                }
                yield new String(var2_2);
            }
        };
    }

    @Nullable
    public Component applyWithFormatting(String string) {
        return switch (this.type.ordinal()) {
            default -> throw new MatchException(null, null);
            case 1 -> null;
            case 0 -> Component.literal(string);
            case 2 -> {
                MutableComponent var2_2 = Component.empty();
                int var3_3 = 0;
                boolean var4_4 = this.mask.get(0);
                while (true) {
                    int var5_5 = var4_4 ? this.mask.nextClearBit(var3_3) : this.mask.nextSetBit(var3_3);
                    int v1 = var5_5 = var5_5 < 0 ? string.length() : var5_5;
                    if (var5_5 == var3_3) break;
                    if (var4_4) {
                        var2_2.append(Component.literal(StringUtils.repeat((char)'#', (int)(var5_5 - var3_3))).withStyle(FILTERED_STYLE));
                    } else {
                        var2_2.append(string.substring(var3_3, var5_5));
                    }
                    var4_4 = !var4_4;
                    var3_3 = var5_5;
                }
                yield var2_2;
            }
        };
    }

    public boolean isEmpty() {
        return this.type == Type.PASS_THROUGH;
    }

    public boolean isFullyFiltered() {
        return this.type == Type.FULLY_FILTERED;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        FilterMask filterMask = (FilterMask)object;
        return this.mask.equals(filterMask.mask) && this.type == filterMask.type;
    }

    public int hashCode() {
        int n = this.mask.hashCode();
        n = 31 * n + this.type.hashCode();
        return n;
    }

    static enum Type implements StringRepresentable
    {
        PASS_THROUGH("pass_through", () -> PASS_THROUGH_CODEC),
        FULLY_FILTERED("fully_filtered", () -> FULLY_FILTERED_CODEC),
        PARTIALLY_FILTERED("partially_filtered", () -> PARTIALLY_FILTERED_CODEC);

        private final String serializedName;
        private final Supplier<MapCodec<FilterMask>> codec;

        private Type(String string2, Supplier<MapCodec<FilterMask>> supplier) {
            this.serializedName = string2;
            this.codec = supplier;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        private MapCodec<FilterMask> codec() {
            return this.codec.get();
        }
    }
}

