package smartbell.restapi.utils;

import java.util.HashMap;
import java.util.Map;

public class FileSizeUtil {
    private enum FileSizeSuffix {
        Bytes("B"), KiloBytes("KB"), MegaBytes("MB");

        public String value;
        FileSizeSuffix(String value) {
            this.value = value;
        }
    }

    private static Map<Long, String> createMap() {
        Map<Long, String> map =  new HashMap<>();

        map.put(1L, FileSizeSuffix.Bytes.value);
        map.put(1000L, FileSizeSuffix.KiloBytes.value);
        map.put(1000000L, FileSizeSuffix.MegaBytes.value);

        return map;
    }
    private static Map<Long, String> factorToSuffixMap = createMap();

    private static String prettifyByFactorOf(long factor, long bytes) {
        long wholePart = (int) bytes / factor;
        long remainder = (int) bytes % factor;
        float floatRemainder = remainder / (factor / 10f);

        return wholePart + "." + Math.round(floatRemainder) + " " + factorToSuffixMap.get(factor);
    }

    public static String toHumanReadableSize(long bytes) {
        if(bytes < 1000) {
            return prettifyByFactorOf(1, bytes);
        } else if (bytes < 1000000) {
            return prettifyByFactorOf(1000, bytes);
        } else if (bytes < 1000000000L) {
            return prettifyByFactorOf(1000000, bytes);
        }

        throw new IllegalStateException("Too big file size");
    }
}
