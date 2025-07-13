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

class DataHandlerManagerTest {
    private DataHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DataHandler("test_employees.txt");
    }

    @Test
    void addManager_valid_addsAndPersists() {
        Manager m = new Manager("Alice", "Chen", "achen", "pw", "HR", 38.0, 22.0, 8);

        boolean added = handler.addEmployee(m);

        assertTrue(added);
        assertTrue(handler.getManagers().stream()
                   .anyMatch(x -> x.getUsername().equalsIgnoreCase("achen")));
    }


  @Test
  void addManager_notSavedToFile() throws Exception {
	  String filePath = "test_employees.txt";
	  Manager m1 = new Manager("Bob", "Lee", "blee", "pw", "SALES", 30.0, 20.0, 8);
	  Manager m2 = new Manager("Ben", "Li", "blee", "pw", "SALES", 30.0, 20.0, 8);

	  handler.addEmployee(m1);
	  handler.addEmployee(m2); // Should not be added
	  handler.saveToFile(filePath);
	  
	  long count = Files.lines(Paths.get(filePath))
        .filter(line -> line.contains("blee"))
        .count();
	  assertEquals(1, count); // Only one "blee" should be saved
}
    
    @Test
    void addManager_duplicateUsername_fails() {
        Manager m1 = new Manager("Bob", "Lee", "blee", "pw", "SALES", 30.0, 20.0, 8);
        Manager m2 = new Manager("Ben", "Li",  "blee", "pw", "SALES", 30.0, 20.0, 8);

        assertTrue(handler.addEmployee(m1));
        assertFalse(handler.addEmployee(m2));
    }
}