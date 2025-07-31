package application;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.naming.AuthenticationException;

public class Manager extends Employee {

	public Manager(String firstName, String lastName, String username, String password, String department,
			double payRate, double taxRate, int ptoDays) {
		super(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
	}
	

	public static Manager login(String username, String password, List<Employee> employees)
			throws AuthenticationException {
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

	/*
	 * boolean success = manager.editDailyEntry( employeeUsername, dayIndex,
	 * newHours, newPTO, currentWeekMap, weekRepo, "hours.txt", "audit_trail.txt" );
	 */
	// Edits hours for an employee only if the employee exists in the given
	// employees file.
	// Returns true if the edit and audit succeed, false otherwise.
	public boolean editSelectiveDays(Employee emp, Week selectedWeek, int[] newHours, boolean[] newPTOs,
	        WeekRepository weekRepo, String auditFilePath) {

	    if (emp == null || selectedWeek == null || newHours.length != 7 || newPTOs.length != 7)
	        return false;

	    int[] oldHours = selectedWeek.getHours().clone();
	    boolean[] oldPTOs = selectedWeek.getIsPTO().clone();

	    boolean changed = false;
	    
	    for (int i = 0; i < newHours.length; i++) {
	        if (newHours[i] < 0 || newHours[i] > 24) {
	            return false;
	        }
	    }

	    // Apply changes and track if anything changed
	    for (int i = 0; i < 7; i++) {
	        if (oldHours[i] != newHours[i] || oldPTOs[i] != newPTOs[i]) {
	            selectedWeek.getHours()[i] = newHours[i];
	            selectedWeek.getIsPTO()[i] = newPTOs[i];
	            changed = true;
	        }
	    }


	    if (!changed) {
	        return false; 
	    }

	    // Save changes
	    weekRepo.save();

	    // Audit log: Before and After side-by-side
	    try (PrintWriter writer = new PrintWriter(new FileWriter(auditFilePath, true))) {
	        writer.printf("=== Week %d Edit by ManagerID:%s for EmployeeID:%s ===\n\n",
	                selectedWeek.getWeekNumber(), this.getEmployeeID(), emp.getEmployeeID());

	        writer.println("Day   | Before (Hours / PTO)   | After (Hours / PTO)");
	        writer.println("----------------------------------------------------");

	        for (int i = 0; i < 7; i++) {
	            writer.printf("%-5s | %-7d / %-10b | %-7d / %-10b\n",
	                    getDayAbbrev(i),
	                    oldHours[i], oldPTOs[i],
	                    selectedWeek.getHours()[i], selectedWeek.getIsPTO()[i]);
	        }

	        writer.println();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return true; // <-- Ensures method always returns a boolean
	}
	
	// Shows all changes to a particular Employee or Manager's hours for selected
	// week(s)
	public String auditEmployeeFull(Employee emp, String auditFilePath) {
	    StringBuilder result = new StringBuilder();
	    try (Scanner scanner = new Scanner(new File(auditFilePath))) {
	        StringBuilder currentBlock = new StringBuilder();
	        boolean inRelevantBlock = false;

	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine();

	            // Check if this is a new block start
	            if (line.startsWith("=== Week")) {
	                // If we were collecting a relevant block, append it now
	                if (inRelevantBlock) {
	                    result.append(currentBlock.toString()).append("\n");
	                }

	                // Reset block and check if it's for our target employee
	                currentBlock = new StringBuilder();
	                inRelevantBlock = line.contains("EmployeeID:" + emp.getEmployeeID());
	                if (inRelevantBlock) {
	                    currentBlock.append(line).append("\n");
	                }
	            } else if (inRelevantBlock) {
	                currentBlock.append(line).append("\n");
	            }
	        }

	        // Append final block if relevant
	        if (inRelevantBlock) {
	            result.append(currentBlock.toString());
	        }

	    } catch (Exception e) {
	        return "Error reading audit log: " + e.getMessage();
	    }

	    if (result.length() == 0) {
	        return "No audit records found for " + emp.getFullName() + " (" + emp.getEmployeeID() + ")";
	    }

	    return result.toString();
	}

	// Returns a string of audit records for this manager, filtered by mode and
	// week/range.
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
							} catch (NumberFormatException ignored) {
							}
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

	// Edits the fields of an Employee object if new values are provided and
	// different from current.
	// Returns true if any field was changed, false otherwise.
	// Note: Password is hashed before storing.
	// Only update fields if the new value is provided and different from the
	// current value
	public boolean editEmployee(Employee emp, String newFirstName, String newLastName, String newPassword,
			String newDepartment, Double newPayRate, Double newTaxRate) {
		boolean changed = false;

		// First name
		if (newFirstName != null && !newFirstName.isBlank() && !newFirstName.equals(emp.getFirstName())) {
			emp.firstName = newFirstName;
			changed = true;
		}
		// Last name
		if (newLastName != null && !newLastName.isBlank() && !newLastName.equals(emp.getLastName())) {
			emp.lastName = newLastName;
			changed = true;
		}
		// password
		if (newPassword != null && !newPassword.isBlank()) {
			String currentHash = emp.getPassword();
			String newHash = DataHandler.hashPassword(newPassword);
			if (!newHash.equals(currentHash)) {
				emp.setHashedPassword(newHash);
				changed = true;
			}
		}
		// Department
		if (newDepartment != null && !newDepartment.isBlank() && !newDepartment.equals(emp.getDepartment())) {
			emp.department = newDepartment;
			changed = true;
		}
		// Pay rate
		if (newPayRate != null && newPayRate > 0 && Double.compare(newPayRate, emp.getPayRate()) != 0) {
			emp.payRate = newPayRate;
			changed = true;
		}
		// Tax rate
		if (newTaxRate != null && newTaxRate >= 0 && Double.compare(newTaxRate, emp.getTaxRate()) != 0) {
			emp.taxRate = newTaxRate;
			changed = true;
		}

		return changed;
	}

	private String getDayAbbrev(int i) {
		return switch (i) {
		case 0 -> "Mon";
		case 1 -> "Tue";
		case 2 -> "Wed";
		case 3 -> "Thu";
		case 4 -> "Fri";
		case 5 -> "Sat";
		case 6 -> "Sun";
		default -> "?";
		};
	}
}
