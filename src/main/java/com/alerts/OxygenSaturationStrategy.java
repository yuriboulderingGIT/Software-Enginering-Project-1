package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

public class OxygenSaturationStrategy implements AlertStrategy {
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;
    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> satRecords = filterByType(patient, "Saturation");

        checkLowSaturation(patientId, satRecords, alerts);
        checkRapidDrop(patientId, satRecords, alerts);

        return alerts;
    }

    private void checkLowSaturation(String patientId, List<PatientRecord> satRecords, List<Alert> alerts) {
        if (satRecords.isEmpty()) return;
        PatientRecord latest = satRecords.get(satRecords.size() - 1);
        if (latest.getMeasurementValue() < 92.0) {
            alerts.add(factory.createAlert(patientId, "Low blood saturation", latest.getTimestamp()));
        }
    }

    private void checkRapidDrop(String patientId, List<PatientRecord> satRecords, List<Alert> alerts) {
        for (int i = 0; i < satRecords.size(); i++) {
            double baseVal = satRecords.get(i).getMeasurementValue();
            for (int j = i + 1; j < satRecords.size(); j++) {
                PatientRecord later = satRecords.get(j);
                if (later.getTimestamp() - satRecords.get(i).getTimestamp() > TEN_MINUTES_MS) break;
                if (baseVal - later.getMeasurementValue() >= 5.0) {
                    alerts.add(factory.createAlert(patientId, "Rapid blood saturation drop", later.getTimestamp()));
                    return;
                }
            }
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
