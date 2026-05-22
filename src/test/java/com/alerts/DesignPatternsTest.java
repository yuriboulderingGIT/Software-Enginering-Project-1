package com.alerts;

import com.alerts.factory.AlertFactory;
import com.alerts.factory.BloodPressureAlertFactory;
import com.alerts.factory.BloodOxygenAlertFactory;
import com.alerts.factory.ECGAlertFactory;
import com.alerts.strategy.BloodPressureStrategy;
import com.alerts.strategy.OxygenSaturationStrategy;
import com.alerts.strategy.HeartRateStrategy;
import com.alerts.decorator.PriorityAlertDecorator;
import com.alerts.decorator.RepeatedAlertDecorator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the design patterns we used in Part 4:
 * Factory, Strategy, Decorator, and Singleton.
 *
 * <p>Each test checks one specific behaviour to make sure the code works correctly.
 * We reset the DataStorage singleton before each test so tests don't affect each other.
 */
class DesignPatternsTest {

    /**
     * Resets the DataStorage before each test so we always start fresh.
     */
    @BeforeEach
    void reset() {
        DataStorage.resetForTesting();
    }

    /**
     * Helper method to check if any alert in the list contains a certain text.
     *
     * @param alerts the list of alerts to search through
     * @param text the text we are looking for in the condition string
     * @return true if at least one alert contains the text, false otherwise
     */
    private boolean anyAlertContains(List<Alert> alerts, String text) {
        for (Alert a : alerts) {
            if (a.getCondition().contains(text)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ Factory tests

    /**
     * Checks that the BloodPressureAlertFactory adds the right prefix to the condition.
     */
    @Test
    void testBloodPressureFactoryPrefix() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("BloodPressure: "));
    }

    /**
     * Checks that the BloodOxygenAlertFactory adds the right prefix to the condition.
     */
    @Test
    void testBloodOxygenFactoryPrefix() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("BloodOxygen: "));
    }

    /**
     * Checks that the ECGAlertFactory adds the right prefix to the condition.
     */
    @Test
    void testEcgFactoryPrefix() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("ECG: "));
    }

    /**
     * Checks that the factory correctly passes through the patient id and timestamp.
     */
    @Test
    void testFactoryPassesThroughPatientIdAndTimestamp() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("42", "test condition", 9999L);
        assertEquals("42", alert.getPatientId());
        assertEquals(9999L, alert.getTimestamp());
    }

    // ------------------------------------------------------------------ Strategy tests

    /**
     * Checks that BloodPressureStrategy detects an increasing trend in systolic pressure.
     * Three readings each going up by more than 10 should trigger the alert.
     */
    @Test
    void testBloodPressureStrategyIncreasingTrend() {
        Patient p = new Patient(1);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(131.0, "SystolicPressure", 2000L);
        p.addRecord(142.0, "SystolicPressure", 3000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "increasing trend"));
    }

    /**
     * Checks that BloodPressureStrategy detects critically high systolic pressure (above 180).
     */
    @Test
    void testBloodPressureStrategyCriticalHighSystolic() {
        Patient p = new Patient(2);
        p.addRecord(185.0, "SystolicPressure", 1000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "systolic pressure high"));
    }

    /**
     * Checks that BloodPressureStrategy detects hypotensive hypoxemia
     * when both systolic pressure and oxygen saturation are below their thresholds.
     */
    @Test
    void testBloodPressureStrategyHypotensiveHypoxemia() {
        Patient p = new Patient(3);
        p.addRecord(85.0, "SystolicPressure", 1000L);
        p.addRecord(90.0, "Saturation", 1000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "Hypotensive Hypoxemia"));
    }

    /**
     * Checks that OxygenSaturationStrategy detects when the oxygen level is below 92%.
     */
    @Test
    void testOxygenSaturationStrategyLowSaturation() {
        Patient p = new Patient(4);
        p.addRecord(91.0, "Saturation", 1000L);

        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "Low blood saturation"));
    }

    /**
     * Checks that OxygenSaturationStrategy detects when the oxygen level drops by 5% quickly.
     */
    @Test
    void testOxygenSaturationStrategyRapidDrop() {
        Patient p = new Patient(5);
        p.addRecord(98.0, "Saturation", 1000L);
        p.addRecord(93.0, "Saturation", 2000L);

        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "Rapid blood saturation drop"));
    }

    /**
     * Checks that HeartRateStrategy detects an ECG anomaly.
     * We add 20 normal readings of 1.0, then one spike of 10.0.
     * 10.0 is 10 times the average, which is way above the threshold of 2x.
     */
    @Test
    void testHeartRateStrategyEcgAnomaly() {
        Patient p = new Patient(6);
        for (int i = 0; i < 20; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100L);
        }
        p.addRecord(10.0, "ECG", 3000L); // 10x the window average -- well above threshold

        List<Alert> alerts = new HeartRateStrategy().checkAlert(p);
        assertTrue(anyAlertContains(alerts, "ECG anomaly"));
    }

    /**
     * Checks that no alerts are created when all readings are within normal ranges.
     */
    @Test
    void testNoAlertsForNormalReadings() {
        Patient p = new Patient(7);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(80.0, "DiastolicPressure", 1000L);
        p.addRecord(97.0, "Saturation", 1000L);

        assertTrue(new BloodPressureStrategy().checkAlert(p).isEmpty());
        assertTrue(new OxygenSaturationStrategy().checkAlert(p).isEmpty());
        assertTrue(new HeartRateStrategy().checkAlert(p).isEmpty());
    }

    // ------------------------------------------------------------------ Decorator tests

    /**
     * Checks that PriorityAlertDecorator adds "[PRIORITY] " to the start of the condition.
     */
    @Test
    void testPriorityAlertDecoratorPrependsTag() {
        Alert base = new Alert("1", "some condition", 1000L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "HIGH");
        assertTrue(decorated.getCondition().startsWith("[PRIORITY] "));
    }

    /**
     * Checks that RepeatedAlertDecorator adds the repeat count at the end and stores it correctly.
     */
    @Test
    void testRepeatedAlertDecoratorAppendsCount() {
        Alert base = new Alert("1", "some condition", 1000L);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 3);
        assertTrue(decorated.getCondition().contains("[Repeated x3]"));
        assertEquals(3, decorated.getRepeatCount());
    }

    /**
     * Checks that we can chain two decorators together and both changes show up in the condition.
     */
    @Test
    void testDecoratorsCanBeChained() {
        Alert base = new Alert("1", "some condition", 1000L);
        PriorityAlertDecorator priority = new PriorityAlertDecorator(base, "HIGH");
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(priority, 2);

        String condition = repeated.getCondition();
        assertTrue(condition.contains("[PRIORITY] "));
        assertTrue(condition.contains("[Repeated x2]"));
    }

    /**
     * Checks that decorators correctly pass through the patient id and timestamp from the original alert.
     */
    @Test
    void testDecoratorsPassThroughPatientIdAndTimestamp() {
        Alert base = new Alert("42", "some condition", 9999L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "HIGH");
        assertEquals("42", decorated.getPatientId());
        assertEquals(9999L, decorated.getTimestamp());
    }

    // ------------------------------------------------------------------ Singleton tests

    /**
     * Checks that DataStorage.getInstance() always returns the exact same object.
     */
    @Test
    void testDataStorageSingletonReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    /**
     * Checks that data added through one instance is visible through another instance.
     * This makes sense because they are both the same singleton object.
     */
    @Test
    void testDataStorageSingletonDataVisible() {
        DataStorage storage1 = DataStorage.getInstance();
        storage1.addPatientData(1, 100.0, "ECG", 1000L);

        DataStorage storage2 = DataStorage.getInstance();
        List<PatientRecord> records = storage2.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
    }

    /**
     * Checks that resetForTesting() creates a brand new instance with no data in it.
     */
    @Test
    void testDataStorageResetForTestingProducesFreshInstance() {
        DataStorage first = DataStorage.getInstance();
        first.addPatientData(1, 100.0, "ECG", 1000L);

        DataStorage.resetForTesting();
        DataStorage second = DataStorage.getInstance();

        assertNotSame(first, second);
        assertTrue(second.getRecords(1, 1000L, 1000L).isEmpty());
    }
}
