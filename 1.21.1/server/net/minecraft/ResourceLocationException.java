/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringEscapeUtils
 */
package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceLocationException
extends RuntimeException {
    public ResourceLocationException(String string) {
        super(StringEscapeUtils.escapeJava((String)string));
    }

    public ResourceLocationException(String string, Throwable throwable) {
        super(StringEscapeUtils.escapeJava((String)string), throwable);
    }
}

