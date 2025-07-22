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
    private WeekRepository weekRepo;
    private Map<String, Week> currentWeekMap;
    private Manager manager;
	
 // Clear files ONCE before all tests
    @BeforeAll
    static void cleanFilesOnce() {
        try {
            new PrintWriter(TEST_HOURS_FILE).close(); // Clear hours file
            new PrintWriter(TEST_AUDIT_FILE).close(); // Clear audit file
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

	/*
	 * @Test void testEditDailyEntryFailsForMissingEmployee() throws Exception {
	 * String missingUsername = "not_a_real_user"; int dayIndex = 0; int newHours =
	 * 8; boolean newPTO = false;
	 * 
	 * boolean result = manager.editDailyEntry( missingUsername, dayIndex, newHours,
	 * newPTO, currentWeekMap, weekRepo, TEST_HOURS_FILE, TEST_AUDIT_FILE );
	 * 
	 * Assertions.assertFalse(result);
	 * 
	 * // Audit file should remain empty List<String> auditLines =
	 * Files.readAllLines(Paths.get(TEST_AUDIT_FILE));
	 * Assertions.assertTrue(auditLines.isEmpty()); }
	 */
}
