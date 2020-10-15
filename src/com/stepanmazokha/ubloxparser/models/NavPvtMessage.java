package com.stepanmazokha.ubloxparser.models;

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
    public double lat;
    public double lng;
    public int height;
    public int heightMSL;
    public int numSV;
    public long time;

    @Override
    public String toString() {
        return fixType.toString() + " " + lat + " " + lng + " [" + numSV + "]";
    }

    public String toCsvRow() {
        return fixType.toString() + "\t" + time + "\t" + lat + "\t" + lng + "\t" + numSV + "\t" + height + "\t" + heightMSL;
    }

    public static String getCsvHeaders() {
        return "Fix\tTime (millis)\tLat\tLng\tNum of Sats\tHeight\tHeight MSL";
    }
}
