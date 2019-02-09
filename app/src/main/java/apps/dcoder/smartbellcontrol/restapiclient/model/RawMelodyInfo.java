package apps.dcoder.smartbellcontrol.restapiclient.model;

import apps.dcoder.smartbellcontrol.restapiclient.model.utils.FileSizeUtil;

public class MelodyInfo {

    private String melodyName;
    private String duration;
    private String contentType;

    private long fileSize;
    private boolean isRingtone;

    private String fileSizeString;
    private String formattedDuration;

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public String getLocalizedFileSizeString() {
        return fileSizeString;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public void setRingtone(boolean ringtone) {
        isRingtone = ringtone;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
