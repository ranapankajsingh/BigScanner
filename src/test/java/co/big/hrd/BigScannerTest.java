package co.big.hrd;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BigScannerTest {
    private BigScanner bigScanner;
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        bigScanner = new BigScanner();
        tempFile = Files.createTempFile("test-employees", ".csv");
    }

    @Test
    void testAllOk() throws IOException, CsvException {
        String testData = """
                100,CEO,NO1,90000,
                200,Manager1,Manage,60000,100
                300,Worker1,Work,45000,200
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertEquals(2, results.size()); // Only headers, no violations
        assertTrue(results.get(0).startsWith("Salary Analysis"));
        assertTrue(results.get(1).startsWith("Reporting Line Analysis"));
    }

    @Test
    void testSalaryTooLow() throws IOException, CsvException {
        String testData = """
                100,CEO,No1,100000,
                200,Manager,Manage,50000,100
                300,Worker,Work,45000,200
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("Manager Manage") && s.contains("earns less than minimum")));
    }

    @Test
    void testSalaryTooHigh() throws IOException, CsvException {
        String testData = """
                100,CEO,No1,100000,
                200,Manager,Manage,80000,100
                300,Worker,Work,45000,200
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("Manager Manage") && s.contains("earns more than maximum")));
    }

    @Test
    void testLongReportingLine() throws IOException, CsvException {
        String testData = """
                100,CEO,Top,100000,
                200,Mgr1,One,80000,100
                300,Mgr2,Two,70000,200
                400,Mgr3,Three,60000,300
                500,Mgr4,Four,50000,400
                600,Worker,Low,40000,500
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("Worker Low") && s.contains("exceeding limit by 1")));
    }

    @Test
    void testLongReportingLine2() throws IOException, CsvException {
        String testData = """
                100,CEO,Top,100000,
                200,Mgr1,One,80000,100
                300,Mgr2,Two,70000,200
                400,Mgr3,Three,60000,300
                500,Mgr4,Four,50000,400
                600,Worker,Low,40000,500
                700,SubWorker,Low,40000,600
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("SubWorker Low") && s.contains("exceeding limit by 2")));
    }
    @Test
    void testSampleData() throws IOException, CsvException {
        String testData = """
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                300,Alice,Hasacat,50000,124
                305,Brett,Hardleaf,34000,300
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("Martin") && s.contains("earns less than minimum")));
        assertEquals(3, results.size()); // Headers + 1 violation
    }

    @Test
    void testInvalidFile() {
        assertThrows(IOException.class, () -> bigScanner.loadCsv("nonexistent.csv"));
    }

    @Test
    void testMalformedData() throws IOException {
        String testData = "100,CEO,Top,invalid_salary,";
        Files.writeString(tempFile, testData);
        assertThrows(java.lang.NumberFormatException.class, () -> bigScanner.loadCsv(tempFile.toString()));
    }

    @Test
    void testMalformedCSV() throws IOException {
        String testData = " 100,CEO,Top,,,,5600,";
        Files.writeString(tempFile, testData);
        assertThrows(java.lang.NumberFormatException.class, () -> bigScanner.loadCsv(tempFile.toString()));
    }

    @Test
    void testNoReportingLine() throws IOException, CsvException {
        String testData = """
               100,CEO,Top,100000,
               """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        // if only CEO no Exceptions
        assertEquals(2, results.size()); // Only headers, no violations
        assertTrue(results.get(0).startsWith("Salary Analysis"));
        assertTrue(results.get(1).startsWith("Reporting Line Analysis"));
    }

    @Test
    void testNoCEO() throws IOException, CsvException {
        String testData = """
               100,Manager,Top,100000,100
               """;
        Files.writeString(tempFile, testData);
        assertThrows(java.lang.UnsupportedOperationException.class, () -> bigScanner.loadCsv(tempFile.toString()));
       // assertEquals("Error processing file: ==> Employee Id same as Manager ID", exception.getMessage());
    }

    @Test
    void testOneEmpWithCEO() throws IOException, CsvException {
        String testData = """
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertEquals(2, results.size()); // Headers + 1 violation
    }

    @Test
    void testOneEmpWithCEOErr() throws IOException, CsvException {
        String testData = """
                123,Joe,Doe,45000,
                124,Martin,Chekov,60000,123
                """;
        Files.writeString(tempFile, testData);
        bigScanner.loadCsv(tempFile.toString());

        List<String> results = bigScanner.checkCompliance();
        assertTrue(results.stream().anyMatch(s -> s.contains("Joe Doe") && s.contains("earns less than minimum")));
        assertEquals(3, results.size()); // Headers + 1 violation
    }

    @Test
    void testOneEmpWithCEODataErr() throws IOException, CsvException {
        String testData = """
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,124
                """;
        Files.writeString(tempFile, testData);
        assertThrows(java.lang.UnsupportedOperationException.class, () -> bigScanner.loadCsv(tempFile.toString()));
        List<String> results = bigScanner.checkCompliance();
        assertEquals(2, results.size()); // Headers + 1 violation
    }
    @Test
    void testOneEmpWithDataErr() throws IOException, CsvException {
        String testData = """
                123,Joe,Doe,60000,
                null,Martin,Chekov,45000,123
                """;
        Files.writeString(tempFile, testData);
        // should Throw NumberFormatException for null Employee Id.
        assertThrows(java.lang.NumberFormatException.class, () -> bigScanner.loadCsv(tempFile.toString()));
        List<String> results = bigScanner.checkCompliance();
        assertEquals(2, results.size()); // Headers + 1 violation
    }
}
