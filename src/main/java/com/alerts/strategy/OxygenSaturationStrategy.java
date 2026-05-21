package com.alerts.strategy;

import com.alerts.Alert;
import com.alerts.factory.AlertFactory;
import com.alerts.factory.BloodOxygenAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * This class checks a patient's oxygen saturation levels and creates alerts if needed.
 * It uses the Strategy design pattern.
 *
 * <p>It checks two things:
 * 1. Is the latest oxygen level below 92%? That's too low.
 * 2. Did the oxygen level drop by 5% or more within a 10-minute window? That's a rapid drop.
 */
public class OxygenSaturationStrategy implements AlertStrategy {

    // 10 minutes in milliseconds - used to detect rapid drops
    private static final long TEN_MINUTES_MS = 10 * 60 * 1000L;

    // I use this factory to make blood oxygen alerts
    private AlertFactory factory = new BloodOxygenAlertFactory();

    /**
     * Runs all the oxygen saturation checks for a patient and returns any alerts found.
     *
     * @param patient the patient we want to check
     * @return a list of alerts, or an empty list if everything looks fine
     */
    @Override
    public List<Alert> checkAlert(Patient patient) {
        String patientId = String.valueOf(patient.getPatientId());
        List<Alert> alerts = new ArrayList<>();

        // get all saturation records for this patient
        List<PatientRecord> satRecords = filterByType(patient, "Saturation");

        checkLowSaturation(patientId, satRecords, alerts);
        checkRapidDrop(patientId, satRecords, alerts);

        return alerts;
    }

    /**
     * Checks if the most recent oxygen reading is below 92%.
     * If it is, we add a low saturation alert.
     *
     * @param patientId the id of the patient as a string
     * @param satRecords the list of saturation readings
     * @param alerts the list we add new alerts to
     */
    private void checkLowSaturation(String patientId, List<PatientRecord> satRecords, List<Alert> alerts) {
        if (satRecords.isEmpty()) {
            return;
        }

        PatientRecord latest = satRecords.get(satRecords.size() - 1);
        if (latest.getMeasurementValue() < 92.0) {
            alerts.add(factory.createAlert(patientId, "Low blood saturation", latest.getTimestamp()));
        }
    }

    /**
     * Checks if the oxygen level dropped by 5% or more within any 10-minute window.
     * We loop through the readings and compare each pair that falls within 10 minutes of each other.
     *
     * @param patientId the id of the patient as a string
     * @param satRecords the list of saturation readings in order of time
     * @param alerts the list we add new alerts to
     */
    private void checkRapidDrop(String patientId, List<PatientRecord> satRecords, List<Alert> alerts) {
        for (int i = 0; i < satRecords.size(); i++) {
            double baseVal = satRecords.get(i).getMeasurementValue();

            for (int j = i + 1; j < satRecords.size(); j++) {
                PatientRecord later = satRecords.get(j);

                // stop looking if the reading is more than 10 minutes away
                if (later.getTimestamp() - satRecords.get(i).getTimestamp() > TEN_MINUTES_MS) {
                    break;
                }

                // if the value dropped by 5 or more, that's a rapid drop
                if (baseVal - later.getMeasurementValue() >= 5.0) {
                    alerts.add(factory.createAlert(patientId, "Rapid blood saturation drop", later.getTimestamp()));
                    return;
                }
            }
        }
    }

    /**
     * Goes through all a patient's records and returns only the ones with the given type.
     *
     * @param patient the patient whose records we search through
     * @param type the record type we want, for example "Saturation"
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
