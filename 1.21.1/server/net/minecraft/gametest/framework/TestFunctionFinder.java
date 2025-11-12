/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import java.util.stream.Stream;
import net.minecraft.gametest.framework.TestFunction;

@FunctionalInterface
public interface TestFunctionFinder {
    public Stream<TestFunction> findTestFunctions();
}

