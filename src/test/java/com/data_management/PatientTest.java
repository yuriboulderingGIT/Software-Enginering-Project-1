package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient(1);
        patient.addRecord(70.0, "HeartRate", 1000L);
        patient.addRecord(75.0, "HeartRate", 5000L);
        patient.addRecord(80.0, "HeartRate", 9000L);
    }

    @Test
    void testGetRecordsReturnsRecordsWithinRange() {
        List<PatientRecord> records = patient.getRecords(2000L, 8000L);
        assertEquals(1, records.size());
        assertEquals(75.0, records.get(0).getMeasurementValue(), 0.001);
    }

    @Test
    void testGetRecordsIncludesBoundaries() {
        List<PatientRecord> records = patient.getRecords(1000L, 9000L);
        assertEquals(3, records.size());
    }

    @Test
    void testGetRecordsReturnsEmptyWhenNoMatch() {
        List<PatientRecord> records = patient.getRecords(10000L, 20000L);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsWithStartAfterEndReturnsEmpty() {
        List<PatientRecord> records = patient.getRecords(9000L, 1000L);
        assertTrue(records.isEmpty());
    }
}