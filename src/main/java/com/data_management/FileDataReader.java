package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implements DataReader by reading patient data from text files in a directory.
 *
 * Expected file format — one record per line:
 *   patientId, timestamp, label, data
 * Example:
 *   1, 1714376800000, HeartRate, 75.0
 *
 * This implementation is used when the HealthDataSimulator is run with the
 * --output file:<output_dir> argument.  It scans the entire directory and
 * processes every .txt file it finds.
 */
public class FileDataReader implements DataReader {

    private final String directoryPath;

    /**
     * Constructs a FileDataReader that will look for data files in the given directory.
     *
     * @param directoryPath the path to the directory containing patient data files
     */
    public FileDataReader(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Reads all .txt files in the configured directory, parses each line,
     * and stores the resulting records in the provided DataStorage.
     *
     * Lines that cannot be parsed (wrong format, non-numeric values, etc.)
     * are logged and skipped rather than aborting the entire read.
     *
     * @param dataStorage the storage object where parsed patient data will be stored
     * @throws IOException if the directory does not exist or cannot be read
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path dir = Paths.get(directoryPath);

        // Validate that the directory exists and is accessible
        if (!Files.exists(dir)) {
            throw new IOException("Data directory does not exist: " + directoryPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new IOException("Provided path is not a directory: " + directoryPath);
        }

        // Iterate over every file in the directory
        File[] files = dir.toFile().listFiles();
        if (files == null) {
            throw new IOException("Cannot list files in directory: " + directoryPath);
        }

        for (File file : files) {
            // Only process text files
            if (file.isFile() && file.getName().endsWith(".txt")) {
                parseFile(file, dataStorage);
            }
        }
    }

    /**
     * Reads a single data file and adds each valid record to DataStorage.
     *
     * @param file        the file to read
     * @param dataStorage the storage to populate
     * @throws IOException if the file cannot be opened or read
     */
    private void parseFile(File file, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip blank lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                parseLine(line, file.getName(), lineNumber, dataStorage);
            }
        }
    }

    /**
     * Parses a single comma-separated data line and stores it in DataStorage.
     *
     * Expected format:  patientId, timestamp, label, value
     *
     * On any parse error the line is skipped with a warning rather than
     * crashing the entire read operation — this makes the reader robust
     * against occasional corrupted records.
     *
     * @param line        the raw text line to parse
     * @param fileName    used only in error messages for easier debugging
     * @param lineNumber  used only in error messages for easier debugging
     * @param dataStorage the storage to write the parsed record into
     */
    private void parseLine(String line, String fileName, int lineNumber, DataStorage dataStorage) {
        // Split on comma; allow optional surrounding whitespace around each token
        String[] parts = line.split(",");

        if (parts.length != 4) {
            System.err.println("[FileDataReader] Skipping malformed line " + lineNumber
                    + " in " + fileName + " (expected 4 fields, got " + parts.length + "): " + line);
            return;
        }

        try {
            int    patientId  = Integer.parseInt(parts[0].trim());
            long   timestamp  = Long.parseLong(parts[1].trim());
            String label      = parts[2].trim();
            double value      = Double.parseDouble(parts[3].trim());

            if (label.isEmpty()) {
                System.err.println("[FileDataReader] Skipping line " + lineNumber
                        + " in " + fileName + ": label is empty.");
                return;
            }

            dataStorage.addPatientData(patientId, value, label, timestamp);

        } catch (NumberFormatException e) {
            System.err.println("[FileDataReader] Skipping line " + lineNumber
                    + " in " + fileName + " due to number format error: " + e.getMessage());
        }
    }
}