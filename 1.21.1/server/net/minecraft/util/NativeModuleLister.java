/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.logging.LogUtils
 *  com.sun.jna.Memory
 *  com.sun.jna.Native
 *  com.sun.jna.Platform
 *  com.sun.jna.Pointer
 *  com.sun.jna.platform.win32.Kernel32
 *  com.sun.jna.platform.win32.Kernel32Util
 *  com.sun.jna.platform.win32.Tlhelp32$MODULEENTRY32W
 *  com.sun.jna.platform.win32.Version
 *  com.sun.jna.platform.win32.Win32Exception
 *  com.sun.jna.ptr.IntByReference
 *  com.sun.jna.ptr.PointerByReference
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.CrashReportCategory;
import org.slf4j.Logger;

public class NativeModuleLister {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LANG_MASK = 65535;
    private static final int DEFAULT_LANG = 1033;
    private static final int CODEPAGE_MASK = -65536;
    private static final int DEFAULT_CODEPAGE = 0x4B00000;

    public static List<NativeModuleInfo> listModules() {
        if (!Platform.isWindows()) {
            return ImmutableList.of();
        }
        int n = Kernel32.INSTANCE.GetCurrentProcessId();
        ImmutableList.Builder builder = ImmutableList.builder();
        List list = Kernel32Util.getModules((int)n);
        for (Tlhelp32.MODULEENTRY32W mODULEENTRY32W : list) {
            String string = mODULEENTRY32W.szModule();
            Optional<NativeModuleVersion> optional = NativeModuleLister.tryGetVersion(mODULEENTRY32W.szExePath());
            builder.add((Object)new NativeModuleInfo(string, optional));
        }
        return builder.build();
    }

    private static Optional<NativeModuleVersion> tryGetVersion(String string) {
        try {
            IntByReference intByReference = new IntByReference();
            int n = Version.INSTANCE.GetFileVersionInfoSize(string, intByReference);
            if (n == 0) {
                int n2 = Native.getLastError();
                if (n2 == 1813 || n2 == 1812) {
                    return Optional.empty();
                }
                throw new Win32Exception(n2);
            }
            Memory memory = new Memory((long)n);
            if (!Version.INSTANCE.GetFileVersionInfo(string, 0, n, (Pointer)memory)) {
                throw new Win32Exception(Native.getLastError());
            }
            IntByReference intByReference2 = new IntByReference();
            Pointer pointer = NativeModuleLister.queryVersionValue((Pointer)memory, "\\VarFileInfo\\Translation", intByReference2);
            int[] nArray = pointer.getIntArray(0L, intByReference2.getValue() / 4);
            OptionalInt optionalInt = NativeModuleLister.findLangAndCodepage(nArray);
            if (optionalInt.isEmpty()) {
                return Optional.empty();
            }
            int n3 = optionalInt.getAsInt();
            int n4 = n3 & 0xFFFF;
            int n5 = (n3 & 0xFFFF0000) >> 16;
            String string2 = NativeModuleLister.queryVersionString((Pointer)memory, NativeModuleLister.langTableKey("FileDescription", n4, n5), intByReference2);
            String string3 = NativeModuleLister.queryVersionString((Pointer)memory, NativeModuleLister.langTableKey("CompanyName", n4, n5), intByReference2);
            String string4 = NativeModuleLister.queryVersionString((Pointer)memory, NativeModuleLister.langTableKey("FileVersion", n4, n5), intByReference2);
            return Optional.of(new NativeModuleVersion(string2, string4, string3));
        }
        catch (Exception exception) {
            LOGGER.info("Failed to find module info for {}", (Object)string, (Object)exception);
            return Optional.empty();
        }
    }

    private static String langTableKey(String string, int n, int n2) {
        return String.format(Locale.ROOT, "\\StringFileInfo\\%04x%04x\\%s", n, n2, string);
    }

    private static OptionalInt findLangAndCodepage(int[] nArray) {
        OptionalInt optionalInt = OptionalInt.empty();
        for (int n : nArray) {
            if ((n & 0xFFFF0000) == 0x4B00000 && (n & 0xFFFF) == 1033) {
                return OptionalInt.of(n);
            }
            optionalInt = OptionalInt.of(n);
        }
        return optionalInt;
    }

    private static Pointer queryVersionValue(Pointer pointer, String string, IntByReference intByReference) {
        PointerByReference pointerByReference = new PointerByReference();
        if (!Version.INSTANCE.VerQueryValue(pointer, string, pointerByReference, intByReference)) {
            throw new UnsupportedOperationException("Can't get version value " + string);
        }
        return pointerByReference.getValue();
    }

    private static String queryVersionString(Pointer pointer, String string, IntByReference intByReference) {
        try {
            Pointer pointer2 = NativeModuleLister.queryVersionValue(pointer, string, intByReference);
            byte[] byArray = pointer2.getByteArray(0L, (intByReference.getValue() - 1) * 2);
            return new String(byArray, StandardCharsets.UTF_16LE);
        }
        catch (Exception exception) {
            return "";
        }
    }

    public static void addCrashSection(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Modules", () -> NativeModuleLister.listModules().stream().sorted(Comparator.comparing(nativeModuleInfo -> nativeModuleInfo.name)).map(nativeModuleInfo -> "\n\t\t" + String.valueOf(nativeModuleInfo)).collect(Collectors.joining()));
    }

    public static class NativeModuleInfo {
        public final String name;
        public final Optional<NativeModuleVersion> version;

        public NativeModuleInfo(String string, Optional<NativeModuleVersion> optional) {
            this.name = string;
            this.version = optional;
        }

        public String toString() {
            return this.version.map(nativeModuleVersion -> this.name + ":" + String.valueOf(nativeModuleVersion)).orElse(this.name);
        }
    }

    public static class NativeModuleVersion {
        public final String description;
        public final String version;
        public final String company;

        public NativeModuleVersion(String string, String string2, String string3) {
            this.description = string;
            this.version = string2;
            this.company = string3;
        }

        public String toString() {
            return this.description + ":" + this.version + ":" + this.company;
        }
    }
}

