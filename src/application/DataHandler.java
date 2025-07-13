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

//STORAGE OF EMPLOYEES
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

//HELPERS
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

	public List <Employee> getEmployeesByDepartment(){
		List<Employee> empsByDep = getAllEmps();
		Collections.sort(empsByDep, new Comparator<Employee>(){
			@Override
			public int compare(Employee e1, Employee e2) {
				int cmp = e1.getDepartment().compareToIgnoreCase(e2.getDepartment());
				if (cmp != 0) {
					return cmp;
				}
				cmp = e1.getLastName().compareToIgnoreCase(e2.getLastName());
				if (cmp != 0) {
					return cmp;
				}
				cmp = e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
				if (cmp != 0) {
					return cmp;
				}
				return e1.getEmployeeID().compareTo(e2.getEmployeeID());
			}
		});
		return empsByDep;
	}
}