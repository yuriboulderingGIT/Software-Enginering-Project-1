package com.alerts.factory;

import com.alerts.Alert;

public abstract class AlertFactory {
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}
