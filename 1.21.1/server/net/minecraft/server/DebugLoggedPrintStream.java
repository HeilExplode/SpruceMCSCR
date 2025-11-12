/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import net.minecraft.server.LoggedPrintStream;
import org.slf4j.Logger;

public class DebugLoggedPrintStream
extends LoggedPrintStream {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DebugLoggedPrintStream(String string, OutputStream outputStream) {
        super(string, outputStream);
    }

    @Override
    protected void logLine(String string) {
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        StackTraceElement stackTraceElement = stackTraceElementArray[Math.min(3, stackTraceElementArray.length)];
        LOGGER.info("[{}]@.({}:{}): {}", new Object[]{this.name, stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), string});
    }
}

