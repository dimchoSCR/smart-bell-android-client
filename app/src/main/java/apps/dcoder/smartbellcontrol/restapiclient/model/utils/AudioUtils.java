package smartbell.restapi.utils;

public class AudioUtils {

    public static final String OGG_CONTENT_TYPE= "audio/vorbis";

    private static final String[] timeFormatterArray = {"00:", "0", "", ":"};

    private static int[] prettifyMilliseconds(long durationMillis) {
        int unFormattedSeconds = Math.round(durationMillis / 1000f);
        int hours = unFormattedSeconds / 3600;
        int unFormattedMinutes = unFormattedSeconds % 3600;
        int minutes = unFormattedMinutes / 60;
        int seconds = unFormattedMinutes % 60;

        return new int[] { seconds, minutes, hours };
    }

    private static String formatTimeDigit(int timeDigit) {
        int digitLength = String.valueOf(timeDigit).length();
        return timeFormatterArray[digitLength] + timeDigit;
    }

    public static String toHumanReadableDuration(String xmpDMDuration, String contentType) {
        if(xmpDMDuration == null) {
            return "Unknown";
        }

        // If no '.' symbol exists the original string is returned
        String[] splitDuration = xmpDMDuration.split("\\."); // Sample duration string: 32324.2323
        // For some reason apache tika parses the xmpDMDuration in seconds for ogg and in milliseconds for mp3
        long durationWholePartMillis;
        if(contentType.equalsIgnoreCase(OGG_CONTENT_TYPE)) {
            durationWholePartMillis = Long.parseLong(splitDuration[0]) * 1000;
        } else {
            durationWholePartMillis = Long.parseLong(splitDuration[0]);
        }

        int[] timeArr = prettifyMilliseconds(durationWholePartMillis);

        int seconds = timeArr[0];
        int minutes = timeArr[1];
        int hours = timeArr[2];
        if(durationWholePartMillis < 60000) { // Case seconds
            return timeFormatterArray[0] + formatTimeDigit(seconds);
        } else if (durationWholePartMillis < 3600000L) { // Case minutes and seconds
           return formatTimeDigit(minutes) + timeFormatterArray[3] + formatTimeDigit(seconds);
        } else if(durationWholePartMillis < 86400000L){ // Case hours minutes and seconds
            return formatTimeDigit(hours) + timeFormatterArray[3] +
                    formatTimeDigit(minutes) + timeFormatterArray[3] +
                    formatTimeDigit(seconds);
        }

        throw new IllegalStateException("Can not format time string! Time string exceeds 24 hours!");
    }
}
