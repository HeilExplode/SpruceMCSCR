/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;

public class SnbtPrinterTagVisitor
implements TagVisitor {
    private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("{}", Lists.newArrayList((Object[])new String[]{"DataVersion", "author", "size", "data", "entities", "palette", "palettes"}));
        hashMap.put("{}.data.[].{}", Lists.newArrayList((Object[])new String[]{"pos", "state", "nbt"}));
        hashMap.put("{}.entities.[].{}", Lists.newArrayList((Object[])new String[]{"blockPos", "pos"}));
    });
    private static final Set<String> NO_INDENTATION = Sets.newHashSet((Object[])new String[]{"{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}"});
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private final String indentation;
    private final int depth;
    private final List<String> path;
    private String result = "";

    public SnbtPrinterTagVisitor() {
        this("    ", 0, Lists.newArrayList());
    }

    public SnbtPrinterTagVisitor(String string, int n, List<String> list) {
        this.indentation = string;
        this.depth = n;
        this.path = list;
    }

    public String visit(Tag tag) {
        tag.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag stringTag) {
        this.result = StringTag.quoteAndEscape(stringTag.getAsString());
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        this.result = String.valueOf(byteTag.getAsNumber()) + "b";
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        this.result = String.valueOf(shortTag.getAsNumber()) + "s";
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.result = String.valueOf(intTag.getAsNumber());
    }

    @Override
    public void visitLong(LongTag longTag) {
        this.result = String.valueOf(longTag.getAsNumber()) + "L";
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        this.result = floatTag.getAsFloat() + "f";
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        this.result = doubleTag.getAsDouble() + "d";
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        StringBuilder stringBuilder = new StringBuilder(LIST_OPEN).append("B").append(LIST_TYPE_SEPARATOR);
        byte[] byArray = byteArrayTag.getAsByteArray();
        for (int i = 0; i < byArray.length; ++i) {
            stringBuilder.append(ELEMENT_SPACING).append(byArray[i]).append("B");
            if (i == byArray.length - 1) continue;
            stringBuilder.append(ELEMENT_SEPARATOR);
        }
        stringBuilder.append(LIST_CLOSE);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        StringBuilder stringBuilder = new StringBuilder(LIST_OPEN).append("I").append(LIST_TYPE_SEPARATOR);
        int[] nArray = intArrayTag.getAsIntArray();
        for (int i = 0; i < nArray.length; ++i) {
            stringBuilder.append(ELEMENT_SPACING).append(nArray[i]);
            if (i == nArray.length - 1) continue;
            stringBuilder.append(ELEMENT_SEPARATOR);
        }
        stringBuilder.append(LIST_CLOSE);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        String string = "L";
        StringBuilder stringBuilder = new StringBuilder(LIST_OPEN).append("L").append(LIST_TYPE_SEPARATOR);
        long[] lArray = longArrayTag.getAsLongArray();
        for (int i = 0; i < lArray.length; ++i) {
            stringBuilder.append(ELEMENT_SPACING).append(lArray[i]).append("L");
            if (i == lArray.length - 1) continue;
            stringBuilder.append(ELEMENT_SEPARATOR);
        }
        stringBuilder.append(LIST_CLOSE);
        this.result = stringBuilder.toString();
    }

    @Override
    public void visitList(ListTag listTag) {
        String string;
        if (listTag.isEmpty()) {
            this.result = "[]";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(LIST_OPEN);
        this.pushPath("[]");
        String string2 = string = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
        if (!string.isEmpty()) {
            stringBuilder.append(NEWLINE);
        }
        for (int i = 0; i < listTag.size(); ++i) {
            stringBuilder.append(Strings.repeat((String)string, (int)(this.depth + 1)));
            stringBuilder.append(new SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(listTag.get(i)));
            if (i == listTag.size() - 1) continue;
            stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? ELEMENT_SPACING : NEWLINE);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(NEWLINE).append(Strings.repeat((String)string, (int)this.depth));
        }
        stringBuilder.append(LIST_CLOSE);
        this.result = stringBuilder.toString();
        this.popPath();
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        String string;
        if (compoundTag.isEmpty()) {
            this.result = "{}";
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(STRUCT_OPEN);
        this.pushPath("{}");
        String string2 = string = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
        if (!string.isEmpty()) {
            stringBuilder.append(NEWLINE);
        }
        List<String> list = this.getKeys(compoundTag);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String string3 = (String)iterator.next();
            Tag tag = compoundTag.get(string3);
            this.pushPath(string3);
            stringBuilder.append(Strings.repeat((String)string, (int)(this.depth + 1))).append(SnbtPrinterTagVisitor.handleEscapePretty(string3)).append(NAME_VALUE_SEPARATOR).append(ELEMENT_SPACING).append(new SnbtPrinterTagVisitor(string, this.depth + 1, this.path).visit(tag));
            this.popPath();
            if (!iterator.hasNext()) continue;
            stringBuilder.append(ELEMENT_SEPARATOR).append(string.isEmpty() ? ELEMENT_SPACING : NEWLINE);
        }
        if (!string.isEmpty()) {
            stringBuilder.append(NEWLINE).append(Strings.repeat((String)string, (int)this.depth));
        }
        stringBuilder.append(STRUCT_CLOSE);
        this.result = stringBuilder.toString();
        this.popPath();
    }

    private void popPath() {
        this.path.remove(this.path.size() - 1);
    }

    private void pushPath(String string) {
        this.path.add(string);
    }

    protected List<String> getKeys(CompoundTag compoundTag) {
        HashSet hashSet = Sets.newHashSet(compoundTag.getAllKeys());
        ArrayList arrayList = Lists.newArrayList();
        List<String> list = KEY_ORDER.get(this.pathString());
        if (list != null) {
            for (String string : list) {
                if (!hashSet.remove(string)) continue;
                arrayList.add(string);
            }
            if (!hashSet.isEmpty()) {
                hashSet.stream().sorted().forEach(arrayList::add);
            }
        } else {
            arrayList.addAll(hashSet);
            Collections.sort(arrayList);
        }
        return arrayList;
    }

    public String pathString() {
        return String.join((CharSequence)".", this.path);
    }

    protected static String handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return string;
        }
        return StringTag.quoteAndEscape(string);
    }

    @Override
    public void visitEnd(EndTag endTag) {
    }
}

