package application;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.*;

public class DataHandler {
	protected List<Manager> managers = new ArrayList<>();
	protected List<Staff> staff = new ArrayList<>();

	public DataHandler(String filePath) {
		loadFile(filePath);
	}

//PREVENT DUPLICATE USERNAMES
	public boolean usernameExists(String username) { //helper to check for duplicate managers and staff usernames.
		for (Manager m : managers) {
			if (m.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}

		for (Staff s : staff) {
			if (s.getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}

		return false;
	}


// Add Employee to the actual list
	public boolean addEmployee(Employee emp) {
		emp.setHashedPassword(hashPassword(emp.getPassword()));
		if (usernameExists(emp.getUsername())) {
			return false;
		}
		if (emp instanceof Manager) {
			managers.add((Manager) emp);
		} else if (emp instanceof Staff) {
			staff.add((Staff) emp);
		}
		return true; // Successfully added
	}


//LOAD txt file for employee
	private void loadFile(String filePath) {
		try (Scanner scanner = new Scanner(new File(filePath))) {
			String section = "";
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty())
					continue;

				if (line.equalsIgnoreCase("managers:")) {
					section = "manager";
					continue;
				} else if (line.equalsIgnoreCase("staff:")) {
					section = "staff";
					continue;
				}

				String[] parts = line.split(",");
				if (parts.length != 8)
					continue;

				String firstName = parts[0];
				String lastName = parts[1];
				String username = parts[2];
				String hashedPassword = parts[3];
				String department = parts[4];
				double payRate = Double.parseDouble(parts[5]);
				double taxRate = Double.parseDouble(parts[6]);
				int ptoDays = Integer.parseInt(parts[7]);

				if (section.equals("manager")) {
					managers.add(new Manager(firstName, lastName, username, hashedPassword, department, payRate,
							taxRate, ptoDays));
				} else if (section.equals("staff")) {
					staff.add(new Staff(firstName, lastName, username, hashedPassword, department, payRate, taxRate,
							ptoDays));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//SAVE EMPLOYEES TO TXT FILE
	public void saveToFile(String filePath) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			writer.println("managers:");
			for (Manager m : managers) {
				writer.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%d\n", m.getFirstName(), m.getLastName(), m.getUsername(),
						m.password, m.getDepartment(), m.getPayRate(), m.getTaxRate(), m.getPtoDays());
			}

			writer.println("\nstaff:");
			for (Staff s : staff) {
				writer.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%d\n", s.getFirstName(), s.getLastName(), s.getUsername(),
						s.password, s.getDepartment(), s.getPayRate(), s.getTaxRate(), s.getPtoDays());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


//HASH PASSWORDS BEFORE STORAGE
	public static String hashPassword(String password) { // i got this off stock overflow if I am being honest requires further research on MessageDigest class.
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] encoded = digest.digest(password.getBytes());
			StringBuilder sb = new StringBuilder();
			for (byte b : encoded) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

//GETTERS
	public List<Manager> getManagers() {// used to easily return list of managers, just need enhance for loop to print 
		return managers;
	}

	public List<Staff> getStaff() {// used to easily return list of staff, just need enhance for loop to print.
		return staff;
	}

	public List<Employee> getAllEmps(){
		List<Employee> allEmps = new ArrayList<>();
        allEmps.addAll(managers);
		allEmps.addAll(staff);

		return allEmps;
	}
	
	public List<Employee> getEmployeesInDepartment(String departmentName) {
		    List<Employee> emps = new ArrayList<>();

		    for (Employee e : getAllEmps()) {
		        if (e.getDepartment().equalsIgnoreCase(departmentName)) {
		            emps.add(e);
		        }
		    }
		    emps.sort(Employee.BY_DEPARTMENT);
		    return emps;
	}
	
	public Employee findEmployeeByUsername(String username) {
	    for (Manager m : managers) {
	        if (m.getUsername().equalsIgnoreCase(username)) {
	            return m;
	        }
	    }

	    for (Staff s : staff) {
	        if (s.getUsername().equalsIgnoreCase(username)) {
	            return s;
	        }
	    }

	    return null;
	}
	
	public boolean deleteEmployeeInFile(String username) {
	    File inputFile = new File("employees.txt");
	    File tempFile = new File("employees_temp.txt");
	    File deletedFile = new File("deleted_employees.txt");

	    boolean deleted = false;
	    String deletedLine = null;
	    String section = "";

	    try (
	        Scanner scanner = new Scanner(inputFile);
	        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));
	        PrintWriter deletedWriter = new PrintWriter(new FileWriter(deletedFile, true)) // append mode
	    ) {
	        while (scanner.hasNextLine()) {
	            String line = scanner.nextLine();

	            // Preserve section headers
	            if (line.equalsIgnoreCase("managers:") || line.equalsIgnoreCase("staff:")) {
	                section = line.toLowerCase();
	                writer.println(line);
	                continue;
	            }

	            // Preserve blank lines
	            if (line.trim().isEmpty()) {
	                writer.println();
	                continue;
	            }

	            String[] parts = line.split(",");
	            if (parts.length != 8) {
	                writer.println(line);
	                continue;
	            }

	            String currentUsername = parts[2];

	            if (currentUsername.equalsIgnoreCase(username)) {
	                deleted = true;
	                deletedLine = line;

	                // First copy to deleted_employees.txt
	                deletedWriter.println(section);
	                deletedWriter.println(line);
	                deletedWriter.println(); // separator

	                // Do not write to temp file (i.e., delete from employees.txt)
	                continue;
	            }

	            writer.println(line); // Keep all other lines
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }

	    // Replace original file if deletion happened
	    if (deleted) {
	        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
	            System.err.println("File replacement failed.");
	            return false;
	        }
	    } else {
	        tempFile.delete(); // Cleanup if nothing deleted
	    }

	    return deleted;
	}
}