package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    // How many times larger than the average an ECG value must be to trigger an alert
    private static final double ECG_SPIKE_THRESHOLD = 2.0;
    // Sliding window size for ECG average
    private static final int ECG_WINDOW_SIZE = 20;
    // 10-minute window in milliseconds for rapid saturation drop check
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

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
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        long now = Long.MAX_VALUE;
        long windowStart = 0L;

        List<PatientRecord> systolicRecords  = filterByType(patient, "SystolicPressure",  windowStart, now);
        List<PatientRecord> diastolicRecords = filterByType(patient, "DiastolicPressure", windowStart, now);
        List<PatientRecord> satRecords       = filterByType(patient, "Saturation",        windowStart, now);
        List<PatientRecord> ecgRecords       = filterByType(patient, "ECG",               windowStart, now);
        List<PatientRecord> alertRecords     = filterByType(patient, "Alert",             windowStart, now);

        checkBloodPressureTrend(patientId, systolicRecords,  "Systolic");
        checkBloodPressureTrend(patientId, diastolicRecords, "Diastolic");
        checkBloodPressureCritical(patientId, systolicRecords, diastolicRecords);
        checkLowSaturation(patientId, satRecords);
        checkRapidSaturationDrop(patientId, satRecords);
        checkHypotensiveHypoxemia(patientId, systolicRecords, satRecords);
        checkEcgAnomaly(patientId, ecgRecords);
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


    // Private helper methods

    private void checkBloodPressureTrend(String patientId, List<PatientRecord> records, String label) {
        if (records.size() < 3) return;

        for (int i = records.size() - 3; i < records.size() - 2; i++) {
            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();

            if ((v2 - v1 > 10) && (v3 - v2 > 10)) {
                triggerAlert(new Alert(patientId, label + " pressure increasing trend",
                        records.get(i + 2).getTimestamp()));
            } else if ((v1 - v2 > 10) && (v2 - v3 > 10)) {
                triggerAlert(new Alert(patientId, label + " pressure decreasing trend",
                        records.get(i + 2).getTimestamp()));
            }
        }
    }

    private void checkBloodPressureCritical(String patientId,
                                             List<PatientRecord> systolicRecords,
                                             List<PatientRecord> diastolicRecords) {
        if (!systolicRecords.isEmpty()) {
            PatientRecord latest = systolicRecords.get(systolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 180) triggerAlert(new Alert(patientId, "Critical systolic pressure high", latest.getTimestamp()));
            else if (val < 90) triggerAlert(new Alert(patientId, "Critical systolic pressure low", latest.getTimestamp()));
        }

        if (!diastolicRecords.isEmpty()) {
            PatientRecord latest = diastolicRecords.get(diastolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 120) triggerAlert(new Alert(patientId, "Critical diastolic pressure high", latest.getTimestamp()));
            else if (val < 60) triggerAlert(new Alert(patientId, "Critical diastolic pressure low", latest.getTimestamp()));
        }
    }

    private void checkLowSaturation(String patientId, List<PatientRecord> satRecords) {
        if (satRecords.isEmpty()) return;
        PatientRecord latest = satRecords.get(satRecords.size() - 1);
        if (latest.getMeasurementValue() < 92.0) {
            triggerAlert(new Alert(patientId, "Low blood saturation", latest.getTimestamp()));
        }
    }

    private void checkRapidSaturationDrop(String patientId, List<PatientRecord> satRecords) {
        for (int i = 0; i < satRecords.size(); i++) {
            double baseVal = satRecords.get(i).getMeasurementValue();
            for (int j = i + 1; j < satRecords.size(); j++) {
                PatientRecord later = satRecords.get(j);
                if (later.getTimestamp() - satRecords.get(i).getTimestamp() > TEN_MINUTES_MS) break;
                if (baseVal - later.getMeasurementValue() >= 5.0) {
                    triggerAlert(new Alert(patientId, "Rapid blood saturation drop", later.getTimestamp()));
                    return;
                }
            }
        }
    }

    private void checkHypotensiveHypoxemia(String patientId,
                                            List<PatientRecord> systolicRecords,
                                            List<PatientRecord> satRecords) {
        if (systolicRecords.isEmpty() || satRecords.isEmpty()) return;
        double latestSystolic = systolicRecords.get(systolicRecords.size() - 1).getMeasurementValue();
        double latestSat      = satRecords.get(satRecords.size() - 1).getMeasurementValue();
        if (latestSystolic < 90 && latestSat < 92) {
            triggerAlert(new Alert(patientId, "Hypotensive Hypoxemia Alert", System.currentTimeMillis()));
        }
    }

    private void checkEcgAnomaly(String patientId, List<PatientRecord> ecgRecords) {
        if (ecgRecords.size() < ECG_WINDOW_SIZE) return;
        for (int i = ECG_WINDOW_SIZE; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - ECG_WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(ecgRecords.get(j).getMeasurementValue());
            }
            double avg = sum / ECG_WINDOW_SIZE;
            double current = Math.abs(ecgRecords.get(i).getMeasurementValue());
            if (avg > 0 && current > ECG_SPIKE_THRESHOLD * avg) {
                triggerAlert(new Alert(patientId, "ECG anomaly detected", ecgRecords.get(i).getTimestamp()));
                return;
            }
        }
    }

    private void checkTriggeredAlert(String patientId, List<PatientRecord> alertRecords) {
        if (alertRecords.isEmpty()) return;
        PatientRecord latest = alertRecords.get(alertRecords.size() - 1);
        if (latest.getMeasurementValue() == 1.0) {
            triggerAlert(new Alert(patientId, "Manual alert triggered", latest.getTimestamp()));
        }
    }

    private List<PatientRecord> filterByType(Patient patient, String type, long start, long end) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(start, end)) {
            if (r.getRecordType().equals(type)) {
                result.add(r);
            }
        }
        return result;
    }
}