package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketClientTest {

    private DataStorage storage;
    private WebSocketClientImpl client;

    @BeforeEach
    void setUp() throws Exception {
        DataStorage.resetForTesting();
        storage = DataStorage.getInstance();
        client = new WebSocketClientImpl(new URI("ws://localhost:9090"), storage);
    }

    // --- Parsing valid messages ---

    @Test
    void testValidMessageStoredCorrectly() {
        client.onMessage("1,1714376789050,SystolicPressure,120.0");

        List<PatientRecord> records = storage.getRecords(1, 1714376789000L, 1714376790000L);
        assertEquals(1, records.size());
        assertEquals("SystolicPressure", records.get(0).getRecordType());
        assertEquals(120.0, records.get(0).getMeasurementValue(), 0.001);
        assertEquals(1, records.get(0).getPatientId());
    }

    @Test
    void testMultipleValidMessagesStored() {
        client.onMessage("3,1000000000000,HeartRate,80.0");
        client.onMessage("3,1000000000001,HeartRate,82.0");
        client.onMessage("3,1000000000002,HeartRate,78.0");

        List<PatientRecord> records = storage.getRecords(3, 999999999999L, 1000000000003L);
        assertEquals(3, records.size());
    }

    // --- Parsing invalid messages ---

    @Test
    void testTooFewFieldsSkipped() {
        assertDoesNotThrow(() -> client.onMessage("1,1714376789050,HeartRate"));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testTooManyFieldsSkipped() {
        assertDoesNotThrow(() -> client.onMessage("1,1714376789050,HeartRate,75.0,extraField"));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testNonNumericPatientIdSkipped() {
        assertDoesNotThrow(() -> client.onMessage("abc,1714376789050,HeartRate,75.0"));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testNonNumericValueSkipped() {
        assertDoesNotThrow(() -> client.onMessage("1,1714376789050,HeartRate,notanumber"));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testEmptyMessageSkipped() {
        assertDoesNotThrow(() -> client.onMessage(""));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    @Test
    void testNullMessageSkipped() {
        assertDoesNotThrow(() -> client.onMessage((String) null));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }

    // --- Error handling callbacks ---

    @Test
    void testOnCloseDoesNotThrow() {
        assertDoesNotThrow(() -> client.onClose(1000, "Normal closure", false));
    }

    @Test
    void testOnErrorDoesNotThrow() {
        assertDoesNotThrow(() -> client.onError(new Exception("test error")));
    }

    // --- Connection state ---

    @Test
    void testClientConstruction() {
        assertNotNull(client);
    }

    @Test
    void testClientIsNotOpenBeforeConnecting() {
        assertFalse(client.isOpen());
    }

    @Test
    void testEmptyLabelSkipped() {
        assertDoesNotThrow(() -> client.onMessage("1,1000,,75.0"));

        List<PatientRecord> records = storage.getRecords(1, 0L, Long.MAX_VALUE);
        assertEquals(0, records.size());
    }
}
