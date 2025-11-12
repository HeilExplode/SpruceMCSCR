/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Queues;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCommandsPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundCommandsPacket> STREAM_CODEC = Packet.codec(ClientboundCommandsPacket::write, ClientboundCommandsPacket::new);
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<Entry> entries;

    public ClientboundCommandsPacket(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap = ClientboundCommandsPacket.enumerateNodes(rootCommandNode);
        this.entries = ClientboundCommandsPacket.createEntries(object2IntMap);
        this.rootIndex = object2IntMap.getInt(rootCommandNode);
    }

    private ClientboundCommandsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entries = friendlyByteBuf.readList(ClientboundCommandsPacket::readNode);
        this.rootIndex = friendlyByteBuf.readVarInt();
        ClientboundCommandsPacket.validateEntries(this.entries);
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.entries, (friendlyByteBuf, entry) -> entry.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
        friendlyByteBuf2.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<Entry> list, BiPredicate<Entry, IntSet> biPredicate) {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet((IntCollection)IntSets.fromTo((int)0, (int)list.size()));
        while (!intOpenHashSet.isEmpty()) {
            boolean bl = intOpenHashSet.removeIf(arg_0 -> ClientboundCommandsPacket.lambda$validateEntries$1(biPredicate, list, (IntSet)intOpenHashSet, arg_0));
            if (bl) continue;
            throw new IllegalStateException("Server sent an impossible command tree");
        }
    }

    private static void validateEntries(List<Entry> list) {
        ClientboundCommandsPacket.validateEntries(list, Entry::canBuild);
        ClientboundCommandsPacket.validateEntries(list, Entry::canResolve);
    }

    private static Object2IntMap<CommandNode<SharedSuggestionProvider>> enumerateNodes(RootCommandNode<SharedSuggestionProvider> rootCommandNode) {
        CommandNode commandNode;
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        ArrayDeque arrayDeque = Queues.newArrayDeque();
        arrayDeque.add(rootCommandNode);
        while ((commandNode = (CommandNode)arrayDeque.poll()) != null) {
            if (object2IntOpenHashMap.containsKey((Object)commandNode)) continue;
            int n = object2IntOpenHashMap.size();
            object2IntOpenHashMap.put((Object)commandNode, n);
            arrayDeque.addAll(commandNode.getChildren());
            if (commandNode.getRedirect() == null) continue;
            arrayDeque.add(commandNode.getRedirect());
        }
        return object2IntOpenHashMap;
    }

    private static List<Entry> createEntries(Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        ObjectArrayList objectArrayList = new ObjectArrayList(object2IntMap.size());
        objectArrayList.size(object2IntMap.size());
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            objectArrayList.set(entry.getIntValue(), (Object)ClientboundCommandsPacket.createEntry((CommandNode<SharedSuggestionProvider>)((CommandNode)entry.getKey()), object2IntMap));
        }
        return objectArrayList;
    }

    private static Entry readNode(FriendlyByteBuf friendlyByteBuf) {
        byte by = friendlyByteBuf.readByte();
        int[] nArray = friendlyByteBuf.readVarIntArray();
        int n = (by & 8) != 0 ? friendlyByteBuf.readVarInt() : 0;
        NodeStub nodeStub = ClientboundCommandsPacket.read(friendlyByteBuf, by);
        return new Entry(nodeStub, by, n, nArray);
    }

    @Nullable
    private static NodeStub read(FriendlyByteBuf friendlyByteBuf, byte by) {
        int n = by & 3;
        if (n == 2) {
            String string = friendlyByteBuf.readUtf();
            int n2 = friendlyByteBuf.readVarInt();
            ArgumentTypeInfo argumentTypeInfo = (ArgumentTypeInfo)BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(n2);
            if (argumentTypeInfo == null) {
                return null;
            }
            Object t = argumentTypeInfo.deserializeFromNetwork(friendlyByteBuf);
            ResourceLocation resourceLocation = (by & 0x10) != 0 ? friendlyByteBuf.readResourceLocation() : null;
            return new ArgumentNodeStub(string, (ArgumentTypeInfo.Template<?>)t, resourceLocation);
        }
        if (n == 1) {
            String string = friendlyByteBuf.readUtf();
            return new LiteralNodeStub(string);
        }
        return null;
    }

    private static Entry createEntry(CommandNode<SharedSuggestionProvider> commandNode, Object2IntMap<CommandNode<SharedSuggestionProvider>> object2IntMap) {
        Object object;
        NodeStub nodeStub;
        int n;
        int n2 = 0;
        if (commandNode.getRedirect() != null) {
            n2 |= 8;
            n = object2IntMap.getInt((Object)commandNode.getRedirect());
        } else {
            n = 0;
        }
        if (commandNode.getCommand() != null) {
            n2 |= 4;
        }
        if (commandNode instanceof RootCommandNode) {
            n2 |= 0;
            nodeStub = null;
        } else if (commandNode instanceof ArgumentCommandNode) {
            object = (ArgumentCommandNode)commandNode;
            nodeStub = new ArgumentNodeStub((ArgumentCommandNode<SharedSuggestionProvider, ?>)object);
            n2 |= 2;
            if (object.getCustomSuggestions() != null) {
                n2 |= 0x10;
            }
        } else if (commandNode instanceof LiteralCommandNode) {
            LiteralCommandNode literalCommandNode = (LiteralCommandNode)commandNode;
            nodeStub = new LiteralNodeStub(literalCommandNode.getLiteral());
            n2 |= 1;
        } else {
            throw new UnsupportedOperationException("Unknown node type " + String.valueOf(commandNode));
        }
        object = commandNode.getChildren().stream().mapToInt(arg_0 -> object2IntMap.getInt(arg_0)).toArray();
        return new Entry(nodeStub, n2, n, (int[])object);
    }

    @Override
    public PacketType<ClientboundCommandsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_COMMANDS;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommands(this);
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot(CommandBuildContext commandBuildContext) {
        return (RootCommandNode)new NodeResolver(commandBuildContext, this.entries).resolve(this.rootIndex);
    }

    private static /* synthetic */ boolean lambda$validateEntries$1(BiPredicate biPredicate, List list, IntSet intSet, int n) {
        return biPredicate.test((Entry)list.get(n), intSet);
    }

    static class Entry {
        @Nullable
        final NodeStub stub;
        final int flags;
        final int redirect;
        final int[] children;

        Entry(@Nullable NodeStub nodeStub, int n, int n2, int[] nArray) {
            this.stub = nodeStub;
            this.flags = n;
            this.redirect = n2;
            this.children = nArray;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.flags);
            friendlyByteBuf.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                friendlyByteBuf.writeVarInt(this.redirect);
            }
            if (this.stub != null) {
                this.stub.write(friendlyByteBuf);
            }
        }

        public boolean canBuild(IntSet intSet) {
            if ((this.flags & 8) != 0) {
                return !intSet.contains(this.redirect);
            }
            return true;
        }

        public boolean canResolve(IntSet intSet) {
            for (int n : this.children) {
                if (!intSet.contains(n)) continue;
                return false;
            }
            return true;
        }
    }

    static interface NodeStub {
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext var1);

        public void write(FriendlyByteBuf var1);
    }

    static class ArgumentNodeStub
    implements NodeStub {
        private final String id;
        private final ArgumentTypeInfo.Template<?> argumentType;
        @Nullable
        private final ResourceLocation suggestionId;

        @Nullable
        private static ResourceLocation getSuggestionId(@Nullable SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
            return suggestionProvider != null ? SuggestionProviders.getName(suggestionProvider) : null;
        }

        ArgumentNodeStub(String string, ArgumentTypeInfo.Template<?> template, @Nullable ResourceLocation resourceLocation) {
            this.id = string;
            this.argumentType = template;
            this.suggestionId = resourceLocation;
        }

        public ArgumentNodeStub(ArgumentCommandNode<SharedSuggestionProvider, ?> argumentCommandNode) {
            this(argumentCommandNode.getName(), ArgumentTypeInfos.unpack(argumentCommandNode.getType()), ArgumentNodeStub.getSuggestionId((SuggestionProvider<SharedSuggestionProvider>)argumentCommandNode.getCustomSuggestions()));
        }

        @Override
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext) {
            Object obj = this.argumentType.instantiate(commandBuildContext);
            RequiredArgumentBuilder requiredArgumentBuilder = RequiredArgumentBuilder.argument((String)this.id, obj);
            if (this.suggestionId != null) {
                requiredArgumentBuilder.suggests(SuggestionProviders.getProvider(this.suggestionId));
            }
            return requiredArgumentBuilder;
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
            ArgumentNodeStub.serializeCap(friendlyByteBuf, this.argumentType);
            if (this.suggestionId != null) {
                friendlyByteBuf.writeResourceLocation(this.suggestionId);
            }
        }

        private static <A extends ArgumentType<?>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo.Template<A> template) {
            ArgumentNodeStub.serializeCap(friendlyByteBuf, template.type(), template);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeCap(FriendlyByteBuf friendlyByteBuf, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
            friendlyByteBuf.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(argumentTypeInfo));
            argumentTypeInfo.serializeToNetwork(template, friendlyByteBuf);
        }
    }

    static class LiteralNodeStub
    implements NodeStub {
        private final String id;

        LiteralNodeStub(String string) {
            this.id = string;
        }

        @Override
        public ArgumentBuilder<SharedSuggestionProvider, ?> build(CommandBuildContext commandBuildContext) {
            return LiteralArgumentBuilder.literal((String)this.id);
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.id);
        }
    }

    static class NodeResolver {
        private final CommandBuildContext context;
        private final List<Entry> entries;
        private final List<CommandNode<SharedSuggestionProvider>> nodes;

        NodeResolver(CommandBuildContext commandBuildContext, List<Entry> list) {
            this.context = commandBuildContext;
            this.entries = list;
            ObjectArrayList objectArrayList = new ObjectArrayList();
            objectArrayList.size(list.size());
            this.nodes = objectArrayList;
        }

        public CommandNode<SharedSuggestionProvider> resolve(int n) {
            RootCommandNode rootCommandNode;
            CommandNode<SharedSuggestionProvider> commandNode = this.nodes.get(n);
            if (commandNode != null) {
                return commandNode;
            }
            Entry entry = this.entries.get(n);
            if (entry.stub == null) {
                rootCommandNode = new RootCommandNode();
            } else {
                ArgumentBuilder<SharedSuggestionProvider, ?> argumentBuilder = entry.stub.build(this.context);
                if ((entry.flags & 8) != 0) {
                    argumentBuilder.redirect(this.resolve(entry.redirect));
                }
                if ((entry.flags & 4) != 0) {
                    argumentBuilder.executes(commandContext -> 0);
                }
                rootCommandNode = argumentBuilder.build();
            }
            this.nodes.set(n, (CommandNode<SharedSuggestionProvider>)rootCommandNode);
            for (int n2 : entry.children) {
                CommandNode<SharedSuggestionProvider> commandNode2 = this.resolve(n2);
                if (commandNode2 instanceof RootCommandNode) continue;
                rootCommandNode.addChild(commandNode2);
            }
            return rootCommandNode;
        }
    }
}

