package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for all patient data generators in the health data simulator.
 * Each implementation simulates one specific type of health data for a patient.
 * The generated data is passed to the OutputStrategy, which determines how
 *  the data is delivered.
 */

public interface PatientDataGenerator {

     /**
     * Generates a simulated health data record for the specified patient.
     *
     * @param patientId      the unique identifier of the patient
     * @param outputStrategy the output strategy to receive the generated data
     */

    void generate(int patientId, OutputStrategy outputStrategy);
}
