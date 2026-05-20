package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {
    private static final double ECG_SPIKE_THRESHOLD = 2.0;
    private static final int ECG_WINDOW_SIZE = 20;
    private final AlertFactory factory = new ECGAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> ecgRecords = filterByType(patient, "ECG");

        if (ecgRecords.size() <= ECG_WINDOW_SIZE) return alerts;

        for (int i = ECG_WINDOW_SIZE; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - ECG_WINDOW_SIZE; j < i; j++) {
                sum += Math.abs(ecgRecords.get(j).getMeasurementValue());
            }
            double avg = sum / ECG_WINDOW_SIZE;
            double current = Math.abs(ecgRecords.get(i).getMeasurementValue());
            if (avg > 0 && current > ECG_SPIKE_THRESHOLD * avg) {
                alerts.add(factory.createAlert(patientId, "ECG anomaly detected", ecgRecords.get(i).getTimestamp()));
                return alerts;
            }
        }
        return alerts;
    }

    private List<PatientRecord> filterByType(Patient patient, String type) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord r : patient.getRecords(0L, Long.MAX_VALUE)) {
            if (r.getRecordType().equals(type)) result.add(r);
        }
        return result;
    }
}
