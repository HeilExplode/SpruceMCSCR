/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 */
package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.commands.functions.CommandFunction;

public record StringTemplate(List<String> segments, List<String> variables) {
    public static StringTemplate fromString(String string, int n) {
        ImmutableList.Builder builder = ImmutableList.builder();
        ImmutableList.Builder builder2 = ImmutableList.builder();
        int n2 = string.length();
        int n3 = 0;
        int n4 = string.indexOf(36);
        while (n4 != -1) {
            if (n4 == n2 - 1 || string.charAt(n4 + 1) != '(') {
                n4 = string.indexOf(36, n4 + 1);
                continue;
            }
            builder.add((Object)string.substring(n3, n4));
            int n5 = string.indexOf(41, n4 + 1);
            if (n5 == -1) {
                throw new IllegalArgumentException("Unterminated macro variable in macro '" + string + "' on line " + n);
            }
            String string2 = string.substring(n4 + 2, n5);
            if (!StringTemplate.isValidVariableName(string2)) {
                throw new IllegalArgumentException("Invalid macro variable name '" + string2 + "' on line " + n);
            }
            builder2.add((Object)string2);
            n3 = n5 + 1;
            n4 = string.indexOf(36, n3);
        }
        if (n3 == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + n);
        }
        if (n3 != n2) {
            builder.add((Object)string.substring(n3));
        }
        return new StringTemplate((List<String>)builder.build(), (List<String>)builder2.build());
    }

    private static boolean isValidVariableName(String string) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return false;
        }
        return true;
    }

    public String substitute(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.variables.size(); ++i) {
            stringBuilder.append(this.segments.get(i)).append(list.get(i));
            CommandFunction.checkCommandLineLength(stringBuilder);
        }
        if (this.segments.size() > this.variables.size()) {
            stringBuilder.append(this.segments.get(this.segments.size() - 1));
        }
        CommandFunction.checkCommandLineLength(stringBuilder);
        return stringBuilder.toString();
    }
}

