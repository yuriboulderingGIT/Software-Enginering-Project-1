package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.factory.AlertFactory;
import com.alerts.factory.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks a patient's ECG data and creates an alert if it detects an abnormal spike.
 * It uses the Strategy design pattern.
 *
 * <p>The way it works: for each ECG reading, we calculate the average of the 20 readings before it.
 * If the current reading is more than 2 times bigger than that average, we call it an anomaly.
 */
public class HeartRateStrategy implements AlertStrategy {

    // how many times bigger a reading needs to be to count as a spike
    private static final double ECG_SPIKE_THRESHOLD = 2.0;

    // how many previous readings we use to calculate the average
    private static final int ECG_WINDOW_SIZE = 20;

    // I use this factory to make ECG alerts
    private AlertFactory factory = new ECGAlertFactory();

    /**
     * Checks the patient's ECG readings for any anomalies and returns alerts if found.
     *
     * @param patient the patient we want to check
     * @return a list of alerts, or an empty list if everything looks fine
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();

        List<PatientRecord> ecgRecords = filterByType(patient, "ECG");

        // we need more than 20 records before we can start checking
        if (ecgRecords.size() <= ECG_WINDOW_SIZE) {
            return alerts;
        }

        // check each reading starting from index 20
        for (int i = ECG_WINDOW_SIZE; i < ecgRecords.size(); i++) {

            // calculate the average of the 20 readings before this one
            double sum = 0;
            for (int j = i - ECG_WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(ecgRecords.get(j).getMeasurementValue());
            }
            double avg = sum / ECG_WINDOW_SIZE;

            double current = Math.abs(ecgRecords.get(i).getMeasurementValue());

            // if current reading is way bigger than the average, it's a spike
            if (avg > 0 && current > ECG_SPIKE_THRESHOLD * avg) {
                alerts.add(factory.createAlert(patientId, "ECG anomaly detected", ecgRecords.get(i).getTimestamp()));
                return alerts;
            }
        }

        return alerts;
    }

    /**
     * Goes through all a patient's records and returns only the ones with the given type.
     *
     * @param patient the patient whose records we search through
     * @param type the record type we want, for example "ECG"
     * @return a new list that only contains records matching the type
     */
    private List<PatientRecord> filterByType(Patient patient, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0L, Long.MAX_VALUE)) {
            if (r.getRecordType().equals(type)) {
                result.add(r);
            }
        }
        return result;
    }
}
