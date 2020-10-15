package com.stepanmazokha.ubloxparser.services;

import java.io.FileWriter;
import java.util.UUID;

public class LogWriter {

    private String fileName;

    public LogWriter(String directory) {
        this.fileName = directory + composeFileName();
    }

    public boolean log(String row) {
        try {
            FileWriter writer = new FileWriter(this.fileName, true);
            writer.write(row + "\n");
            writer.close();
            return true;
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
    }

    private String composeFileName() {
        return UUID.randomUUID() + ".csv";
    }
}
