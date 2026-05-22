package com.data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class DataStorageTest {

    @BeforeEach
    void reset() {
        DataStorage.resetForTesting();
    }

    @Test
    void testAddAndGetRecords() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }

    @Test
    void testGetAllPatientsReturnsAllAdded() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(2, 90.0, "HeartRate", 1000L);
        storage.addPatientData(3, 80.0, "HeartRate", 1000L);

        assertEquals(3, storage.getAllPatients().size());
    }

    @Test
    void testGetRecordsOnMissingPatientReturnsEmpty() {
        DataStorage storage = DataStorage.getInstance();
        List<PatientRecord> records = storage.getRecords(999, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecordsExcludesOutOfRangeRecords() {
        DataStorage storage = DataStorage.getInstance();
        storage.addPatientData(1, 100.0, "HeartRate", 1000L);
        storage.addPatientData(1, 200.0, "HeartRate", 9000L); // outside the query window

        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        assertEquals(1, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }
}
