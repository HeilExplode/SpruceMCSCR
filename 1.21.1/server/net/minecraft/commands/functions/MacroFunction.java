/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntLists
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.commands.functions.PlainTextFunction;
import net.minecraft.commands.functions.StringTemplate;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MacroFunction<T extends ExecutionCommandSource<T>>
implements CommandFunction<T> {
    private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#"), decimalFormat -> {
        decimalFormat.setMaximumFractionDigits(15);
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25f);
    private final ResourceLocation id;
    private final List<Entry<T>> entries;

    public MacroFunction(ResourceLocation resourceLocation, List<Entry<T>> list, List<String> list2) {
        this.id = resourceLocation;
        this.entries = list;
        this.parameters = list2;
    }

    @Override
    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable CompoundTag compoundTag, CommandDispatcher<T> commandDispatcher) throws FunctionInstantiationException {
        if (compoundTag == null) {
            throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
        }
        ArrayList<String> arrayList = new ArrayList<String>(this.parameters.size());
        for (String object2 : this.parameters) {
            Tag tag = compoundTag.get(object2);
            if (tag == null) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), object2));
            }
            arrayList.add(MacroFunction.stringify(tag));
        }
        InstantiatedFunction instantiatedFunction = (InstantiatedFunction)this.cache.getAndMoveToLast(arrayList);
        if (instantiatedFunction != null) {
            return instantiatedFunction;
        }
        if (this.cache.size() >= 8) {
            this.cache.removeFirst();
        }
        InstantiatedFunction<T> instantiatedFunction2 = this.substituteAndParse(this.parameters, arrayList, commandDispatcher);
        this.cache.put(arrayList, instantiatedFunction2);
        return instantiatedFunction2;
    }

    private static String stringify(Tag tag) {
        if (tag instanceof FloatTag) {
            FloatTag floatTag = (FloatTag)tag;
            return DECIMAL_FORMAT.format(floatTag.getAsFloat());
        }
        if (tag instanceof DoubleTag) {
            DoubleTag doubleTag = (DoubleTag)tag;
            return DECIMAL_FORMAT.format(doubleTag.getAsDouble());
        }
        if (tag instanceof ByteTag) {
            ByteTag byteTag = (ByteTag)tag;
            return String.valueOf(byteTag.getAsByte());
        }
        if (tag instanceof ShortTag) {
            ShortTag shortTag = (ShortTag)tag;
            return String.valueOf(shortTag.getAsShort());
        }
        if (tag instanceof LongTag) {
            LongTag longTag = (LongTag)tag;
            return String.valueOf(longTag.getAsLong());
        }
        return tag.getAsString();
    }

    private static void lookupValues(List<String> list, IntList intList, List<String> list2) {
        list2.clear();
        intList.forEach(n -> list2.add((String)list.get(n)));
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> list, List<String> list2, CommandDispatcher<T> commandDispatcher) throws FunctionInstantiationException {
        ArrayList arrayList = new ArrayList(this.entries.size());
        ArrayList<String> arrayList2 = new ArrayList<String>(list2.size());
        for (Entry<T> entry : this.entries) {
            MacroFunction.lookupValues(list2, entry.parameters(), arrayList2);
            arrayList.add(entry.instantiate(arrayList2, commandDispatcher, this.id));
        }
        return new PlainTextFunction(this.id().withPath(string -> string + "/" + list.hashCode()), arrayList);
    }

    static interface Entry<T> {
        public IntList parameters();

        public UnboundEntryAction<T> instantiate(List<String> var1, CommandDispatcher<T> var2, ResourceLocation var3) throws FunctionInstantiationException;
    }

    static class MacroEntry<T extends ExecutionCommandSource<T>>
    implements Entry<T> {
        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public MacroEntry(StringTemplate stringTemplate, IntList intList, T t) {
            this.template = stringTemplate;
            this.parameters = intList;
            this.compilationContext = t;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, ResourceLocation resourceLocation) throws FunctionInstantiationException {
            String string = this.template.substitute(list);
            try {
                return CommandFunction.parseCommand(commandDispatcher, this.compilationContext, new StringReader(string));
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new FunctionInstantiationException(Component.translatable("commands.function.error.parse", Component.translationArg(resourceLocation), string, commandSyntaxException.getMessage()));
            }
        }
    }

    static class PlainTextEntry<T>
    implements Entry<T> {
        private final UnboundEntryAction<T> compiledAction;

        public PlainTextEntry(UnboundEntryAction<T> unboundEntryAction) {
            this.compiledAction = unboundEntryAction;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, ResourceLocation resourceLocation) {
            return this.compiledAction;
        }
    }
}

