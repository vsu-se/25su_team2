package application;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.naming.AuthenticationException;

public class Manager extends Employee {

	public Manager(String firstName, String lastName, String username, String password, String department,
			double payRate, double taxRate, int ptoDays) {
		super(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
	}
	
	public static Manager login(String username,String password,List<Employee> employees) throws AuthenticationException {
		// 1. Find the employee with the matching username
		Employee found = null;
		for (Employee e : employees) {
			if (e.getUsername().equals(username)) {
				found = e;
				break;
				}
			}
			// 2. No such user?
			if (found == null) {
			throw new AuthenticationException("Invalid username or password");
			}
			// 3. Not a manager?
			if (!(found instanceof Manager)) {
			throw new AuthenticationException("User is not a manager");
			}
			// 4. Wrong password?
			if (!found.getPassword().equals(password)) {
			throw new AuthenticationException("Invalid username or password");
			}
			// 5. Success!
			return (Manager) found;
		}
	
	// Edits a daily entry for an employee only if the employee exists in the given employees file.
	// Returns true if the edit and audit succeed, false otherwise.
	// Manager.java
	public boolean editDailyEntry(String employeeUsername, int dayIndex, int newHours, boolean newPTO,
	    Map<String, Week> currentWeekMap, WeekRepository weekRepo, String hoursFilePath, String auditFilePath) {

	    Week week = currentWeekMap.get(employeeUsername);
	    if (week == null || dayIndex < 0 || dayIndex > 6) return false;

	    int oldHours = week.getHours()[dayIndex];
	    boolean oldPTO = week.getIsPTO()[dayIndex];

	    week.getHours()[dayIndex] = newHours;
	    week.getIsPTO()[dayIndex] = newPTO;

	    try (PrintWriter writer = new PrintWriter(new FileWriter(hoursFilePath, true))) {
	        writer.println(week.toFileString());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    try (PrintWriter writer = new PrintWriter(new FileWriter(auditFilePath, true))) {
	        writer.printf(
	            "ManagerID:%s | EmployeeID:%s | Day:%d | OldHours:%d | NewHours:%d | OldPTO:%b | NewPTO:%b\n",
	            this.getEmployeeID(), employeeUsername, dayIndex, oldHours, newHours, oldPTO, newPTO
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return true;
	}
}	
