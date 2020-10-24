package com.stepanmazokha.ubloxparser.models;

import java.util.Calendar;
import java.util.TimeZone;

public class NavPvtMessage {
    public enum FixType {
        NoFix,
        DeadReckoning,
        Fix2D,
        Fix3D,
        GnssDeadReckoning,
        TimeOnly,
        Undefined;
    }

    public FixType fixType;
    public double latDeg;
    public double lngDeg;
    public int heightMm;
    public int heightMslMm;
    public int numSV;
    public long towMs;
    private long time;
    public long tAccNs;
    public long vAccMm;
    public long hAccMm;
    public double pDop;
    public double macAccDeg;

    /**
     * Parse message time, assuming that it's given in UTC.
     */
    public void setTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.set(year, month - 1, day, hour, minute, second);
        time.set(Calendar.MILLISECOND, 0);
        this.time = time.getTimeInMillis();
    }

    public static String getCsvHeaders() {
        return "Fix\tTime (millis)\tLat\tLng\tNum of Sats\tHeight (m)\tHeight MSL (m)\tTime Accuracy (ns)\tVertical Accuracy (m)\tHorizontal Accuracy (m)\tDoP";
    }

    public String toCsvRow() {
        return fixType.toString() + "\t" + time + "\t" + latDeg + "\t" + lngDeg + "\t" + numSV + "\t" + heightMm / 1000. + "\t" + heightMslMm / 1000. + "\t" + tAccNs + "\t" + vAccMm / 1000. + "\t" + hAccMm / 1000. + "\t" + pDop;
    }

    @Override
    public String toString() {
       return this.toCsvRow();
    }
}
