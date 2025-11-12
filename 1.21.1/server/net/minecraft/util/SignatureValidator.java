/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.authlib.yggdrasil.ServicesKeyType
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureUpdater;
import org.slf4j.Logger;

public interface SignatureValidator {
    public static final SignatureValidator NO_VALIDATION = (signatureUpdater, byArray) -> true;
    public static final Logger LOGGER = LogUtils.getLogger();

    public boolean validate(SignatureUpdater var1, byte[] var2);

    default public boolean validate(byte[] byArray, byte[] byArray2) {
        return this.validate(output -> output.update(byArray), byArray2);
    }

    private static boolean verifySignature(SignatureUpdater signatureUpdater, byte[] byArray, Signature signature) throws SignatureException {
        signatureUpdater.update(signature::update);
        return signature.verify(byArray);
    }

    public static SignatureValidator from(PublicKey publicKey, String string) {
        return (signatureUpdater, byArray) -> {
            try {
                Signature signature = Signature.getInstance(string);
                signature.initVerify(publicKey);
                return SignatureValidator.verifySignature(signatureUpdater, byArray, signature);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to verify signature", (Throwable)exception);
                return false;
            }
        };
    }

    @Nullable
    public static SignatureValidator from(ServicesKeySet servicesKeySet, ServicesKeyType servicesKeyType) {
        Collection collection = servicesKeySet.keys(servicesKeyType);
        if (collection.isEmpty()) {
            return null;
        }
        return (signatureUpdater, byArray) -> collection.stream().anyMatch(servicesKeyInfo -> {
            Signature signature = servicesKeyInfo.signature();
            try {
                return SignatureValidator.verifySignature(signatureUpdater, byArray, signature);
            }
            catch (SignatureException signatureException) {
                LOGGER.error("Failed to verify Services signature", (Throwable)signatureException);
                return false;
            }
        });
    }
}

