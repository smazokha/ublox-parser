package com.stepanmazokha.ubloxparser;

import com.stepanmazokha.ubloxparser.models.NavPvtMessage;
import com.stepanmazokha.ubloxparser.services.RTKReader;
import com.stepanmazokha.ubloxparser.services.LogWriter;

public class Main {

    private static final int BAUD_RATE = 9600;
    private static final String MAC_PORT_NAME = "COM14";//"tty.usbmodem";
    private static final String MAC_LOG_FILE = "C://Users/Stepan/Desktop/Logs/";//"/Users/stepanmazokha/Desktop/logs/";

    /**
     * Expected args:
     * [0]: (string) port name (i.e. tty.usbmodem144101)
     * [1]: (string) log file path + name (i.e. ~/rtk_log.csv)
     */
    public static void main(String[] args) {
        String portName, logFilePath;
        if (args != null && args.length == 2) {
            portName = args[0];
            logFilePath = args[1];
        } else {
            System.out.println("Valid args not found. Using default settings.");
            portName = MAC_PORT_NAME;
            logFilePath = MAC_LOG_FILE;
        }

        RTKReader rtk = new RTKReader(BAUD_RATE);
        LogWriter logger = new LogWriter(logFilePath);

        if (!rtk.findPort(portName)) {
            System.out.println("Port " + portName + " wasn't found. Re-connect & try again.");
            return;
        }

        try {
            if (rtk.open()) {
                logger.log(NavPvtMessage.getCsvHeaders());
                System.out.println(NavPvtMessage.getCsvHeaders());

                while (true) {
                    NavPvtMessage message = rtk.readMessage();
                    if (message == null) continue;

                    System.out.print(message.toCsvRow());
                    logger.log(message.toCsvRow());
                }
            } else {
                System.out.println("Failed to open connection.");
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            rtk.close();
        }
    }
}
