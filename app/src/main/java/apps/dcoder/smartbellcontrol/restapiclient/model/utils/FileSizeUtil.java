package apps.dcoder.smartbellcontrol.restapiclient.model.utils;


import android.content.Context;
import apps.dcoder.smartbellcontrol.R;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FileSizeUtil {

    private static char getDecimalSeparatorForCurrentLocale() {
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(currentLocale);

        return formatSymbols.getDecimalSeparator();
    }

    private static String prettifyByFactorOf(long factor, long bytes, String sizeSuffix) {
        long wholePart = (int) bytes / factor;
        long remainder = (int) bytes % factor;
        float floatRemainder = remainder / (factor / 10f);

        return String.valueOf(wholePart) +
                getDecimalSeparatorForCurrentLocale() +
                Math.round(floatRemainder) +
                ' ' +
               sizeSuffix;
    }

    public static String toHumanReadableSize(long bytes, Context context) {
        if(bytes < 1000) {
            return prettifyByFactorOf(1, bytes, context.getString(R.string.byte_suffix));
        } else if (bytes < 1000000) {
            return prettifyByFactorOf(1000, bytes, context.getString(R.string.kilo_byte_suffix));
        } else if (bytes < 1000000000L) {
            return prettifyByFactorOf(1000000, bytes, context.getString(R.string.mega_byte_suffix));
        }

        throw new IllegalStateException("Too big file size");
    }
}
