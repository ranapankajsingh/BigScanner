package co.big.hrd;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BigScanner {
    /**
     * Constants for Factors (salary multipliers, reporting line limit, Max rows)
     */
    private static final int MAX_ALLOWED_ROWS = 1000;
    private static final double MIN_SALARY_FACTOR = 1.20;
    private static final double MAX_SALARY_FACTOR = 1.50;
    private static final int MAX_REPORTING_LINE = 4;


    private Map<Integer, Employee> employees = new HashMap<>();
    private Map<Integer, List<Employee>> subordinates = new HashMap<>();

    public void loadCsv(String filePath) throws IOException, CsvException, UnsupportedOperationException {
        employees.clear();
        subordinates.clear();

        try (CSVReader csvReader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = csvReader.readAll();

            if (records.size() > MAX_ALLOWED_ROWS) {
                System.err.printf("\nError processing file: %s ==> Max records Limit is 1000 \n", filePath);
                throw new UnsupportedOperationException("Error processing file: %s ==> Max records Limit is 1000 ");
            }
            for (String[] record : records) {
                // Throws NumberFormat Exception for Null Id
                int id = Integer.parseInt(record[0]);
                String firstName = record[1];
                String lastName = record[2];
                double salary = Double.parseDouble(record[3]);
                Integer managerId = record.length > 4 && !record[4].isEmpty() ?
                        Integer.parseInt(record[4]) : null;

                if (managerId != null && managerId == id) {
                    System.err.printf("\nError processing file: %s ==> Employee Id same as Manager ID \n", filePath);
                    throw new UnsupportedOperationException("Error processing file: ==> Employee Id same as Manager ID ");
                }

                Employee emp = new Employee(id, firstName, lastName, salary, managerId);
                employees.put(id, emp);

                if (managerId != null) {
                    subordinates.computeIfAbsent(managerId, k -> new ArrayList<>()).add(emp);
                }
            }
        }
    }

    public List<String> checkCompliance() {
        List<String> results = new ArrayList<>();
        results.addAll(checkSalaries());
        results.addAll(checkReportee());
        return results;
    }

    private List<String> checkSalaries() {
        List<String> results = new ArrayList<>();
        //results.add("===================");
        results.add("Salary Analysis:");
        // results.add("===================");

        for (Integer managerId : subordinates.keySet()) {
            Employee manager = employees.get(managerId);
            List<Employee> directReports = subordinates.get(managerId);

            double avgSubordinateSalary = directReports.stream()
                    .mapToDouble(Employee::salary)
                    .average()
                    .orElse(0.0);

            double minSalary = avgSubordinateSalary * MIN_SALARY_FACTOR;
            double maxSalary = avgSubordinateSalary * MAX_SALARY_FACTOR;

            if (manager.salary() < minSalary) {
                double difference = minSalary - manager.salary();
                results.add(String.format(
                        "Manager %s %s (ID: %d) earns less than minimum (%.2f < %.2f) by %.2f",
                        manager.firstName(), manager.lastName(), manager.id(),
                        manager.salary(), minSalary, difference));
            } else if (manager.salary() > maxSalary) {
                double difference = manager.salary() - maxSalary;
                results.add(String.format(
                        "Manager %s %s (ID: %d) earns more than maximum (%.2f > %.2f) by %.2f",
                        manager.firstName(), manager.lastName(), manager.id(),
                        manager.salary(), maxSalary, difference));
            }
        }
        return results;
    }

    private int countManagersToCeo(int employeeId, Set<Integer> visited) {
        if (!employees.containsKey(employeeId) || visited.contains(employeeId)) {
            return 0;
        }

        Employee emp = employees.get(employeeId);
        if (emp.managerId() == null) { // if Manager Id is null than CEO
            return 0;
        }

        visited.add(employeeId);
        return 1 + countManagersToCeo(emp.managerId(), visited);
    }

    private List<String> checkReportee() {
        List<String> results = new ArrayList<>();
        // results.add("===========================");
        results.add("Reporting Line Analysis:");
        // results.add("===========================");

        for (Employee emp : employees.values()) {
            Set<Integer> visited = new HashSet<>();
            int managerCount = countManagersToCeo(emp.id(), visited);

            if (managerCount > MAX_REPORTING_LINE) {
                int excess = managerCount - MAX_REPORTING_LINE;
                results.add(String.format(
                        "Employee %s %s (ID: %d) has %d managers to CEO, exceeding limit by %d",
                        emp.firstName(), emp.lastName(), emp.id(), managerCount, excess));
            }
        }
        return results;
    }
}