package com.alerts;

import com.alerts.strategy.AlertStrategy;
import com.alerts.strategy.BloodPressureStrategy;
import com.alerts.strategy.OxygenSaturationStrategy;
import com.alerts.strategy.HeartRateStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;
    private List<AlertStrategy> strategies;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.strategies = new ArrayList<>(Arrays.asList(
                new BloodPressureStrategy(),
                new OxygenSaturationStrategy(),
                new HeartRateStrategy()
        ));
    }

    /**
     * Replaces the strategy list. Useful for injecting test doubles.
     *
     * @param strategies the list of strategies to use
     */
    public void setStrategies(List<AlertStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert} method. This method should define the specific
     * conditions under which an alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        for (AlertStrategy strategy : strategies) {
            for (Alert alert : strategy.checkAlert(patient)) {
                triggerAlert(alert);
            }
        }

        // Manual alert check — not delegated to a strategy
        String patientId = String.valueOf(patient.getPatientId());
        List<PatientRecord> alertRecords = filterByType(patient, "Alert");
        checkTriggeredAlert(patientId, alertRecords);
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("ALERT [Patient " + alert.getPatientId() + "] "
                + alert.getCondition()
                + " at " + alert.getTimestamp());
    }

    private void checkTriggeredAlert(String patientId, List<PatientRecord> alertRecords) {
        if (alertRecords.isEmpty()) return;
        PatientRecord latest = alertRecords.get(alertRecords.size() - 1);
        if (latest.getMeasurementValue() == 1.0) {
            triggerAlert(new Alert(patientId, "Manual alert triggered", latest.getTimestamp()));
        }
    }

    private List<PatientRecord> filterByType(Patient patient, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0L, Long.MAX_VALUE)) {
            if (r.getRecordType().equals(type)) result.add(r);
        }
        return result;
    }
}
