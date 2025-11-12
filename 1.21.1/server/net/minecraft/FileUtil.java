/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  org.apache.commons.io.FilenameUtils
 */
package net.minecraft;

import com.mojang.serialization.DataResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FilenameUtils;

public class FileUtil {
    private static final Pattern COPY_COUNTER_PATTERN = Pattern.compile("(<name>.*) \\((<count>\\d*)\\)", 66);
    private static final int MAX_FILE_NAME = 255;
    private static final Pattern RESERVED_WINDOWS_FILENAMES = Pattern.compile(".*\\.|(?:COM|CLOCK\\$|CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?", 2);
    private static final Pattern STRICT_PATH_SEGMENT_CHECK = Pattern.compile("[-._a-z0-9]+");

    public static String sanitizeName(String string) {
        for (char c : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            string = string.replace(c, '_');
        }
        return string.replaceAll("[./\"]", "_");
    }

    public static String findAvailableName(Path path, String object, String string) throws IOException {
        if (RESERVED_WINDOWS_FILENAMES.matcher((CharSequence)(object = FileUtil.sanitizeName((String)object))).matches()) {
            object = "_" + (String)object + "_";
        }
        Matcher matcher = COPY_COUNTER_PATTERN.matcher((CharSequence)object);
        int n = 0;
        if (matcher.matches()) {
            object = matcher.group("name");
            n = Integer.parseInt(matcher.group("count"));
        }
        if (((String)object).length() > 255 - string.length()) {
            object = ((String)object).substring(0, 255 - string.length());
        }
        while (true) {
            Object object2;
            Object object3 = object;
            if (n != 0) {
                object2 = " (" + n + ")";
                int n2 = 255 - ((String)object2).length();
                if (((String)object3).length() > n2) {
                    object3 = ((String)object3).substring(0, n2);
                }
                object3 = (String)object3 + (String)object2;
            }
            object3 = (String)object3 + string;
            object2 = path.resolve((String)object3);
            try {
                Path path2 = Files.createDirectory((Path)object2, new FileAttribute[0]);
                Files.deleteIfExists(path2);
                return path.relativize(path2).toString();
            }
            catch (FileAlreadyExistsException fileAlreadyExistsException) {
                ++n;
                continue;
            }
            break;
        }
    }

    public static boolean isPathNormalized(Path path) {
        Path path2 = path.normalize();
        return path2.equals(path);
    }

    public static boolean isPathPortable(Path path) {
        for (Path path2 : path) {
            if (!RESERVED_WINDOWS_FILENAMES.matcher(path2.toString()).matches()) continue;
            return false;
        }
        return true;
    }

    public static Path createPathToResource(Path path, String string, String string2) {
        String string3 = string + string2;
        Path path2 = Paths.get(string3, new String[0]);
        if (path2.endsWith(string2)) {
            throw new InvalidPathException(string3, "empty resource name");
        }
        return path.resolve(path2);
    }

    public static String getFullResourcePath(String string) {
        return FilenameUtils.getFullPath((String)string).replace(File.separator, "/");
    }

    public static String normalizeResourcePath(String string) {
        return FilenameUtils.normalize((String)string).replace(File.separator, "/");
    }

    public static DataResult<List<String>> decomposePath(String string) {
        int n = string.indexOf(47);
        if (n == -1) {
            return switch (string) {
                case "", ".", ".." -> DataResult.error(() -> "Invalid path '" + string + "'");
                default -> !FileUtil.isValidStrictPathSegment(string) ? DataResult.error(() -> "Invalid path '" + string + "'") : DataResult.success(List.of(string));
            };
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        int n2 = 0;
        boolean bl = false;
        while (true) {
            String string2;
            switch (string2 = string.substring(n2, n)) {
                case "": 
                case ".": 
                case "..": {
                    return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + string + "'");
                }
            }
            if (!FileUtil.isValidStrictPathSegment(string2)) {
                return DataResult.error(() -> "Invalid segment '" + string2 + "' in path '" + string + "'");
            }
            arrayList.add(string2);
            if (bl) {
                return DataResult.success(arrayList);
            }
            n2 = n + 1;
            if ((n = string.indexOf(47, n2)) != -1) continue;
            n = string.length();
            bl = true;
        }
    }

    public static Path resolvePath(Path path, List<String> list) {
        int n = list.size();
        return switch (n) {
            case 0 -> path;
            case 1 -> path.resolve(list.get(0));
            default -> {
                String[] var3_3 = new String[n - 1];
                for (int var4_4 = 1; var4_4 < n; ++var4_4) {
                    var3_3[var4_4 - 1] = list.get(var4_4);
                }
                yield path.resolve(path.getFileSystem().getPath(list.get(0), var3_3));
            }
        };
    }

    public static boolean isValidStrictPathSegment(String string) {
        return STRICT_PATH_SEGMENT_CHECK.matcher(string).matches();
    }

    public static void validatePath(String ... stringArray) {
        if (stringArray.length == 0) {
            throw new IllegalArgumentException("Path must have at least one element");
        }
        for (String string : stringArray) {
            if (!string.equals("..") && !string.equals(".") && FileUtil.isValidStrictPathSegment(string)) continue;
            throw new IllegalArgumentException("Illegal segment " + string + " in path " + Arrays.toString(stringArray));
        }
    }

    public static void createDirectoriesSafe(Path path) throws IOException {
        Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
    }
}

