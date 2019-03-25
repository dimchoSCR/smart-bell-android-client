package apps.dcoder.smartbellcontrol.restapiclient.model;

public class RawRingEntry {

    private long id;
    private String melodyName;
    private String dateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "RawRingEntry{" +
                "id=" + id +
                ", melodyName='" + melodyName + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
