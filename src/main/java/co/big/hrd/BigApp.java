package co.big.hrd;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class BigApp {
    public static void main(String[] args) {
        try {
            BigScanner bigScanner = new BigScanner();
            bigScanner.loadCsv("src/main/resources/EmployeeData.csv");
            bigScanner.checkCompliance().forEach(System.out::println);
        } catch (IOException | CsvException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}

