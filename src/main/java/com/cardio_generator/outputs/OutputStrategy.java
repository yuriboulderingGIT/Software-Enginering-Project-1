package com.cardio_generator.outputs;

/**
 * Interface for all output strategies in the health data simulator.
 * Implementations define how the recorded patient data is delivered.
 */

public interface OutputStrategy {

/**
     * Outputs a single data record for a patient.
     *
     * @param patientId the unique ID of the patient whose data is being output
     * @param timestamp the time at which the data was recorded
     * @param label     the category of the data record
     * @param data      the content of the data record
     */

    void output(int patientId, long timestamp, String label, String data);


}
