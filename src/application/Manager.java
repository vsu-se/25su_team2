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
	public boolean editDailyEntry(String employeeUsername, int dayIndex, int newHours, boolean newPTO,
	    Map<String, Week> currentWeekMap, WeekRepository weekRepo, String hoursFilePath, String auditFilePath) {

	    // Get the current week object for the employee
	    Week week = currentWeekMap.get(employeeUsername);
	    if (week == null || dayIndex < 0 || dayIndex > 6) return false; // Validate input

	    // Store old values for audit
	    int oldHours = week.getHours()[dayIndex];
	    boolean oldPTO = week.getIsPTO()[dayIndex];

	    // Update the hours and PTO for the specified day
	    week.getHours()[dayIndex] = newHours;
	    week.getIsPTO()[dayIndex] = newPTO;

	    // Save the updated week to the hours file
	    try (PrintWriter writer = new PrintWriter(new FileWriter(hoursFilePath, true))) {
	        writer.println(week.toFileString());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Write an audit entry recording the change
	    try (PrintWriter writer = new PrintWriter(new FileWriter(auditFilePath, true))) {
	        writer.printf(
	            "ManagerID:%s | EmployeeID:%s | Day:%d | OldHours:%d | NewHours:%d | OldPTO:%b | NewPTO:%b\n",
	            this.getEmployeeID(), employeeUsername, dayIndex, oldHours, newHours, oldPTO, newPTO
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return true; // Indicate success
	}

	// Shows all changes to a particular Employee or Manager's hours for selected week(s)
	public void auditEmployee(String employeeId, List<Integer> weekNumbers, String auditFilePath, WeekRepository weekRepo) {
	    try (Scanner scanner = new Scanner(new java.io.File(auditFilePath))) {
	        List<String> auditEntries = new java.util.ArrayList<>();
	        // Read each line in the audit file
	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine();
	            // Check if the line is for the specified employee
	            if (line.contains("EmployeeID:" + employeeId)) {
	                // Parse week number from the line
	                String[] parts = line.split("\\|");
	                int weekNum = -1;
	                for (String part : parts) {
	                    if (part.trim().startsWith("Week:")) {
	                        weekNum = Integer.parseInt(part.trim().substring(5));
	                        break;
	                    }
	                }
	                // If weekNumbers is empty, include all weeks; otherwise, filter by weekNumbers
	                if (weekNumbers == null || weekNumbers.isEmpty() || weekNumbers.contains(weekNum)) {
	                    auditEntries.add(line);
	                }
	            }
	        }
	        // If no audit records found, notify user
	        if (auditEntries.isEmpty()) {
	            System.out.println("No audit records found for employee " + employeeId);
	            return;
	        }
	        // Display each audit entry
	        for (String entry : auditEntries) {
	            System.out.println("Audit Entry: " + entry);
	            // Optionally, reconstruct and display the full week's hours before and after
	            // Use weekRepo.getWeek(employeeId, weekNum) if needed
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	// Returns a string of audit records for this manager, filtered by mode and week/range.
	// mode: "single" (one week), "range" (week range), "all" (all weeks)
	public String auditEditor(String auditFilePath, String mode, Integer week, Integer rangeStart, Integer rangeEnd) {
	    StringBuilder sb = new StringBuilder();
	    try (Scanner scanner = new Scanner(new java.io.File(auditFilePath))) {
	        // Read each line in the audit file
	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine();
	            // Only process lines for this manager
	            if (line.contains("ManagerID:" + this.getEmployeeID())) {
	                int weekNum = -1;
	                // Split line into parts to find the week number
	                String[] parts = line.split("\\|");
	                for (String part : parts) {
	                    part = part.trim();
	                    if (part.startsWith("Week:")) {
	                        try {
	                            weekNum = Integer.parseInt(part.substring(5));
	                        } catch (NumberFormatException ignored) {}
	                        break;
	                    }
	                }
	                // Filter by mode
	                if ("single".equalsIgnoreCase(mode) && weekNum == week) {
	                    sb.append(line).append("\n");
	                } else if ("range".equalsIgnoreCase(mode) && weekNum >= rangeStart && weekNum <= rangeEnd) {
	                    sb.append(line).append("\n");
	                } else if ("all".equalsIgnoreCase(mode)) {
	                    sb.append(line).append("\n");
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    // Return results or a message if none found
	    return sb.length() == 0 ? "No audit records found." : sb.toString();
	}
}	
