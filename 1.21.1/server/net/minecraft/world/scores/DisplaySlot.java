/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum DisplaySlot implements StringRepresentable
{
    LIST(0, "list"),
    SIDEBAR(1, "sidebar"),
    BELOW_NAME(2, "below_name"),
    TEAM_BLACK(3, "sidebar.team.black"),
    TEAM_DARK_BLUE(4, "sidebar.team.dark_blue"),
    TEAM_DARK_GREEN(5, "sidebar.team.dark_green"),
    TEAM_DARK_AQUA(6, "sidebar.team.dark_aqua"),
    TEAM_DARK_RED(7, "sidebar.team.dark_red"),
    TEAM_DARK_PURPLE(8, "sidebar.team.dark_purple"),
    TEAM_GOLD(9, "sidebar.team.gold"),
    TEAM_GRAY(10, "sidebar.team.gray"),
    TEAM_DARK_GRAY(11, "sidebar.team.dark_gray"),
    TEAM_BLUE(12, "sidebar.team.blue"),
    TEAM_GREEN(13, "sidebar.team.green"),
    TEAM_AQUA(14, "sidebar.team.aqua"),
    TEAM_RED(15, "sidebar.team.red"),
    TEAM_LIGHT_PURPLE(16, "sidebar.team.light_purple"),
    TEAM_YELLOW(17, "sidebar.team.yellow"),
    TEAM_WHITE(18, "sidebar.team.white");

    public static final StringRepresentable.EnumCodec<DisplaySlot> CODEC;
    public static final IntFunction<DisplaySlot> BY_ID;
    private final int id;
    private final String name;

    private DisplaySlot(int n2, String string2) {
        this.id = n2;
        this.name = string2;
    }

    public int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Nullable
    public static DisplaySlot teamColorToSlot(ChatFormatting chatFormatting) {
        return switch (chatFormatting) {
            default -> throw new MatchException(null, null);
            case ChatFormatting.BLACK -> TEAM_BLACK;
            case ChatFormatting.DARK_BLUE -> TEAM_DARK_BLUE;
            case ChatFormatting.DARK_GREEN -> TEAM_DARK_GREEN;
            case ChatFormatting.DARK_AQUA -> TEAM_DARK_AQUA;
            case ChatFormatting.DARK_RED -> TEAM_DARK_RED;
            case ChatFormatting.DARK_PURPLE -> TEAM_DARK_PURPLE;
            case ChatFormatting.GOLD -> TEAM_GOLD;
            case ChatFormatting.GRAY -> TEAM_GRAY;
            case ChatFormatting.DARK_GRAY -> TEAM_DARK_GRAY;
            case ChatFormatting.BLUE -> TEAM_BLUE;
            case ChatFormatting.GREEN -> TEAM_GREEN;
            case ChatFormatting.AQUA -> TEAM_AQUA;
            case ChatFormatting.RED -> TEAM_RED;
            case ChatFormatting.LIGHT_PURPLE -> TEAM_LIGHT_PURPLE;
            case ChatFormatting.YELLOW -> TEAM_YELLOW;
            case ChatFormatting.WHITE -> TEAM_WHITE;
            case ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE, ChatFormatting.RESET, ChatFormatting.OBFUSCATED, ChatFormatting.STRIKETHROUGH -> null;
        };
    }

    static {
        CODEC = StringRepresentable.fromEnum(DisplaySlot::values);
        BY_ID = ByIdMap.continuous(DisplaySlot::id, DisplaySlot.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    }
}

