/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
    static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    SignedMessageLink nextLink;
    Instant lastTimeStamp = Instant.EPOCH;

    public SignedMessageChain(UUID uUID, UUID uUID2) {
        this.nextLink = SignedMessageLink.root(uUID, uUID2);
    }

    public Encoder encoder(Signer signer) {
        return signedMessageBody -> {
            SignedMessageLink signedMessageLink = this.nextLink;
            if (signedMessageLink == null) {
                return null;
            }
            this.nextLink = signedMessageLink.advance();
            return new MessageSignature(signer.sign(output -> PlayerChatMessage.updateSignature(output, signedMessageLink, signedMessageBody)));
        };
    }

    public Decoder decoder(final ProfilePublicKey profilePublicKey) {
        final SignatureValidator signatureValidator = profilePublicKey.createSignatureValidator();
        return new Decoder(){

            @Override
            public PlayerChatMessage unpack(@Nullable MessageSignature messageSignature, SignedMessageBody signedMessageBody) throws DecodeException {
                if (messageSignature == null) {
                    throw new DecodeException(DecodeException.MISSING_PROFILE_KEY);
                }
                if (profilePublicKey.data().hasExpired()) {
                    throw new DecodeException(DecodeException.EXPIRED_PROFILE_KEY);
                }
                SignedMessageLink signedMessageLink = SignedMessageChain.this.nextLink;
                if (signedMessageLink == null) {
                    throw new DecodeException(DecodeException.CHAIN_BROKEN);
                }
                if (signedMessageBody.timeStamp().isBefore(SignedMessageChain.this.lastTimeStamp)) {
                    this.setChainBroken();
                    throw new DecodeException(DecodeException.OUT_OF_ORDER_CHAT);
                }
                SignedMessageChain.this.lastTimeStamp = signedMessageBody.timeStamp();
                PlayerChatMessage playerChatMessage = new PlayerChatMessage(signedMessageLink, messageSignature, signedMessageBody, null, FilterMask.PASS_THROUGH);
                if (!playerChatMessage.verify(signatureValidator)) {
                    this.setChainBroken();
                    throw new DecodeException(DecodeException.INVALID_SIGNATURE);
                }
                if (playerChatMessage.hasExpiredServer(Instant.now())) {
                    LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)signedMessageBody.content());
                }
                SignedMessageChain.this.nextLink = signedMessageLink.advance();
                return playerChatMessage;
            }

            @Override
            public void setChainBroken() {
                SignedMessageChain.this.nextLink = null;
            }
        };
    }

    @FunctionalInterface
    public static interface Encoder {
        public static final Encoder UNSIGNED = signedMessageBody -> null;

        @Nullable
        public MessageSignature pack(SignedMessageBody var1);
    }

    public static class DecodeException
    extends ThrowingComponent {
        static final Component MISSING_PROFILE_KEY = Component.translatable("chat.disabled.missingProfileKey");
        static final Component CHAIN_BROKEN = Component.translatable("chat.disabled.chain_broken");
        static final Component EXPIRED_PROFILE_KEY = Component.translatable("chat.disabled.expiredProfileKey");
        static final Component INVALID_SIGNATURE = Component.translatable("chat.disabled.invalid_signature");
        static final Component OUT_OF_ORDER_CHAT = Component.translatable("chat.disabled.out_of_order_chat");

        public DecodeException(Component component) {
            super(component);
        }
    }

    @FunctionalInterface
    public static interface Decoder {
        public static Decoder unsigned(UUID uUID, BooleanSupplier booleanSupplier) {
            return (messageSignature, signedMessageBody) -> {
                if (booleanSupplier.getAsBoolean()) {
                    throw new DecodeException(DecodeException.MISSING_PROFILE_KEY);
                }
                return PlayerChatMessage.unsigned(uUID, signedMessageBody.content());
            };
        }

        public PlayerChatMessage unpack(@Nullable MessageSignature var1, SignedMessageBody var2) throws DecodeException;

        default public void setChainBroken() {
        }
    }
}

