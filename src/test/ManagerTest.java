package test;

import static org.junit.jupiter.api.Assertions.*;
import application.Manager;
import application.Employee;
import application.Staff;
import java.util.ArrayList;
import java.util.List;

import javax.naming.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ManagerTest {
	private List<Employee> employees;
    private Manager alice;
    private Employee bob;
	
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
    }

//    @Test
//    @DisplayName("Login succeeds with valid manager credentials")
//    void loginSucceedsWithValidCredentials() throws AuthenticationException {
//        Manager result = Manager.login("asmith", "secret123", employees);
//        assertSame(alice, result, "Should return the seeded Manager instance");
//    }
//
//    @Test
//    @DisplayName("Login fails with wrong password")
//    void loginFailsWithWrongPassword() {
//        AuthenticationException ex = assertThrows(
//            AuthenticationException.class,
//            () -> Manager.login("asmith", "wrongpass", employees)
//        );
//        assertEquals("Invalid username or password", ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("Login fails for unknown username")
//    void loginFailsForUnknownUsername() {
//        AuthenticationException ex = assertThrows(
//            AuthenticationException.class,
//            () -> Manager.login("unknownUser", "secret123", employees)
//        );
//        assertEquals("Invalid username or password", ex.getMessage());
//    }
//
//    @Test
//    @DisplayName("Login fails when user is not a manager")
//    void loginFailsIfUserIsNotManager() {
//        AuthenticationException ex = assertThrows(
//            AuthenticationException.class,
//            () -> Manager.login("bjones", "password", employees)
//        );
//        assertEquals("User is not a manager", ex.getMessage());
//    }
    
	@Test
	@DisplayName("Total Hours Test:")
	void getTotalHoursTest_case1() {
		fail("Not yet implemented");
	}
	
	@Test
	@DisplayName("Set Hours Test")
	void setHoursTest_case1() {
		fail("Not yet implemented");
	}

}
