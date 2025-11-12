/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.Contract
 */
package net.minecraft.world.item;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Contract;

public enum DyeColor implements StringRepresentable
{
    WHITE(0, "white", 0xF9FFFE, MapColor.SNOW, 0xF0F0F0, 0xFFFFFF),
    ORANGE(1, "orange", 16351261, MapColor.COLOR_ORANGE, 15435844, 16738335),
    MAGENTA(2, "magenta", 13061821, MapColor.COLOR_MAGENTA, 12801229, 0xFF00FF),
    LIGHT_BLUE(3, "light_blue", 3847130, MapColor.COLOR_LIGHT_BLUE, 6719955, 10141901),
    YELLOW(4, "yellow", 16701501, MapColor.COLOR_YELLOW, 14602026, 0xFFFF00),
    LIME(5, "lime", 8439583, MapColor.COLOR_LIGHT_GREEN, 4312372, 0xBFFF00),
    PINK(6, "pink", 15961002, MapColor.COLOR_PINK, 14188952, 16738740),
    GRAY(7, "gray", 4673362, MapColor.COLOR_GRAY, 0x434343, 0x808080),
    LIGHT_GRAY(8, "light_gray", 0x9D9D97, MapColor.COLOR_LIGHT_GRAY, 0xABABAB, 0xD3D3D3),
    CYAN(9, "cyan", 1481884, MapColor.COLOR_CYAN, 2651799, 65535),
    PURPLE(10, "purple", 8991416, MapColor.COLOR_PURPLE, 8073150, 10494192),
    BLUE(11, "blue", 3949738, MapColor.COLOR_BLUE, 2437522, 255),
    BROWN(12, "brown", 8606770, MapColor.COLOR_BROWN, 5320730, 9127187),
    GREEN(13, "green", 6192150, MapColor.COLOR_GREEN, 3887386, 65280),
    RED(14, "red", 11546150, MapColor.COLOR_RED, 11743532, 0xFF0000),
    BLACK(15, "black", 0x1D1D21, MapColor.COLOR_BLACK, 0x1E1B1B, 0);

    private static final IntFunction<DyeColor> BY_ID;
    private static final Int2ObjectOpenHashMap<DyeColor> BY_FIREWORK_COLOR;
    public static final StringRepresentable.EnumCodec<DyeColor> CODEC;
    public static final StreamCodec<ByteBuf, DyeColor> STREAM_CODEC;
    private final int id;
    private final String name;
    private final MapColor mapColor;
    private final int textureDiffuseColor;
    private final int fireworkColor;
    private final int textColor;

    private DyeColor(int n2, String string2, int n3, MapColor mapColor, int n4, int n5) {
        this.id = n2;
        this.name = string2;
        this.mapColor = mapColor;
        this.textColor = n5;
        this.textureDiffuseColor = FastColor.ARGB32.opaque(n3);
        this.fireworkColor = n4;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getTextureDiffuseColor() {
        return this.textureDiffuseColor;
    }

    public MapColor getMapColor() {
        return this.mapColor;
    }

    public int getFireworkColor() {
        return this.fireworkColor;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public static DyeColor byId(int n) {
        return BY_ID.apply(n);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,null->_")
    public static DyeColor byName(String string, @Nullable DyeColor dyeColor) {
        DyeColor dyeColor2 = CODEC.byName(string);
        return dyeColor2 != null ? dyeColor2 : dyeColor;
    }

    @Nullable
    public static DyeColor byFireworkColor(int n) {
        return (DyeColor)BY_FIREWORK_COLOR.get(n);
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        BY_ID = ByIdMap.continuous(DyeColor::getId, DyeColor.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        BY_FIREWORK_COLOR = new Int2ObjectOpenHashMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap(dyeColor -> dyeColor.fireworkColor, dyeColor -> dyeColor)));
        CODEC = StringRepresentable.fromEnum(DyeColor::values);
        STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DyeColor::getId);
    }
}

