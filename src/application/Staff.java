package application;

public class Staff extends Employee {

	public Staff(String firstName, String lastName, String username, String password, String department, double payRate,
				double taxRate, int ptoDays) {
		super(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
	}
	
	/** Serialize for file (8 fields, comma-delimited) */
    public String toDataLine() {
        return String.join(",",
            getFirstName(),
            getLastName(),
            getUsername(),
            getPassword(),     // already hashed
            getDepartment(),
            String.valueOf(getPayRate()),
            String.valueOf(getTaxRate()),
            String.valueOf(getPtoDays())
        );
    }
}
