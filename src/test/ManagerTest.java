package test;

import static org.junit.jupiter.api.Assertions.*;
import application.Manager;
import application.Employee;
import application.Staff;
import application.Week;
import application.WeekRepository;

import java.util.ArrayList;
import java.util.List;

import javax.naming.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;



class ManagerTest {
	private List<Employee> employees;
    private Manager alice;
    private Employee bob;
    private static final String TEST_HOURS_FILE = "test_hours.txt";
    private static final String TEST_AUDIT_FILE = "test_audit_trail.txt";
    private static final String TEST_EMPLOYEES_FILE = "test_employees.txt";
    private WeekRepository weekRepo;
    private Map<String, Week> currentWeekMap;
    private Manager manager;
	
 // Clear files ONCE before all tests
    @BeforeAll
    static void cleanFilesOnce() {
        try {
            new PrintWriter(TEST_HOURS_FILE).close(); // Clear hours file
            new PrintWriter(TEST_AUDIT_FILE).close(); // Clear audit file
            new PrintWriter(TEST_EMPLOYEES_FILE).close(); // Clear employees file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    @BeforeEach
    void setUp() {
        
        employees = new ArrayList<>();

        // seed one Manager and one Staff
        alice = new Manager(
            "Alice", "Smith",
            "asmith", "secret123",
            "Payroll", 50.0, 20.0, 10
        );
        bob = new Staff(
            "Bob", "Jones",
            "bjones", "password",
            "Dev", 30.0, 15.0, 5
        );

        employees.add(alice);
        employees.add(bob);
        
        manager = alice; // Fix: assign manager
        weekRepo = new WeekRepository();
        currentWeekMap = new HashMap<>();
            
    }

    @Test
    @DisplayName("Login succeeds with valid manager credentials")
    void loginSucceedsWithValidCredentials() throws AuthenticationException {
        Manager result = Manager.login("asmith", "secret123", employees);
        assertSame(alice, result, "Should return the seeded Manager instance");
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void loginFailsWithWrongPassword() {
        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> Manager.login("asmith", "wrongpass", employees)
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Login fails for unknown username")
    void loginFailsForUnknownUsername() {
        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> Manager.login("unknownUser", "secret123", employees)
        );
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Login fails when user is not a manager")
    void loginFailsIfUserIsNotManager() {
        AuthenticationException ex = assertThrows(
            AuthenticationException.class,
            () -> Manager.login("bjones", "password", employees)
        );
        assertEquals("User is not a manager", ex.getMessage());
    }
    
    //sprint 2 test cases
    @Test
    void testEditDailyEntryPersistsAndAudits() throws Exception {
        String employeeUsername = bob.getUsername(); // Use bob's username for testing
        int[] initialHours = {8, 8, 8, 8, 8, 0, 0};
        boolean[] initialPTO = {false, false, false, false, false, false, false};
        Week week = new Week(bob.getEmployeeID(), 1, initialHours, initialPTO);
        currentWeekMap.put(bob.getUsername(), week);

        int dayIndex = 1; // Tuesday
        int newHours = 5;
        boolean newPTO = true;
        boolean result = manager.editDailyEntry(employeeUsername, dayIndex, newHours, newPTO, currentWeekMap, weekRepo, TEST_HOURS_FILE, TEST_AUDIT_FILE);

        Assertions.assertTrue(result);
        Assertions.assertEquals(newHours, week.getHours()[dayIndex]);
        Assertions.assertTrue(week.getIsPTO()[dayIndex]);

        // Check audit trail file
        List<String> auditLines = Files.readAllLines(Paths.get(TEST_AUDIT_FILE));
        Assertions.assertFalse(auditLines.isEmpty());
        String lastLine = auditLines.get(auditLines.size() - 1);
        Assertions.assertTrue(lastLine.contains("ManagerID:" + manager.getEmployeeID()));
        Assertions.assertTrue(lastLine.contains("EmployeeID:" + employeeUsername));
        Assertions.assertTrue(lastLine.contains("Day:" + dayIndex));
        Assertions.assertTrue(lastLine.contains("OldHours:8"));
        Assertions.assertTrue(lastLine.contains("NewHours:5"));
        Assertions.assertTrue(lastLine.contains("OldPTO:false"));
        Assertions.assertTrue(lastLine.contains("NewPTO:true"));
    }

    
    @Test
    void testAuditEmployee_AllWeeks() throws Exception {
        // Write sample audit entries to the audit file
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_AUDIT_FILE))) {
            writer.println("ManagerID:0001 | EmployeeID:" + bob.getEmployeeID() + " | Week:1 | Day:0 | OldHours:8 | NewHours:9 | OldPTO:false | NewPTO:false");
            writer.println("ManagerID:0001 | EmployeeID:" + bob.getEmployeeID() + " | Week:2 | Day:1 | OldHours:7 | NewHours:8 | OldPTO:false | NewPTO:true");
            writer.println("ManagerID:0001 | EmployeeID:9999 | Week:1 | Day:2 | OldHours:6 | NewHours:7 | OldPTO:false | NewPTO:false");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        manager.auditEmployee(bob.getEmployeeID(), Collections.emptyList(), TEST_AUDIT_FILE, weekRepo);

        System.setOut(originalOut);
        String output = out.toString();
        Assertions.assertTrue(output.contains("Week:1"));
        Assertions.assertTrue(output.contains("Week:2"));
        Assertions.assertFalse(output.contains("EmployeeID:9999"));
    }

    @Test
    void testAuditEmployee_SpecificWeek() throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_AUDIT_FILE))) {
            writer.println("ManagerID:0001 | EmployeeID:" + bob.getEmployeeID() + " | Week:1 | Day:0 | OldHours:8 | NewHours:9 | OldPTO:false | NewPTO:false");
            writer.println("ManagerID:0001 | EmployeeID:" + bob.getEmployeeID() + " | Week:2 | Day:1 | OldHours:7 | NewHours:8 | OldPTO:false | NewPTO:true");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        manager.auditEmployee(bob.getEmployeeID(), Arrays.asList(1), TEST_AUDIT_FILE, weekRepo);

        System.setOut(originalOut);
        String output = out.toString();
        Assertions.assertTrue(output.contains("Week:1"));
        Assertions.assertFalse(output.contains("Week:2"));
    }

    @Test
    void testAuditEmployee_NoRecords() throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_AUDIT_FILE))) {
            writer.println("ManagerID:0001 | EmployeeID:9999 | Week:1 | Day:2 | OldHours:6 | NewHours:7 | OldPTO:false | NewPTO:false");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        manager.auditEmployee("notfound", Collections.emptyList(), TEST_AUDIT_FILE, weekRepo);

        System.setOut(originalOut);
        String output = out.toString();
        Assertions.assertTrue(output.contains("No audit records found for employee notfound"));
    }
    
    @Test
    void testAuditEditorSingleWeek() throws Exception {
        // Write two audit entries for different weeks
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_AUDIT_FILE))) {
            writer.println("ManagerID:" + alice.getEmployeeID() + " | EmployeeID:" + bob.getEmployeeID() + " | Week:1 | Day:0 | OldHours:8 | NewHours:9 | OldPTO:false | NewPTO:false");
            writer.println("ManagerID:" + alice.getEmployeeID() + " | EmployeeID:" + bob.getEmployeeID() + " | Week:2 | Day:1 | OldHours:7 | NewHours:8 | OldPTO:false | NewPTO:true");
        }
        String result = alice.auditEditor(TEST_AUDIT_FILE, "single", 1, null, null);
        Assertions.assertTrue(result.contains("Week:1"));
        Assertions.assertFalse(result.contains("Week:2"));
    }

    @Test
    void testAuditEditorAllWeeks() throws Exception {
        // Write two audit entries for different weeks
        try (PrintWriter writer = new PrintWriter(new FileWriter(TEST_AUDIT_FILE))) {
            writer.println("ManagerID:" + alice.getEmployeeID() + " | EmployeeID:" + bob.getEmployeeID() + " | Week:1 | Day:0 | OldHours:8 | NewHours:9 | OldPTO:false | NewPTO:false");
            writer.println("ManagerID:" + alice.getEmployeeID() + " | EmployeeID:" + bob.getEmployeeID() + " | Week:2 | Day:1 | OldHours:7 | NewHours:8 | OldPTO:false | NewPTO:true");
        }
        String result = alice.auditEditor(TEST_AUDIT_FILE, "all", null, null, null);
        Assertions.assertTrue(result.contains("Week:1"));
        Assertions.assertTrue(result.contains("Week:2"));
    }
    @Test
    void testEditEmployeeUpdatesFields() {
        Employee emp = new Staff("John", "Doe", "jdoe", "pass", "Sales", 25.0, 10.0, 5);
        Manager mgr = new Manager("Alice", "Smith", "asmith", "secret123", "Payroll", 50.0, 20.0, 10);

        boolean changed = mgr.editEmployee(emp, "Jane", "Smith", "newpass", "Marketing", 30.0, 12.0);

        assertTrue(changed);
        assertEquals("Jane", emp.getFirstName());
        assertEquals("Smith", emp.getLastName());
        assertTrue(emp.authenticate("newpass"));
        assertEquals("Marketing", emp.getDepartment());
        assertEquals(30.0, emp.getPayRate());
        assertEquals(12.0, emp.getTaxRate());
    }

 }
