package com.data_management;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileDataReader.
 *
 * Covers:
 *  - Normal multi-record reads
 *  - Blank lines and comment lines are skipped
 *  - Malformed lines (wrong field count, bad numbers) are skipped
 *  - Multiple files in the same directory are all read
 *  - Non-.txt files are ignored
 *  - Missing directory throws IOException
 *  - Empty directory produces no records but no crash
 *  - Empty label on a line is skipped gracefully
 */
class FileDataReaderTest {

    @TempDir
    Path tempDir;

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        // Reset singleton between tests so state doesn't bleed across
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    @AfterEach
    void tearDown() {
        DataStorage.resetInstance();
    }

    // ------------------------------------------------------------------ helpers

    /** Writes content to a named file inside the temp directory. */
    private File writeFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }
        return file;
    }

    // ------------------------------------------------------------------ tests

    @Test
    void testReadsSingleFileWithValidRecords() throws IOException {
        writeFile("patient1.txt",
                "1, 1000000, HeartRate, 72.0\n" +
                "1, 2000000, HeartRate, 80.0\n");

        FileDataReader reader = new FileDataReader(tempDir.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
    }

    @Test
    void testCorrectValuesAreParsed() throws IOException {
        writeFile("data.txt", "3, 5000000, BloodPressure, 120.5\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(3, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());

        PatientRecord r = records.get(0);
        assertEquals(3,             r.getPatientId());
        assertEquals(5000000L,      r.getTimestamp());
        assertEquals("BloodPressure", r.getRecordType());
        assertEquals(120.5,         r.getMeasurementValue(), 0.001);
    }

    @Test
    void testSkipsBlankLines() throws IOException {
        writeFile("data.txt",
                "2, 1000, HeartRate, 65.0\n" +
                "\n" +
                "   \n" +
                "2, 2000, HeartRate, 70.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(2, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsCommentLines() throws IOException {
        writeFile("data.txt",
                "# This is a comment\n" +
                "5, 9000, OxygenSaturation, 98.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(1, storage.getRecords(5, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsMalformedLines_wrongFieldCount() throws IOException {
        writeFile("data.txt",
                "1, 1000, HeartRate\n" +          // only 3 fields
                "1, 2000, HeartRate, 75.0\n");     // valid

        new FileDataReader(tempDir.toString()).readData(storage);

        // Only the valid line should have been stored
        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsMalformedLines_badNumber() throws IOException {
        writeFile("data.txt",
                "abc, 1000, HeartRate, 72.0\n" +  // patientId not a number
                "1, notatime, HeartRate, 72.0\n" + // timestamp not a number
                "1, 3000, HeartRate, notavalue\n" + // value not a number
                "2, 4000, BloodPressure, 115.0\n"); // valid

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(0, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testSkipsLinesWithEmptyLabel() throws IOException {
        writeFile("data.txt",
                "1, 1000, , 72.0\n" +             // empty label
                "1, 2000, HeartRate, 80.0\n");     // valid

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(1, storage.getRecords(1, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testReadsMultipleFilesInDirectory() throws IOException {
        writeFile("patientA.txt", "10, 1000, HeartRate, 68.0\n");
        writeFile("patientB.txt", "11, 2000, HeartRate, 90.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(1, storage.getRecords(10, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(11, 0, Long.MAX_VALUE).size());
    }

    @Test
    void testIgnoresNonTxtFiles() throws IOException {
        // .csv file — should be ignored
        writeFile("data.csv", "7, 1000, HeartRate, 70.0\n");
        // .txt file — should be read
        writeFile("data.txt", "8, 2000, HeartRate, 75.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertEquals(0, storage.getRecords(7, 0, Long.MAX_VALUE).size(),
                "CSV file should have been ignored");
        assertEquals(1, storage.getRecords(8, 0, Long.MAX_VALUE).size(),
                "TXT file should have been read");
    }

    @Test
    void testEmptyDirectoryProducesNoRecords() throws IOException {
        // TempDir is empty — no files written
        new FileDataReader(tempDir.toString()).readData(storage);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testEmptyFileProducesNoRecords() throws IOException {
        writeFile("empty.txt", "");

        new FileDataReader(tempDir.toString()).readData(storage);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void testNonExistentDirectoryThrowsIOException() {
        FileDataReader reader = new FileDataReader("/this/path/does/not/exist");

        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    @Test
    void testPathPointingToFilenNotDirectoryThrowsIOException() throws IOException {
        File file = writeFile("notadir.txt", "1, 1000, HeartRate, 72.0\n");

        FileDataReader reader = new FileDataReader(file.getAbsolutePath());

        assertThrows(IOException.class, () -> reader.readData(storage));
    }

    @Test
    void testMultipleRecordsSamePatientStoredCorrectly() throws IOException {
        writeFile("data.txt",
                "1, 1000, SystolicPressure, 120.0\n" +
                "1, 2000, DiastolicPressure, 80.0\n" +
                "1, 3000, SystolicPressure, 125.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        List<PatientRecord> all = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertEquals(3, all.size());
    }

    @Test
    void testTimeRangeFilteringWorksAfterFileRead() throws IOException {
        writeFile("data.txt",
                "1, 1000, HeartRate, 60.0\n" +
                "1, 5000, HeartRate, 70.0\n" +
                "1, 9000, HeartRate, 80.0\n");

        new FileDataReader(tempDir.toString()).readData(storage);

        // Only records with timestamp between 2000 and 8000
        List<PatientRecord> records = storage.getRecords(1, 2000, 8000);
        assertEquals(1, records.size());
        assertEquals(70.0, records.get(0).getMeasurementValue(), 0.001);
    }
}