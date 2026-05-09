package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Simulates blood oxygen saturation readings for patients.
 * Each patient starts with a baseline between 95% and 100%.
 * On each call the value shifts slightly and stays within 90% to 100%.
 */

public class BloodSaturationDataGenerator implements PatientDataGenerator {

    /** Used to simulate random saturation change. */
    private static final Random random = new Random();

     /** Stores the last saturation value for each patient. */
    private int[] lastSaturationValues;

    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    @Override

    /**
     * Generates a blood saturation reading for the patient and sends it to the output strategy.
     * The value changes by -1, 0, or +1 from the previous reading and stays within [90, 100].
     *
     * @param patientId      the unique identifier of the patient
     * @param outputStrategy the output strategy to receive the generated data
     */
    
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
