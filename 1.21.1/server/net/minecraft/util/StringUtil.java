/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
    private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

    public static String formatTickDuration(int n, float f) {
        int n2 = Mth.floor((float)n / f);
        int n3 = n2 / 60;
        n2 %= 60;
        int n4 = n3 / 60;
        n3 %= 60;
        if (n4 > 0) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", n4, n3, n2);
        }
        return String.format(Locale.ROOT, "%02d:%02d", n3, n2);
    }

    public static String stripColor(String string) {
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return StringUtils.isEmpty((CharSequence)string);
    }

    public static String truncateStringIfNecessary(String string, int n, boolean bl) {
        if (string.length() <= n) {
            return string;
        }
        if (bl && n > 3) {
            return string.substring(0, n - 3) + "...";
        }
        return string.substring(0, n);
    }

    public static int lineCount(String string) {
        if (string.isEmpty()) {
            return 0;
        }
        Matcher matcher = LINE_PATTERN.matcher(string);
        int n = 1;
        while (matcher.find()) {
            ++n;
        }
        return n;
    }

    public static boolean endsWithNewLine(String string) {
        return LINE_END_PATTERN.matcher(string).find();
    }

    public static String trimChatMessage(String string) {
        return StringUtil.truncateStringIfNecessary(string, 256, false);
    }

    public static boolean isAllowedChatCharacter(char c) {
        return c != '\u00a7' && c >= ' ' && c != '\u007f';
    }

    public static boolean isValidPlayerName(String string) {
        if (string.length() > 16) {
            return false;
        }
        return string.chars().filter(n -> n <= 32 || n >= 127).findAny().isEmpty();
    }

    public static String filterText(String string) {
        return StringUtil.filterText(string, false);
    }

    public static String filterText(String string, boolean bl) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (StringUtil.isAllowedChatCharacter(c)) {
                stringBuilder.append(c);
                continue;
            }
            if (!bl || c != '\n') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static boolean isWhitespace(int n) {
        return Character.isWhitespace(n) || Character.isSpaceChar(n);
    }

    public static boolean isBlank(@Nullable String string) {
        if (string == null || string.length() == 0) {
            return true;
        }
        return string.chars().allMatch(StringUtil::isWhitespace);
    }
}

