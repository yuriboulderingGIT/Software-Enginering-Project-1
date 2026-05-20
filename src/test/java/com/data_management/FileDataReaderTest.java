package com.data_management;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataReaderTest {

    @TempDir
    Path tempDir;

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetForTesting();
        storage = DataStorage.getInstance();
    }

    private void writeFile(String name, String content) throws IOException {
        File file = tempDir.resolve(name).toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
    }

    @Test
    void testReadsValidRecords() throws IOException {
        writeFile("data.txt", "1, 1000, HeartRate, 72.0\n1, 2000, HeartRate, 80.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(2, storage.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testCorrectValuesStored() throws IOException {
        writeFile("data.txt", "3, 5000, SystolicPressure, 120.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);

        PatientRecord r = storage.getRecords(3, 0, Long.MAX_VALUE).get(0);
        assertEquals(3, r.getPatientId());
        assertEquals(5000L, r.getTimestamp());
        assertEquals("SystolicPressure", r.getRecordType());
        assertEquals(120.0, r.getMeasurementValue(), 0.001);
    }

    @Test
    void testSkipsBlankAndCommentLines() throws IOException {
        writeFile("data.txt", "# comment\n\n1, 1000, HeartRate, 70.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsMalformedLines() throws IOException {
        writeFile("data.txt", "1, 1000, HeartRate\n2, 2000, HeartRate, 75.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(0, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsBadNumbers() throws IOException {
        writeFile("data.txt", "abc, 1000, HeartRate, 72.0\n1, 2000, HeartRate, 75.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testIgnoresNonTxtFiles() throws IOException {
        writeFile("data.csv", "7, 1000, HeartRate, 70.0\n");
        writeFile("data.txt", "8, 2000, HeartRate, 75.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertTrue(storage.getRecords(7, 0, Long.MAX_VALUE).isEmpty());
        assertEquals(1, storage.getRecords(8, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testReadsMultipleFiles() throws IOException {
        writeFile("a.txt", "10, 1000, HeartRate, 68.0\n");
        writeFile("b.txt", "11, 2000, HeartRate, 90.0\n");
        new FileDataReader(tempDir.toString()).readData(storage);
        assertEquals(1, storage.getRecords(10, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(11, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testEmptyDirectoryProducesNoRecords() throws IOException {
        new FileDataReader(tempDir.toString()).readData(storage);
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testNonExistentDirectoryThrowsIOException() {
        assertThrows(IOException.class,
                () -> new FileDataReader("/nonexistent/path").readData(storage));
    }

    @Test
    void testFilePathInsteadOfDirectoryThrowsIOException() throws IOException {
        File file = tempDir.resolve("file.txt").toFile();
        file.createNewFile();
        assertThrows(IOException.class,
                () -> new FileDataReader(file.getAbsolutePath()).readData(storage));
    }
}