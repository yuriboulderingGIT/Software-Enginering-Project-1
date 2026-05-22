//also uses underscore in name but it's the name of the folders

package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;


/**
 * Simulates alert events for patients in the health data monitoring system.
 * Each patient can have an alert either active or inactive. Active alerts
 * have a 90% chance of resolving on each call. If no alert is active, a new
 * one may be triggered based on a probability calculated from a Poisson process.
 */

public class AlertGenerator implements PatientDataGenerator {

    /** Random number generator used to determine alert triggering and resolution probabilities. */
    public static final Random randomGenerator = new Random();
    /** Tracks the current alert state per patient. True means an alert is active. */
    private boolean[] alertStates; // false = resolved, true = pressed


    /**
     * Constructs an AlertGenerator for the given number of patients.
     * All patients start with no active alerts.
     *
     * @param patientCount the total number of patients to track
     */

    public AlertGenerator(int patientCount) {
        // Changed from AlertStates to alertStates - non-constant fields must use lowerCamelCase per Google Java Style Guide
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates an alert event for the specified patient and sends it to the output strategy.
     *
     * <p>If an alert is active, there is a 90% chance it will be resolved.
     * Otherwise, a new alert may be triggered based on the calculated probability.
     *
     * @param patientId      the unique identifier of the patient
     * @param outputStrategy the output strategy to receive the alert event
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Changed from Lambda to lambda - local variables must use lowerCamelCase per Google Java Style Guide
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
