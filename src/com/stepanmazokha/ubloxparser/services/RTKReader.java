package com.stepanmazokha.ubloxparser.services;

import com.fazecast.jSerialComm.SerialPort;
import com.stepanmazokha.ubloxparser.models.NavPvtMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A parser of u-Blox UBX-NAV-PVT message.
 * Documentation: https://www.u-blox.com/sites/default/files/products/documents/u-blox8-M8_ReceiverDescrProtSpec_%28UBX-13003221%29.pdf
 * Page: 332 (in the book), 346 (in PDF)
 */
public class RTKReader {

    private static final char[] UBX_HEADER_CLASS_ID = {0xB5, 0x62, 0x01, 0x07};
    private static final int PVT_PAYLOAD_LENGTH = 92;

    private int baudRate;
    private SerialPort serial;
    private InputStream stream;

    public RTKReader(int baudRate) {
        this.baudRate = baudRate;
    }

    public boolean findPort(String portName) {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            if (port.getSystemPortName().contains(portName)) {
                serial = port;
                return true;
            }
        }
        return false;
    }

    public boolean open() throws IOException {
        if (serial == null)
            throw new IOException("Serial port is not initialized. Use #findPort() before invoking this function.");

        serial.setBaudRate(baudRate);
        serial.openPort();
        serial.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        stream = serial.getInputStream();

        return true;
    }

    public void close() {
        if (stream == null) return;

        try {
            stream.close();
        } catch (Exception exc) {
            System.out.println("Failed to close the stream.");
            exc.printStackTrace();
        }
    }

    public NavPvtMessage readMessage() throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("InputStream isn't open.");
        }

        NavPvtMessage pvt = new NavPvtMessage();

        // SYNC CHAR 1 [1 byte]
        int syncChar1 = stream.read();
        if (UBX_HEADER_CLASS_ID[0] != syncChar1) return null;

        // SYNC CHAR 2 [1 byte]
        int syncChar2 = stream.read();
        if (UBX_HEADER_CLASS_ID[1] != syncChar2) return null;

        // CLASS       [1 byte]
        int msgClass = stream.read();
        if (UBX_HEADER_CLASS_ID[2] != msgClass) return null;

        // ID          [1 byte]
        int id = stream.read();
        if (UBX_HEADER_CLASS_ID[3] != id) return null;

        // LENGTH      [2 bytes]
        int length = readShort(stream);
        if (length != PVT_PAYLOAD_LENGTH) return null;

        // PAYLOAD     [LENGTH bytes]
        pvt.towMs = readInt(stream); // GPS time of week of the navigation epoch
        pvt.setTime(
                readShort(stream), /* year */
                stream.read(), /* month */
                stream.read(), /* day */
                stream.read(), /* hour */
                stream.read(), /* minute */
                stream.read()); /* second */
        skip(stream, 1); // byte[] valid = readBytes(serial, 1);
        pvt.tAccNs = readInt(stream); // time accuracy estimate (UTC), ns
        skip(stream, 4); // byte[] nano = readBytes(serial, 4);
        pvt.fixType = readFixType(stream);
        skip(stream, 1); // byte[] flags = readBytes(serial, 1);
        skip(stream, 1); // byte[] flags2 = readBytes(serial, 1);
        pvt.numSV = stream.read(); // Number of satellites used in Nav Solution
        pvt.lngDeg = readInt(stream) * 1e-7; // deg
        pvt.latDeg = readInt(stream) * 1e-7; // deg
        pvt.heightMm = readInt(stream); // mm
        pvt.heightMslMm = readInt(stream); // mm
        pvt.hAccMm = readInt(stream); // horizontal accuracy estimate, mm
        pvt.vAccMm = readInt(stream); // vertical accuracy estimate, mm
        skip(stream, 4); // int velN = readInt(serial, 4); // mm/s
        skip(stream, 4); // int velE = readInt(serial, 4); // mm/s
        skip(stream, 4); // int velD = readInt(serial, 4); // mm/s
        skip(stream, 4); // byte[] gSpeed = readBytes(serial, 4);
        skip(stream, 4); // byte[] heatMot = readBytes(serial, 4);
        skip(stream, 4); // byte[] sAcc = readBytes(serial, 4);
        skip(stream, 4); // byte[] headAcc = readBytes(serial, 4);
        pvt.pDop = readShort(stream) * 0.01; // dilution of precision, scaling: 0.01
        skip(stream, 1); // byte[] flags3 = readBytes(serial, 1);
        skip(stream, 5); // reserved
        skip(stream, 4); // byte[] headVeh = readBytes(serial, 4);
        skip(stream, 2); // byte[] macDec = readBytes(serial, 2);
        pvt.macAccDeg = readShort(stream) * 1e-2; // Magnetic declination accuracy, deg

        // CHECKSUM    [2 bytes]
        byte[] checksum = readBytes(stream, 2);

        return pvt;
    }

    private int readShort(InputStream in) throws IOException {
        byte[] b = readBytes(in, 2);
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);
        return wrapped.getShort();
    }

    private int readInt(InputStream in) throws IOException {
        byte[] b = readBytes(in, 4);
        ByteBuffer wrapped = ByteBuffer.wrap(b);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);
        return wrapped.getInt();
    }

    private byte[] readBytes(InputStream in, int lengthBytes) throws IOException {
        byte[] result = new byte[lengthBytes];
        in.read(result, 0, lengthBytes);
        return result;
    }

    private NavPvtMessage.FixType readFixType(InputStream in) throws IOException {
        int code = in.read();
        switch (code) {
            case 0:
                return NavPvtMessage.FixType.NoFix;
            case 1:
                return NavPvtMessage.FixType.DeadReckoning;
            case 2:
                return NavPvtMessage.FixType.Fix2D;
            case 3:
                return NavPvtMessage.FixType.Fix3D;
            case 4:
                return NavPvtMessage.FixType.GnssDeadReckoning;
            case 5:
                return NavPvtMessage.FixType.TimeOnly;
            default:
                return NavPvtMessage.FixType.Undefined;
        }
    }

    private void skip(InputStream in, int lengthBytes) throws IOException {
        readBytes(in, lengthBytes);
    }
}
