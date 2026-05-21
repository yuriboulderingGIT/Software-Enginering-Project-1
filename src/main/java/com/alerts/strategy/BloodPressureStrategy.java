package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.factory.AlertFactory;
import com.alerts.factory.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks a patient's blood pressure and creates alerts if something looks wrong.
 * It implements the Strategy design pattern, which basically means it has one main method
 * that does all the checking and returns a list of alerts.
 *
 * <p>It checks three things:
 * 1. Is there an increasing or decreasing trend in the last 3 readings?
 * 2. Is the latest reading critically high or low?
 * 3. Are both blood pressure AND oxygen saturation dangerously low at the same time?
 */
public class BloodPressureStrategy implements AlertStrategy {

    // I use this factory to make blood pressure alerts
    private AlertFactory factory = new BloodPressureAlertFactory();

    /**
     * Runs all the blood pressure checks for a patient and returns any alerts found.
     *
     * @param patient the patient we want to check
     * @return a list of alerts, or an empty list if everything looks fine
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();

        // get the different types of records we need
        List<PatientRecord> systolicRecords = filterByType(patient, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(patient, "DiastolicPressure");
        List<PatientRecord> satRecords = filterByType(patient, "Saturation");

        // run all our checks and add any alerts to the list
        checkTrend(patientId, systolicRecords, "Systolic", alerts);
        checkTrend(patientId, diastolicRecords, "Diastolic", alerts);
        checkCritical(patientId, systolicRecords, diastolicRecords, alerts);
        checkHypotensiveHypoxemia(patientId, systolicRecords, satRecords, alerts);

        return alerts;
    }

    /**
     * Checks if the last 3 readings are going up or down by more than 10 each time.
     * If yes, that's a dangerous trend and we create an alert.
     *
     * @param patientId the id of the patient as a string
     * @param records the list of readings we want to check
     * @param label either "Systolic" or "Diastolic" so we know which one to put in the alert message
     * @param alerts the list we add new alerts to
     */
    private void checkTrend(String patientId, List<PatientRecord> records, String label, List<Alert> alerts) {
        // we need at least 3 readings to check for a trend
        if (records.size() < 3) {
            return;
        }

        // grab the last 3 readings
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

    /**
     * Checks if the most recent systolic or diastolic reading is critically high or low.
     * Systolic should be between 90 and 180, diastolic between 60 and 120.
     *
     * @param patientId the id of the patient as a string
     * @param systolicRecords list of systolic pressure readings
     * @param diastolicRecords list of diastolic pressure readings
     * @param alerts the list we add new alerts to
     */
    private void checkCritical(String patientId, List<PatientRecord> systolicRecords,
                                List<PatientRecord> diastolicRecords, List<Alert> alerts) {
        if (!systolicRecords.isEmpty()) {
            PatientRecord latest = systolicRecords.get(systolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 180) {
                alerts.add(factory.createAlert(patientId, "Critical systolic pressure high", latest.getTimestamp()));
            } else if (val < 90) {
                alerts.add(factory.createAlert(patientId, "Critical systolic pressure low", latest.getTimestamp()));
            }
        }

        if (!diastolicRecords.isEmpty()) {
            PatientRecord latest = diastolicRecords.get(diastolicRecords.size() - 1);
            double val = latest.getMeasurementValue();
            if (val > 120) {
                alerts.add(factory.createAlert(patientId, "Critical diastolic pressure high", latest.getTimestamp()));
            } else if (val < 60) {
                alerts.add(factory.createAlert(patientId, "Critical diastolic pressure low", latest.getTimestamp()));
            }
        }
    }

    /**
     * Checks for hypotensive hypoxemia - this is when both blood pressure AND oxygen are low at the same time.
     * It's a serious condition so we always alert when both are below their thresholds.
     *
     * @param patientId the id of the patient as a string
     * @param systolicRecords list of systolic pressure readings
     * @param satRecords list of oxygen saturation readings
     * @param alerts the list we add new alerts to
     */
    private void checkHypotensiveHypoxemia(String patientId, List<PatientRecord> systolicRecords,
                                            List<PatientRecord> satRecords, List<Alert> alerts) {
        if (systolicRecords.isEmpty() || satRecords.isEmpty()) {
            return;
        }

        double latestSystolic = systolicRecords.get(systolicRecords.size() - 1).getMeasurementValue();
        double latestSat = satRecords.get(satRecords.size() - 1).getMeasurementValue();

        if (latestSystolic < 90 && latestSat < 92) {
            alerts.add(factory.createAlert(patientId, "Hypotensive Hypoxemia Alert", System.currentTimeMillis()));
        }
    }

    /**
     * Goes through all a patient's records and returns only the ones with the given type.
     *
     * @param patient the patient whose records we search through
     * @param type the record type we want, for example "SystolicPressure"
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
