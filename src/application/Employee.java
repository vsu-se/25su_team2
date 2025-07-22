package application;

import java.util.Comparator;

public abstract class Employee {
	protected static int counterID = 1;
	protected String firstName, lastName, username, password, department;
	protected double payRate, taxRate;
	protected int ptoDays;
	protected int employeeID;

	public Employee(String firstName, String lastName, String username, String password, String department,
					double payRate, double taxRate, int ptoDays) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.department = department;
		this.payRate = payRate;
		this.taxRate = taxRate;
		this.ptoDays = ptoDays;
		this.employeeID = counterID ++;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public String getUsername() {
		return username;
	}
	
	//Password logic

	
	public String getDepartment() {
		return department;
	}
	
	public double getPayRate() {
		return payRate;
	}
	
	public double getTaxRate() {
		return taxRate;
	}
	
	public int getPtoDays() {
		return ptoDays;
	}
	
	public String getEmployeeID() {
		return String.format("%04d", employeeID);
	}
	
	public String getFullName() {
		return firstName + " " + lastName + "(Username : " + username + ")";
	}

	public String getFullNameNoUser(){ return firstName + " " + lastName;}

	public static final Comparator<Employee> DEFAULT_COMPARATOR =
		    Comparator.comparing(Employee::getLastName, String.CASE_INSENSITIVE_ORDER)
		              .thenComparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER)
		              .thenComparing(Employee::getDepartment, String.CASE_INSENSITIVE_ORDER)
		              .thenComparing(Employee::getEmployeeID);
	
	public static final Comparator<Employee> BY_DEPARTMENT = Comparator
		    .comparing(Employee::getDepartment, String.CASE_INSENSITIVE_ORDER)
		    .thenComparing(e -> (e instanceof Manager) ? 0 : 1)
		    .thenComparing(Employee::getLastName, String.CASE_INSENSITIVE_ORDER)
			.thenComparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER)
			.thenComparing(Employee::getEmployeeID);
	
	//Password logic and helpers 
	String getPassword() {
		return password;
	}
	
	void setHashedPassword(String hashedPassword) {
		this.password = hashedPassword;
	}
	
	public boolean changePassword(String oldPassword, String newPassword) {
	    if (password.equals(DataHandler.hashPassword(oldPassword))) {
	        password = DataHandler.hashPassword(newPassword);
	        return true;
	    }
	    return false;
	}

	public boolean authenticate(String candidate) {
	    if (candidate == null) return false;
	    String hashed = DataHandler.hashPassword(candidate);
	    return hashed.equals(this.password);
	}
}
