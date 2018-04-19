package me.wulfmarius.modinstaller.utils;

import java.text.MessageFormat;

public class StringUtils {

    private static final int GI_BYTE = 1024 * 1024 * 1024;
    private static final int MI_BYTE = 1024 * 1024;
    private static final int KI_BYTE = 1024;

    public static String formatByteCount(long byteCount) {
        if (byteCount > GI_BYTE) {
            return MessageFormat.format("{0,number,0.00} GiB", (double) byteCount / GI_BYTE);
        }

        if (byteCount > MI_BYTE) {
            return MessageFormat.format("{0,number,0.0} MiB", (double) byteCount / MI_BYTE);
        }

        if (byteCount > KI_BYTE) {
            return MessageFormat.format("{0,number,0.0} KiB", (double) byteCount / KI_BYTE);
        }

        return MessageFormat.format("{0} B", byteCount);
    }

    public static String shortenPath(String path) {
        if (path.length() < 60) {
            return path;
        }

        int firstPartLength = 30;
        int lastPathLength = 30;

        return path.substring(0, firstPartLength) + "..." + path.substring(path.length() - lastPathLength, path.length());
    }

    public static String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }

        return value.trim();
    }
}
