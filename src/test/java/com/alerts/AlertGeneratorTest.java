package com.alerts;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlertGenerator.
 * We verify alerts by capturing stdout since triggerAlert prints to System.out.
 */
class AlertGeneratorTest {

    private DataStorage storage;
    private AlertGenerator alertGenerator;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        DataStorage.resetForTesting();
        storage = DataStorage.getInstance();
        alertGenerator = new AlertGenerator(storage);
        // Capture System.out so we can assert on triggered alerts
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    private String getOutput() {
        return output.toString();
    }

    // Helper: add records at evenly spaced timestamps starting from 1000
    private void addRecords(Patient patient, String type, double... values) {
        long time = 1000L;
        for (double v : values) {
            patient.addRecord(v, type, time);
            time += 1000L;
        }
    }

    // ------------------------------------------------------------------ Blood Pressure Trend

    @Test
    void testSystolicIncreasingTrendTriggersAlert() {
        Patient p = new Patient(1);
        addRecords(p, "SystolicPressure", 120.0, 131.0, 142.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("increasing trend"));
    }

    @Test
    void testSystolicDecreasingTrendTriggersAlert() {
        Patient p = new Patient(2);
        addRecords(p, "SystolicPressure", 150.0, 139.0, 128.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("decreasing trend"));
    }

    @Test
    void testNoTrendWhenChangeIsSmall() {
        Patient p = new Patient(3);
        addRecords(p, "SystolicPressure", 120.0, 125.0, 130.0); // only 5 mmHg each
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("trend"));
    }

    // ------------------------------------------------------------------ Blood Pressure Critical

    @Test
    void testHighSystolicTriggersAlert() {
        Patient p = new Patient(4);
        addRecords(p, "SystolicPressure", 185.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("systolic pressure high"));
    }

    @Test
    void testLowSystolicTriggersAlert() {
        Patient p = new Patient(5);
        addRecords(p, "SystolicPressure", 85.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("systolic pressure low"));
    }

    @Test
    void testHighDiastolicTriggersAlert() {
        Patient p = new Patient(6);
        addRecords(p, "DiastolicPressure", 125.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("diastolic pressure high"));
    }

    @Test
    void testLowDiastolicTriggersAlert() {
        Patient p = new Patient(7);
        addRecords(p, "DiastolicPressure", 55.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("diastolic pressure low"));
    }

    @Test
    void testNormalBloodPressureNoAlert() {
        Patient p = new Patient(8);
        addRecords(p, "SystolicPressure", 120.0);
        addRecords(p, "DiastolicPressure", 80.0);
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("pressure"));
    }

    // ------------------------------------------------------------------ Low Saturation

    @Test
    void testLowSaturationTriggersAlert() {
        Patient p = new Patient(9);
        addRecords(p, "Saturation", 91.0);
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("Low blood saturation"));
    }

    @Test
    void testNormalSaturationNoAlert() {
        Patient p = new Patient(10);
        addRecords(p, "Saturation", 97.0);
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("saturation"));
    }

    // ------------------------------------------------------------------ Rapid Saturation Drop

    @Test
    void testRapidSaturationDropTriggersAlert() {
        Patient p = new Patient(11);
        // Drop of 5% within 10 minutes
        p.addRecord(98.0, "Saturation", 1000L);
        p.addRecord(93.0, "Saturation", 2000L); // 5% drop
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("Rapid blood saturation drop"));
    }

    @Test
    void testSmallSaturationDropNoAlert() {
        Patient p = new Patient(12);
        p.addRecord(98.0, "Saturation", 1000L);
        p.addRecord(96.0, "Saturation", 2000L); // only 2% drop
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("Rapid"));
    }

    // ------------------------------------------------------------------ Hypotensive Hypoxemia

    @Test
    void testHypotensiveHypoxemiaTriggersAlert() {
        Patient p = new Patient(13);
        addRecords(p, "SystolicPressure", 85.0); // below 90
        addRecords(p, "Saturation", 90.0);       // below 92
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testNoHypotensiveHypoxemiaWhenOnlyOneLow() {
        Patient p = new Patient(14);
        addRecords(p, "SystolicPressure", 85.0); // low BP
        addRecords(p, "Saturation", 95.0);       // normal sat
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("Hypotensive Hypoxemia"));
    }

    // ------------------------------------------------------------------ ECG Anomaly

    @Test
    void testEcgSpikeTriggersAlert() {
        Patient p = new Patient(15);
        // Fill 20 readings with value 1.0 (the window), then add a spike
        for (int i = 0; i < 20; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100L);
        }
        p.addRecord(10.0, "ECG", 3000L); // 10x the average — well above threshold
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("ECG anomaly"));
    }

    @Test
    void testEcgNoAlertWhenNotEnoughData() {
        Patient p = new Patient(16);
        // Fewer than 20 readings — not enough for sliding window
        for (int i = 0; i < 5; i++) {
            p.addRecord(1.0, "ECG", 1000L + i * 100L);
        }
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("ECG anomaly"));
    }

    // ------------------------------------------------------------------ Triggered Alert

    @Test
    void testManualAlertTriggered() {
        Patient p = new Patient(17);
        p.addRecord(1.0, "Alert", 1000L); // 1.0 = triggered
        alertGenerator.evaluateData(p);
        assertTrue(getOutput().contains("Manual alert triggered"));
    }

    @Test
    void testManualAlertResolvedNoAlert() {
        Patient p = new Patient(18);
        p.addRecord(0.0, "Alert", 1000L); // 0.0 = resolved
        alertGenerator.evaluateData(p);
        assertFalse(getOutput().contains("Manual alert triggered"));
    }
}