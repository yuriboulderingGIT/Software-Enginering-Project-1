package com.alerts;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

public class BloodPressureStrategy implements AlertStrategy {
    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();

        List<PatientRecord> systolicRecords  = filterByType(patient, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(patient, "DiastolicPressure");
        List<PatientRecord> satRecords       = filterByType(patient, "Saturation");

        checkTrend(patientId, systolicRecords,  "Systolic",  alerts);
        checkTrend(patientId, diastolicRecords, "Diastolic", alerts);
        checkCritical(patientId, systolicRecords, diastolicRecords, alerts);
        checkHypotensiveHypoxemia(patientId, systolicRecords, satRecords, alerts);

        return alerts;
    }

    private void checkTrend(String patientId, List<PatientRecord> records, String label, List<Alert> alerts) {
        if (records.size() < 3) return;
        int i = records.size() - 3;
        double v1 = records.get(i).getMeasurementValue();
        double v2 = records.get(i + 1).getMeasurementValue();
        double v3 = records.get(i + 2).getMeasurementValue();

        if ((v2 - v1 > 10) && (v3 - v2 > 10)) {
            alerts.add(factory.createAlert(patientId, label + " pressure increasing trend",
                    records.get(i + 2).getTimestamp()));
        } else if ((v1 - v2 > 10) && (v2 - v3 > 10)) {
            alerts.add(factory.createAlert(patientId, label + " pressure decreasing trend",
                    records.get(i + 2).getTimestamp()));
        }
    }

    private void checkCritical(String patientId,
                                List<PatientRecord> systolicRecords,
                                List<PatientRecord> diastolicRecords,
                                List<Alert> alerts) {
        if (!systolicRecords.isEmpty()) {
            PatientRecord latest = systolicRecords.get(systolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 180)
                alerts.add(factory.createAlert(patientId, "Critical systolic pressure high", latest.getTimestamp()));
            else if (val < 90)
                alerts.add(factory.createAlert(patientId, "Critical systolic pressure low", latest.getTimestamp()));
        }
        if (!diastolicRecords.isEmpty()) {
            PatientRecord latest = diastolicRecords.get(diastolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 120)
                alerts.add(factory.createAlert(patientId, "Critical diastolic pressure high", latest.getTimestamp()));
            else if (val < 60)
                alerts.add(factory.createAlert(patientId, "Critical diastolic pressure low", latest.getTimestamp()));
        }
    }

    private void checkHypotensiveHypoxemia(String patientId,
                                            List<PatientRecord> systolicRecords,
                                            List<PatientRecord> satRecords,
                                            List<Alert> alerts) {
        if (systolicRecords.isEmpty() || satRecords.isEmpty()) return;
        double latestSystolic = systolicRecords.get(systolicRecords.size() - 1).getMeasurementValue();
        double latestSat      = satRecords.get(satRecords.size() - 1).getMeasurementValue();
        if (latestSystolic < 90 && latestSat < 92) {
            alerts.add(factory.createAlert(patientId, "Hypotensive Hypoxemia Alert", System.currentTimeMillis()));
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
