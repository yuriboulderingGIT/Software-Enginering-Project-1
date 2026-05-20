package com.alerts;

import com.data_management.Patient;
import java.util.List;

public interface AlertStrategy {
    List<Alert> checkAlert(Patient patient);
}
