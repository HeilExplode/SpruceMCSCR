/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.serialization.Codec
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
    public static final Codec<LastSeenMessages> CODEC = MessageSignature.CODEC.listOf().xmap(LastSeenMessages::new, LastSeenMessages::entries);
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(Ints.toByteArray((int)this.entries.size()));
        for (MessageSignature messageSignature : this.entries) {
            output.update(messageSignature.bytes());
        }
    }

    public Packed pack(MessageSignatureCache messageSignatureCache) {
        return new Packed(this.entries.stream().map(messageSignature -> messageSignature.pack(messageSignatureCache)).toList());
    }

    public record Packed(List<MessageSignature.Packed> entries) {
        public static final Packed EMPTY = new Packed(List.of());

        public Packed(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeCollection(this.entries, MessageSignature.Packed::write);
        }

        public Optional<LastSeenMessages> unpack(MessageSignatureCache messageSignatureCache) {
            ArrayList<MessageSignature> arrayList = new ArrayList<MessageSignature>(this.entries.size());
            for (MessageSignature.Packed packed : this.entries) {
                Optional<MessageSignature> optional = packed.unpack(messageSignatureCache);
                if (optional.isEmpty()) {
                    return Optional.empty();
                }
                arrayList.add(optional.get());
            }
            return Optional.of(new LastSeenMessages(arrayList));
        }
    }

    public record Update(int offset, BitSet acknowledged) {
        public Update(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFixedBitSet(20));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeVarInt(this.offset);
            friendlyByteBuf.writeFixedBitSet(this.acknowledged, 20);
        }
    }
}

