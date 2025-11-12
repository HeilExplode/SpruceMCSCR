/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringEscapeUtils
 */
package net.minecraft.util;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;

public class CsvOutput {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String FIELD_SEPARATOR = ",";
    private final Writer output;
    private final int columnCount;

    CsvOutput(Writer writer, List<String> list) throws IOException {
        this.output = writer;
        this.columnCount = list.size();
        this.writeLine(list.stream());
    }

    public static Builder builder() {
        return new Builder();
    }

    public void writeRow(Object ... objectArray) throws IOException {
        if (objectArray.length != this.columnCount) {
            throw new IllegalArgumentException("Invalid number of columns, expected " + this.columnCount + ", but got " + objectArray.length);
        }
        this.writeLine(Stream.of(objectArray));
    }

    private void writeLine(Stream<?> stream) throws IOException {
        this.output.write(stream.map(CsvOutput::getStringValue).collect(Collectors.joining(FIELD_SEPARATOR)) + LINE_SEPARATOR);
    }

    private static String getStringValue(@Nullable Object object) {
        return StringEscapeUtils.escapeCsv((String)(object != null ? object.toString() : "[null]"));
    }

    public static class Builder {
        private final List<String> headers = Lists.newArrayList();

        public Builder addColumn(String string) {
            this.headers.add(string);
            return this;
        }

        public CsvOutput build(Writer writer) throws IOException {
            return new CsvOutput(writer, this.headers);
        }
    }
}

