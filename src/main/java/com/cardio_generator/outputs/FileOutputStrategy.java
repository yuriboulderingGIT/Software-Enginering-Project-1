//package name uses underscore (name of the folder) which should be camelCase

package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes patient data records to text files.
 * Each data label gets its own file in the base directory.
 * Files are created if they don't exist yet and new records are added.
 */

public class FileOutputStrategy implements OutputStrategy {
     /** The folder where all output files will be saved. */
    // Changed from BaseDirectory to baseDirectory - fields should use camelCase per Google Java Style Guide
    private String baseDirectory;

     /** Maps each label to its output file path so each label always writes to the same file. */
     // Changed from file_map to fileMap - fields should use camelCase, no underscores per Google Java Style Guide
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();


    /**
     * Creates a FileOutputStrategy that saves files in the given directory.
     *
     * @param baseDirectory path to the output folder
     */
    public FileOutputStrategy(String baseDirectory) {
        
        this.baseDirectory = baseDirectory;
    }

    /**
     * Writes a patient data record to the file for the given label.
     * Format: Patient ID: X, Timestamp: Y, Label: Z, Data: W
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the recording time in milliseconds since epoch
     * @param label     the data category, also used as the filename
     * @param data      the value of the data record
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the filePath variable
        // Changed from FilePath to filePath - local variables should use camelCase per Google Java Style Guide
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}