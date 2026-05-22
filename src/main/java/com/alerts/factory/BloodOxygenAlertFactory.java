package com.alerts.factory;

import com.alerts.Alert;

/**
 * This is a factory class for making blood oxygen alerts.
 * It extends AlertFactory and adds a "BloodOxygen: " prefix to the alert condition
 * so we know what type of alert it is.
 *
 * <p>This uses the Factory design pattern - we have one method that creates the alert object for us.
 */
public class BloodOxygenAlertFactory extends AlertFactory {

    /**
     * Creates a new blood oxygen alert with the given details.
     * It adds "BloodOxygen: " at the start of the condition message.
     *
     * @param patientId the id of the patient this alert is for
     * @param condition a short description of what went wrong
     * @param timestamp when the alert happened, in milliseconds
     * @return a new Alert object with the condition prefixed with "BloodOxygen: "
     */
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, "BloodOxygen: " + condition, timestamp);
    }
}
