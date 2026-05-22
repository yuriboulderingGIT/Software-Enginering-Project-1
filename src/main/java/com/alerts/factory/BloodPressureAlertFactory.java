package com.alerts.factory;

import com.alerts.Alert;

/**
 * This is a factory class for making blood pressure alerts.
 * It extends AlertFactory and adds a "BloodPressure: " prefix to the alert condition
 * so we know what type of alert it is.
 *
 * <p>This uses the Factory design pattern - we have one method that creates the alert object for us.
 */
public class BloodPressureAlertFactory extends AlertFactory {

    /**
     * Creates a new blood pressure alert with the given details.
     * It adds "BloodPressure: " at the start of the condition message.
     *
     * @param patientId the id of the patient this alert is for
     * @param condition a short description of what went wrong
     * @param timestamp when the alert happened, in milliseconds
     * @return a new Alert object with the condition prefixed with "BloodPressure: "
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BloodPressure: " + condition, timestamp);
    }
}
