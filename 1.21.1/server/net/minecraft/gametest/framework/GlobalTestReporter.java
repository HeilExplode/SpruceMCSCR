/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.LogTestReporter;
import net.minecraft.gametest.framework.TestReporter;

public class GlobalTestReporter {
    private static TestReporter DELEGATE = new LogTestReporter();

    public static void replaceWith(TestReporter testReporter) {
        DELEGATE = testReporter;
    }

    public static void onTestFailed(GameTestInfo gameTestInfo) {
        DELEGATE.onTestFailed(gameTestInfo);
    }

    public static void onTestSuccess(GameTestInfo gameTestInfo) {
        DELEGATE.onTestSuccess(gameTestInfo);
    }

    public static void finish() {
        DELEGATE.finish();
    }
}

