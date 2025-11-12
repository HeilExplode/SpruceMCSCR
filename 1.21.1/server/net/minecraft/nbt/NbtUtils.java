/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getInt(1)).thenComparingInt(listTag -> listTag.getInt(0)).thenComparingInt(listTag -> listTag.getInt(2));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDouble(1)).thenComparingDouble(listTag -> listTag.getDouble(0)).thenComparingDouble(listTag -> listTag.getDouble(2));
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on((String)",");
    private static final Splitter COLON_SPLITTER = Splitter.on((char)':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
        if (tag == tag2) {
            return true;
        }
        if (tag == null) {
            return true;
        }
        if (tag2 == null) {
            return false;
        }
        if (!tag.getClass().equals(tag2.getClass())) {
            return false;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            if (compoundTag2.size() < compoundTag.size()) {
                return false;
            }
            for (String string : compoundTag.getAllKeys()) {
                Tag tag3 = compoundTag.get(string);
                if (NbtUtils.compareNbt(tag3, compoundTag2.get(string), bl)) continue;
                return false;
            }
            return true;
        }
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (bl) {
                ListTag listTag2 = (ListTag)tag2;
                if (listTag.isEmpty()) {
                    return listTag2.isEmpty();
                }
                if (listTag2.size() < listTag.size()) {
                    return false;
                }
                for (Tag tag4 : listTag) {
                    boolean bl2 = false;
                    for (Tag tag5 : listTag2) {
                        if (!NbtUtils.compareNbt(tag4, tag5, bl)) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2) continue;
                    return false;
                }
                return true;
            }
        }
        return tag.equals(tag2);
    }

    public static IntArrayTag createUUID(UUID uUID) {
        return new IntArrayTag(UUIDUtil.uuidToIntArray(uUID));
    }

    public static UUID loadUUID(Tag tag) {
        if (tag.getType() != IntArrayTag.TYPE) {
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
        }
        int[] nArray = ((IntArrayTag)tag).getAsIntArray();
        if (nArray.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + nArray.length + ".");
        }
        return UUIDUtil.uuidFromIntArray(nArray);
    }

    public static Optional<BlockPos> readBlockPos(CompoundTag compoundTag, String string) {
        int[] nArray = compoundTag.getIntArray(string);
        if (nArray.length == 3) {
            return Optional.of(new BlockPos(nArray[0], nArray[1], nArray[2]));
        }
        return Optional.empty();
    }

    public static Tag writeBlockPos(BlockPos blockPos) {
        return new IntArrayTag(new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()});
    }

    public static BlockState readBlockState(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
        if (!compoundTag.contains("Name", 8)) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation resourceLocation = ResourceLocation.parse(compoundTag.getString("Name"));
        Optional<Holder.Reference<Block>> optional = holderGetter.get(ResourceKey.create(Registries.BLOCK, resourceLocation));
        if (optional.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = (Block)((Holder)optional.get()).value();
        BlockState blockState = block.defaultBlockState();
        if (compoundTag.contains("Properties", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String string : compoundTag2.getAllKeys()) {
                Property<?> property = stateDefinition.getProperty(string);
                if (property == null) continue;
                blockState = NbtUtils.setValueHelper(blockState, property, string, compoundTag2, compoundTag);
            }
        }
        return blockState;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S s, Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2) {
        Optional<T> optional = property.getValue(compoundTag.getString(string));
        if (optional.isPresent()) {
            return (S)((StateHolder)s.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{string, compoundTag.getString(string), compoundTag2});
        return s;
    }

    public static CompoundTag writeBlockState(BlockState blockState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
        Map<Property<?>, Comparable<?>> map = blockState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    public static CompoundTag writeFluidState(FluidState fluidState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.FLUID.getKey(fluidState.getType()).toString());
        Map<Property<?>, Comparable<?>> map = fluidState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static String prettyPrint(Tag tag) {
        return NbtUtils.prettyPrint(tag, false);
    }

    public static String prettyPrint(Tag tag, boolean bl) {
        return NbtUtils.prettyPrint(new StringBuilder(), tag, 0, bl).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int n, boolean bl) {
        switch (tag.getId()) {
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: 
            case 6: 
            case 8: {
                stringBuilder.append(tag);
                break;
            }
            case 0: {
                break;
            }
            case 7: {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
                byte[] byArray = byteArrayTag.getAsByteArray();
                int n2 = byArray.length;
                NbtUtils.indent(n, stringBuilder).append("byte[").append(n2).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (int i = 0; i < byArray.length; ++i) {
                        if (i != 0) {
                            stringBuilder.append(',');
                        }
                        if (i % 16 == 0 && i / 16 > 0) {
                            stringBuilder.append('\n');
                            if (i < byArray.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (i != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%02X", byArray[i] & 0xFF));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                break;
            }
            case 9: {
                ListTag listTag = (ListTag)tag;
                int n3 = listTag.size();
                byte by = listTag.getElementType();
                String string = by == 0 ? "undefined" : TagTypes.getType(by).getPrettyName();
                NbtUtils.indent(n, stringBuilder).append("list<").append(string).append(">[").append(n3).append("] [");
                if (n3 != 0) {
                    stringBuilder.append('\n');
                }
                for (int i = 0; i < n3; ++i) {
                    if (i != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtUtils.indent(n + 1, stringBuilder);
                    NbtUtils.prettyPrint(stringBuilder, listTag.get(i), n + 1, bl);
                }
                if (n3 != 0) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(n, stringBuilder).append(']');
                break;
            }
            case 11: {
                IntArrayTag intArrayTag = (IntArrayTag)tag;
                int[] nArray = intArrayTag.getAsIntArray();
                int n4 = 0;
                int[] nArray2 = nArray;
                int n5 = nArray2.length;
                for (int i = 0; i < n5; ++i) {
                    int n6 = nArray2[i];
                    n4 = Math.max(n4, String.format(Locale.ROOT, "%X", n6).length());
                }
                int n7 = nArray.length;
                NbtUtils.indent(n, stringBuilder).append("int[").append(n7).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (n5 = 0; n5 < nArray.length; ++n5) {
                        if (n5 != 0) {
                            stringBuilder.append(',');
                        }
                        if (n5 % 16 == 0 && n5 / 16 > 0) {
                            stringBuilder.append('\n');
                            if (n5 < nArray.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (n5 != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + n4 + "X", nArray[n5]));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                break;
            }
            case 10: {
                CompoundTag compoundTag = (CompoundTag)tag;
                ArrayList arrayList = Lists.newArrayList(compoundTag.getAllKeys());
                Collections.sort(arrayList);
                NbtUtils.indent(n, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (n + 1)) {
                    stringBuilder.append('\n');
                    NbtUtils.indent(n + 1, stringBuilder);
                }
                int n8 = arrayList.stream().mapToInt(String::length).max().orElse(0);
                String string = Strings.repeat((String)" ", (int)n8);
                for (int i = 0; i < arrayList.size(); ++i) {
                    if (i != 0) {
                        stringBuilder.append(",\n");
                    }
                    String string2 = (String)arrayList.get(i);
                    NbtUtils.indent(n + 1, stringBuilder).append('\"').append(string2).append('\"').append(string, 0, string.length() - string2.length()).append(": ");
                    NbtUtils.prettyPrint(stringBuilder, compoundTag.get(string2), n + 1, bl);
                }
                if (!arrayList.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(n, stringBuilder).append('}');
                break;
            }
            case 12: {
                int n9;
                LongArrayTag longArrayTag = (LongArrayTag)tag;
                long[] lArray = longArrayTag.getAsLongArray();
                long l = 0L;
                long[] lArray2 = lArray;
                int n10 = lArray2.length;
                for (n9 = 0; n9 < n10; ++n9) {
                    long l2 = lArray2[n9];
                    l = Math.max(l, (long)String.format(Locale.ROOT, "%X", l2).length());
                }
                long l3 = lArray.length;
                NbtUtils.indent(n, stringBuilder).append("long[").append(l3).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (n9 = 0; n9 < lArray.length; ++n9) {
                        if (n9 != 0) {
                            stringBuilder.append(',');
                        }
                        if (n9 % 16 == 0 && n9 / 16 > 0) {
                            stringBuilder.append('\n');
                            if (n9 < lArray.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (n9 != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + l + "X", lArray[n9]));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                break;
            }
            default: {
                stringBuilder.append("<UNKNOWN :(>");
            }
        }
        return stringBuilder;
    }

    private static StringBuilder indent(int n, StringBuilder stringBuilder) {
        int n2 = stringBuilder.lastIndexOf("\n") + 1;
        int n3 = stringBuilder.length() - n2;
        for (int i = 0; i < 2 * n - n3; ++i) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static Component toPrettyComponent(Tag tag) {
        return new TextComponentTagVisitor("").visit(tag);
    }

    public static String structureToSnbt(CompoundTag compoundTag) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate(compoundTag));
    }

    public static CompoundTag snbtToStructure(String string) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseTag(string));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag;
        ListTag listTag2;
        boolean bl = compoundTag2.contains("palettes", 9);
        ListTag listTag4 = bl ? compoundTag2.getList("palettes", 9).getList(0) : compoundTag2.getList("palette", 10);
        ListTag listTag5 = listTag4.stream().map(CompoundTag.class::cast).map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put("palette", listTag5);
        if (bl) {
            listTag2 = new ListTag();
            listTag = compoundTag2.getList("palettes", 9);
            listTag.stream().map(ListTag.class::cast).forEach(listTag3 -> {
                CompoundTag compoundTag = new CompoundTag();
                for (int i = 0; i < listTag3.size(); ++i) {
                    compoundTag.putString(listTag5.getString(i), NbtUtils.packBlockState(listTag3.getCompound(i)));
                }
                listTag2.add(compoundTag);
            });
            compoundTag2.put("palettes", listTag2);
        }
        if (compoundTag2.contains("entities", 9)) {
            listTag2 = compoundTag2.getList("entities", 10);
            listTag = listTag2.stream().map(CompoundTag.class::cast).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos", 6), YXZ_LISTTAG_DOUBLE_COMPARATOR)).collect(Collectors.toCollection(ListTag::new));
            compoundTag2.put("entities", listTag);
        }
        listTag2 = compoundTag2.getList("blocks", 10).stream().map(CompoundTag.class::cast).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos", 3), YXZ_LISTTAG_INT_COMPARATOR)).peek(compoundTag -> compoundTag.putString("state", listTag5.getString(compoundTag.getInt("state")))).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put(SNBT_DATA_TAG, listTag2);
        compoundTag2.remove("blocks");
        return compoundTag2;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag = compoundTag2.getList("palette", 8);
        Map map = (Map)listTag.stream().map(StringTag.class::cast).map(StringTag::getAsString).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        if (compoundTag2.contains("palettes", 9)) {
            compoundTag2.put("palettes", compoundTag2.getList("palettes", 10).stream().map(CompoundTag.class::cast).map(compoundTag -> map.keySet().stream().map(compoundTag::getString).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            compoundTag2.remove("palette");
        } else {
            compoundTag2.put("palette", map.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        if (compoundTag2.contains(SNBT_DATA_TAG, 9)) {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            object2IntOpenHashMap.defaultReturnValue(-1);
            for (int i = 0; i < listTag.size(); ++i) {
                object2IntOpenHashMap.put((Object)listTag.getString(i), i);
            }
            ListTag listTag2 = compoundTag2.getList(SNBT_DATA_TAG, 10);
            for (int i = 0; i < listTag2.size(); ++i) {
                CompoundTag compoundTag3 = listTag2.getCompound(i);
                String string = compoundTag3.getString("state");
                int n = object2IntOpenHashMap.getInt((Object)string);
                if (n == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                compoundTag3.putInt("state", n);
            }
            compoundTag2.put("blocks", listTag2);
            compoundTag2.remove(SNBT_DATA_TAG);
        }
        return compoundTag2;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag compoundTag) {
        StringBuilder stringBuilder = new StringBuilder(compoundTag.getString("Name"));
        if (compoundTag.contains("Properties", 10)) {
            CompoundTag compoundTag2 = compoundTag.getCompound("Properties");
            String string2 = compoundTag2.getAllKeys().stream().sorted().map(string -> string + ":" + compoundTag2.get((String)string).getAsString()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            stringBuilder.append('{').append(string2).append('}');
        }
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String string) {
        String string3;
        CompoundTag compoundTag = new CompoundTag();
        int n = string.indexOf(123);
        if (n >= 0) {
            string3 = string.substring(0, n);
            CompoundTag compoundTag2 = new CompoundTag();
            if (n + 2 <= string.length()) {
                String string4 = string.substring(n + 1, string.indexOf(125, n));
                COMMA_SPLITTER.split((CharSequence)string4).forEach(string2 -> {
                    List list = COLON_SPLITTER.splitToList((CharSequence)string2);
                    if (list.size() == 2) {
                        compoundTag2.putString((String)list.get(0), (String)list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                compoundTag.put("Properties", compoundTag2);
            }
        } else {
            string3 = string;
        }
        compoundTag.putString("Name", string3);
        return compoundTag;
    }

    public static CompoundTag addCurrentDataVersion(CompoundTag compoundTag) {
        int n = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        return NbtUtils.addDataVersion(compoundTag, n);
    }

    public static CompoundTag addDataVersion(CompoundTag compoundTag, int n) {
        compoundTag.putInt("DataVersion", n);
        return compoundTag;
    }

    public static int getDataVersion(CompoundTag compoundTag, int n) {
        return compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : n;
    }
}

