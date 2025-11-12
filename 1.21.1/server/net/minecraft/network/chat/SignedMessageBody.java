/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.google.common.primitives.Longs
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageBody(String content, Instant timeStamp, long salt, LastSeenMessages lastSeen) {
    public static final MapCodec<SignedMessageBody> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("content").forGetter(SignedMessageBody::content), (App)ExtraCodecs.INSTANT_ISO8601.fieldOf("time_stamp").forGetter(SignedMessageBody::timeStamp), (App)Codec.LONG.fieldOf("salt").forGetter(SignedMessageBody::salt), (App)LastSeenMessages.CODEC.optionalFieldOf("last_seen", (Object)LastSeenMessages.EMPTY).forGetter(SignedMessageBody::lastSeen)).apply((Applicative)instance, SignedMessageBody::new));

    public static SignedMessageBody unsigned(String string) {
        return new SignedMessageBody(string, Instant.now(), 0L, LastSeenMessages.EMPTY);
    }

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(Longs.toByteArray((long)this.salt));
        output.update(Longs.toByteArray((long)this.timeStamp.getEpochSecond()));
        byte[] byArray = this.content.getBytes(StandardCharsets.UTF_8);
        output.update(Ints.toByteArray((int)byArray.length));
        output.update(byArray);
        this.lastSeen.updateSignature(output);
    }

    public Packed pack(MessageSignatureCache messageSignatureCache) {
        return new Packed(this.content, this.timeStamp, this.salt, this.lastSeen.pack(messageSignatureCache));
    }

    public record Packed(String content, Instant timeStamp, long salt, LastSeenMessages.Packed lastSeen) {
        public Packed(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), friendlyByteBuf.readLong(), new LastSeenMessages.Packed(friendlyByteBuf));
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(this.content, 256);
            friendlyByteBuf.writeInstant(this.timeStamp);
            friendlyByteBuf.writeLong(this.salt);
            this.lastSeen.write(friendlyByteBuf);
        }

        public Optional<SignedMessageBody> unpack(MessageSignatureCache messageSignatureCache) {
            return this.lastSeen.unpack(messageSignatureCache).map(lastSeenMessages -> new SignedMessageBody(this.content, this.timeStamp, this.salt, (LastSeenMessages)lastSeenMessages));
        }
    }
}

