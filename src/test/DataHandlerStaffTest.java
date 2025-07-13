package test;

import static org.junit.jupiter.api.Assertions.*;
import application.Manager;
import application.DataHandler;
import application.Employee;
import application.Staff;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.naming.AuthenticationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DataHandlerStaffTest {
    private DataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DataHandler("test_employees.txt");
    }

    @Test
    void addStaff_valid_addsAndPersists() {
        Staff s = new Staff("Tom", "Nash", "tnash", "pw", "HR", 18.0, 12.0, 5);

        boolean added = handler.addEmployee(s);

        assertTrue(added);
        assertTrue(handler.getStaff().stream()
                   .anyMatch(x -> x.getUsername().equalsIgnoreCase("tnash")));
    }

    @Test
    void addStaff_duplicateUsername_fails() {
        Staff s1 = new Staff("Amy", "Lee", "alee", "pw", "OPS", 18.0, 12.0, 6);
        Staff s2 = new Staff("Anna", "Li", "alee", "pw", "OPS", 18.0, 12.0, 6);

        assertTrue(handler.addEmployee(s1));
        assertFalse(handler.addEmployee(s2));
    }
}