/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
    public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index), (App)UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender), (App)UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)).apply((Applicative)instance, SignedMessageLink::new));

    public static SignedMessageLink unsigned(UUID uUID) {
        return SignedMessageLink.root(uUID, Util.NIL_UUID);
    }

    public static SignedMessageLink root(UUID uUID, UUID uUID2) {
        return new SignedMessageLink(0, uUID, uUID2);
    }

    public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
        output.update(UUIDUtil.uuidToByteArray(this.sender));
        output.update(UUIDUtil.uuidToByteArray(this.sessionId));
        output.update(Ints.toByteArray((int)this.index));
    }

    public boolean isDescendantOf(SignedMessageLink signedMessageLink) {
        return this.index > signedMessageLink.index() && this.sender.equals(signedMessageLink.sender()) && this.sessionId.equals(signedMessageLink.sessionId());
    }

    @Nullable
    public SignedMessageLink advance() {
        if (this.index == Integer.MAX_VALUE) {
            return null;
        }
        return new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
    }
}

