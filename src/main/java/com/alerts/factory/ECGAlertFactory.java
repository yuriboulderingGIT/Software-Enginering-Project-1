package com.alerts.factory;

import com.alerts.Alert;

/**
 * This is a factory class for making ECG alerts.
 * It extends AlertFactory and adds an "ECG: " prefix to the alert condition
 * so we know what type of alert it is.
 *
 * <p>This uses the Factory design pattern - we have one method that creates the alert object for us.
 */
public class ECGAlertFactory extends AlertFactory {

    /**
     * Creates a new ECG alert with the given details.
     * It adds "ECG: " at the start of the condition message.
     *
     * @param patientId the id of the patient this alert is for
     * @param condition a short description of what went wrong
     * @param timestamp when the alert happened, in milliseconds
     * @return a new Alert object with the condition prefixed with "ECG: "
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "ECG: " + condition, timestamp);
    }
}
