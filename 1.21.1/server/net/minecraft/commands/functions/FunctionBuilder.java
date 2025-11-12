/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.MacroFunction;
import net.minecraft.commands.functions.PlainTextFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.resources.ResourceLocation;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {
    @Nullable
    private List<UnboundEntryAction<T>> plainEntries = new ArrayList<UnboundEntryAction<T>>();
    @Nullable
    private List<MacroFunction.Entry<T>> macroEntries;
    private final List<String> macroArguments = new ArrayList<String>();

    FunctionBuilder() {
    }

    public void addCommand(UnboundEntryAction<T> unboundEntryAction) {
        if (this.macroEntries != null) {
            this.macroEntries.add(new MacroFunction.PlainTextEntry<T>(unboundEntryAction));
        } else {
            this.plainEntries.add(unboundEntryAction);
        }
    }

    private int getArgumentIndex(String string) {
        int n = this.macroArguments.indexOf(string);
        if (n == -1) {
            n = this.macroArguments.size();
            this.macroArguments.add(string);
        }
        return n;
    }

    private IntList convertToIndices(List<String> list) {
        IntArrayList intArrayList = new IntArrayList(list.size());
        for (String string : list) {
            intArrayList.add(this.getArgumentIndex(string));
        }
        return intArrayList;
    }

    public void addMacro(String string, int n, T t) {
        StringTemplate stringTemplate = StringTemplate.fromString(string, n);
        if (this.plainEntries != null) {
            this.macroEntries = new ArrayList<MacroFunction.Entry<T>>(this.plainEntries.size() + 1);
            for (UnboundEntryAction<T> unboundEntryAction : this.plainEntries) {
                this.macroEntries.add(new MacroFunction.PlainTextEntry<T>(unboundEntryAction));
            }
            this.plainEntries = null;
        }
        this.macroEntries.add(new MacroFunction.MacroEntry<T>(stringTemplate, this.convertToIndices(stringTemplate.variables()), t));
    }

    public CommandFunction<T> build(ResourceLocation resourceLocation) {
        if (this.macroEntries != null) {
            return new MacroFunction<T>(resourceLocation, this.macroEntries, this.macroArguments);
        }
        return new PlainTextFunction<T>(resourceLocation, this.plainEntries);
    }
}

