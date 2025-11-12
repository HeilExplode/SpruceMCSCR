/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestInfo;

class ExhaustedAttemptsException
extends Throwable {
    public ExhaustedAttemptsException(int n, int n2, GameTestInfo gameTestInfo) {
        super("Not enough successes: " + n2 + " out of " + n + " attempts. Required successes: " + gameTestInfo.requiredSuccesses() + ". max attempts: " + gameTestInfo.maxAttempts() + ".", gameTestInfo.getError());
    }
}

