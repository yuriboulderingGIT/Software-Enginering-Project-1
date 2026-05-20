package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DesignPatternsTest {

    @BeforeEach
    void reset() {
        DataStorage.resetForTesting();
    }

    // ------------------------------------------------------------------ Factory

    @Test
    void testBloodPressureFactoryPrefix() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("BloodPressure: "));
    }

    @Test
    void testBloodOxygenFactoryPrefix() {
        AlertFactory factory = new BloodOxygenAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("BloodOxygen: "));
    }

    @Test
    void testEcgFactoryPrefix() {
        AlertFactory factory = new ECGAlertFactory();
        Alert alert = factory.createAlert("1", "some condition", 1000L);
        assertTrue(alert.getCondition().startsWith("ECG: "));
    }

    @Test
    void testFactoryPassesThroughPatientIdAndTimestamp() {
        AlertFactory factory = new BloodPressureAlertFactory();
        Alert alert = factory.createAlert("42", "test condition", 9999L);
        assertEquals("42", alert.getPatientId());
        assertEquals(9999L, alert.getTimestamp());
    }

    // ------------------------------------------------------------------ Strategy

    @Test
    void testBloodPressureStrategyIncreasingTrend() {
        Patient p = new Patient(1);
        p.addRecord(120.0, "SystolicPressure", 1000L);
        p.addRecord(131.0, "SystolicPressure", 2000L);
        p.addRecord(142.0, "SystolicPressure", 3000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("increasing trend")));
    }

    @Test
    void testBloodPressureStrategyCriticalHighSystolic() {
        Patient p = new Patient(2);
        p.addRecord(185.0, "SystolicPressure", 1000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("systolic pressure high")));
    }

    @Test
    void testBloodPressureStrategyHypotensiveHypoxemia() {
        Patient p = new Patient(3);
        p.addRecord(85.0, "SystolicPressure", 1000L);
        p.addRecord(90.0, "Saturation", 1000L);

        List<Alert> alerts = new BloodPressureStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Hypotensive Hypoxemia")));
    }

    @Test
    void testOxygenSaturationStrategyLowSaturation() {
        Patient p = new Patient(4);
        p.addRecord(91.0, "Saturation", 1000L);

        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Low blood saturation")));
    }

    @Test
    void testOxygenSaturationStrategyRapidDrop() {
        Patient p = new Patient(5);
        p.addRecord(98.0, "Saturation", 1000L);
        p.addRecord(93.0, "Saturation", 2000L);

        List<Alert> alerts = new OxygenSaturationStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("Rapid blood saturation drop")));
    }

    @Test
    void testHeartRateStrategyEcgAnomaly() {
        Patient p = new Patient(6);
        for (int i = 0; i < 20; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100L);
        }
        p.addRecord(10.0, "ECG", 3000L); // 10x the window average — well above threshold

        List<Alert> alerts = new HeartRateStrategy().checkAlert(p);
        assertTrue(alerts.stream().anyMatch(a -> a.getCondition().contains("ECG anomaly")));
    }

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

    // ------------------------------------------------------------------ Decorator

    @Test
    void testPriorityAlertDecoratorPrependsTag() {
        Alert base = new Alert("1", "some condition", 1000L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "HIGH");
        assertTrue(decorated.getCondition().startsWith("[PRIORITY] "));
    }

    @Test
    void testRepeatedAlertDecoratorAppendsCount() {
        Alert base = new Alert("1", "some condition", 1000L);
        RepeatedAlertDecorator decorated = new RepeatedAlertDecorator(base, 3);
        assertTrue(decorated.getCondition().contains("[Repeated x3]"));
        assertEquals(3, decorated.getRepeatCount());
    }

    @Test
    void testDecoratorsCanBeChained() {
        Alert base = new Alert("1", "some condition", 1000L);
        PriorityAlertDecorator priority = new PriorityAlertDecorator(base, "HIGH");
        RepeatedAlertDecorator repeated = new RepeatedAlertDecorator(priority, 2);

        String condition = repeated.getCondition();
        assertTrue(condition.contains("[PRIORITY] "));
        assertTrue(condition.contains("[Repeated x2]"));
    }

    @Test
    void testDecoratorsPassThroughPatientIdAndTimestamp() {
        Alert base = new Alert("42", "some condition", 9999L);
        PriorityAlertDecorator decorated = new PriorityAlertDecorator(base, "HIGH");
        assertEquals("42", decorated.getPatientId());
        assertEquals(9999L, decorated.getTimestamp());
    }

    // ------------------------------------------------------------------ Singleton

    @Test
    void testDataStorageSingletonReturnsSameInstance() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();
        assertSame(a, b);
    }

    @Test
    void testDataStorageSingletonDataVisible() {
        DataStorage storage1 = DataStorage.getInstance();
        storage1.addPatientData(1, 100.0, "ECG", 1000L);

        DataStorage storage2 = DataStorage.getInstance();
        List<PatientRecord> records = storage2.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
    }

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
