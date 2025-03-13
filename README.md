# BigScanner

Sample implementation for Big Company

## 1. Overview

### 1.1 Purpose

The BigScanner is a Java-based application designed to analyze a
company's organizational structure based on employee data provided in
CSV format. It identifies salary discrepancies for managers and
excessive reporting lines.

### 1.2 Scope

The system: - Reads employee data from a CSV file - Analyzes manager
salaries against subordinates' average (20% min, 50% max) - Identifies
reporting lines exceeding 4 managers - Provides detailed reporting of
violations

## 2. System Architecture

### 2.1 Components

    +-----------------+
    |  Main Program   |
    |  (entry point)  |
    +-----------------+
             |
             v
    +-----------------+
    | BigScanner       |
    | - Data Loading  |
    | - Analysis      |
    | - Reporting     |
    +-----------------+
        ^          ^
        |          |
    +------+   +-------+
    |Employee| |OpenCSV |
    |Record  | |Library |
    +------+   +-------+

### 2.2 Dependencies

-   Java 17
-   OpenCSV 5.9 (CSV parsing)
-   JUnit 5.10.2 (testing)

## 3. Project Structure

    src/
    +-- main/
    ¦   +-- java/
    ¦       +-- co/
    ¦           +-- big/
    ¦               +-- hrd/
    ¦                   +-- Employee.java
    ¦                   +-- BigScanner.java
    ¦                   +-- BigApp.java
    +-- test/
    ¦   +-- java/
    ¦       +-- co/
    ¦           +-- big/
    ¦               +-- hrd/
    ¦                   +-- BigScannerTest.java

### 3.1 Class Employee (Record)

    Employee
    +-- id: int
    +-- firstName: String
    +-- lastName: String
    +-- salary: double
    +-- managerId: Integer (nullable)

-   Immutable data structure
-   Represents a single employee
-   Uses Java record for concise implementation

### 3.2 BigScanner

    BigScanner
    +-- Constants
    ¦   +-- MAX_ALLOWED_ROWS: int = 1000
    ¦   +-- MIN_SALARY_MULTIPLIER: double = 1.20
    ¦   +-- MAX_SALARY_MULTIPLIER: double = 1.50
    ¦   +-- MAX_REPORTING_LINE: int = 4
    +-- Fields
    ¦   +-- employees: Map<Integer, Employee>
    ¦   +-- subordinates: Map<Integer, List<Employee>>
    +-- Methods
    ¦   +-- loadCsv(filePath: String): void
    ¦   +-- checkCompliance(): List<String>
    ¦   +-- checkSalaries(): List<String> (private)
    ¦   +-- checkReportee(): List<String> (private)
    ¦   +-- countManagersToCeo(employeeId: int, visited: Set): int (private)

-   Main business logic container
-   Manages employee data and analysis
-   Returns results as string list for flexibility

## 4. Functional Requirements

### 4.1 Input Processing

-   Read CSV file with format: `id,firstName,lastName,salary,managerId`
-   Handle up to 1000 records
-   CEO has null managerId
-   if there is only one record no restrictions on salary
-   Employee Id and Manager Id cannot be same
-   Throw exceptions for invalid data

### 4.2 Analysis Rules

-   Salary Analysis:
    -   Managers must earn = 20% more than subordinates' average
    -   Managers must earn = 50% more than subordinates' average
-   Reporting Line Analysis:
    -   Maximum 4 managers between employee and CEO

### 4.3 Output

-   List of strings containing:
    -   Salary violations with differences
    -   Reporting line violations with excess count
    -   Section headers

## 5. Technical Design

### 5.1 Data Structures

    employees: HashMap<Integer, Employee>
    - Key: Employee ID
    - Value: Employee object
    - O(1) lookup time

    subordinates: HashMap<Integer, List<Employee>>
    - Key: Manager ID
    - Value: List of direct subordinates
    - Efficient subordinate access

### 5.2 Algorithms

    Salary Analysis:
    1. For each manager:
       a. Calculate average subordinate salary
       b. Check against min (avg * 1.20) and max (avg * 1.50)
       c. Record violations

    Reporting Line Analysis:
    1. For each employee:
       a. Recursively count managers to CEO
       b. Record if > 4
       c. Use visited set to prevent cycles

### 5.3 Error Handling

    - IOException: File not found/reading errors
    - CsvException: Malformed CSV data
    - NumberFormatException: Invalid numeric values

## 6. Sequence Diagram

    [Main] ? [BigScanner]: loadFromFile(filePath)
    [BigScanner] ? [OpenCSV]: readAll()
    [OpenCSV] ? [BigScanner]: List<String[]>
    [Main] ? [BigScanner]: checkCompliance()
    [BigScanner] ? [Self]: checkSalaries()
    [BigScanner] ? [Self]: checkReportee()
    [BigScanner] ? [Main]: List<String>
    [Main] ? [System.out]: print results

## 7. Test Design

### 7.1 Unit Tests

    OrganizationAnalyzerTest
    +-- testNormalStructure()
    ¦   - Verify no violations case
    +-- testSalaryTooLow()
    ¦   - Check minimum salary violation
    +-- testSalaryTooHigh()
    ¦   - Check maximum salary violation
    +-- testLongReportingLine()
    ¦   - Verify excessive reporting line detection
    +-- testSampleData()
    ¦   - Test with provided sample data
    +-- testInvalidFile()
    ¦   - Verify file error handling
    +-- testMalformedData()
        - Verify CSV parsing errors

### 7.2 Test Data

    Normal Case: 3-level hierarchy, valid salaries
    Salary Too Low: Manager < 20% above average
    Salary Too High: Manager > 50% above average
    Long Line: 6-level hierarchy
    Sample Data: Provided example
    Invalid: Non-existent file
    Malformed: Invalid numeric values

## 8. Constraints and Assumptions

### 8.1 Constraints

-   Maximum 1000 employees
-   CSV format must match specification
-   Single-threaded operation

### 8.2 Assumptions

-   IDs are unique
-   Salary values are positive
-   Organizational structure forms a valid tree
-   No circular references in reporting

## 9. Future Enhancements

-   Configurable threshold values
-   Multiple output formats (JSON, HTML)
-   Performance optimization for larger datasets
-   Support for additional employee attributes
-   Batch processing capability

This design document provides a comprehensive overview of the solution,
including its architecture, class structure, functional requirements,
technical implementation details, and testing approach. It serves as a
blueprint for development and maintenance of the BigScanner system.
