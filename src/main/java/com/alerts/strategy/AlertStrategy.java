package com.alerts.strategy;

import com.alerts.Alert;
import com.data_management.Patient;
import java.util.List;

public interface AlertStrategy {
    List<Alert> checkAlert(Patient patient);
}
