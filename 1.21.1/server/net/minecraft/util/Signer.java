/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import net.minecraft.util.SignatureUpdater;
import org.slf4j.Logger;

public interface Signer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public byte[] sign(SignatureUpdater var1);

    default public byte[] sign(byte[] byArray) {
        return this.sign(output -> output.update(byArray));
    }

    public static Signer from(PrivateKey privateKey, String string) {
        return signatureUpdater -> {
            try {
                Signature signature = Signature.getInstance(string);
                signature.initSign(privateKey);
                signatureUpdater.update(signature::update);
                return signature.sign();
            }
            catch (Exception exception) {
                throw new IllegalStateException("Failed to sign message", exception);
            }
        };
    }
}

