package application;

public abstract class Employee {
	protected static int counterID = 1;
	protected String firstName, lastName, username, password, department;
	protected double payRate, taxRate;
	protected int ptoDays;
	protected int employeeID;
	protected int[] hours = new int[7];
	

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
	
	public String getPassword() {
		return password;
	}
	
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
	
	
	
	public void setHours(int day, int hours) {
		if((day >= 0 && day < 7)&&(hours >=0 && hours <=24)) {
			this.hours[day] = hours;
		}
	}
	
	public int[] getHours() {
		return hours;
	}
	
	public String getFullName() {
		return firstName + " " + lastName;
	}

}
