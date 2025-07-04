package application;

import java.util.List;

import javax.naming.AuthenticationException;

public class Manager extends Employee {

	public Manager(String firstName, String lastName, String username, String password, String department,
			double payRate, double taxRate, int ptoDays) {
		super(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
	}
	
	
	//Authenticate a Manager by username/password.
	 
	public static Manager login(String username,
	                            String password,
	                            List<Employee> employees)
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
	    if (!found.authenticate(password)) {
	        throw new AuthenticationException("Invalid username or password");
	    }
	    // 5. Success!
	    return (Manager) found;
	}
}
