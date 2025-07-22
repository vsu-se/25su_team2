package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import application.Staff;

class StaffTest {

	@Test
	@DisplayName("Staff employeeID increments with each new Staff")
	void staffEmployeeIdIncrements() {
	    Staff s1 = new Staff("Tom", "Nash", "tnash", "pw", "HR", 18.0, 12.0, 5);
	    Staff s2 = new Staff("Amy", "Lee", "alee", "pw", "OPS", 18.0, 12.0, 6);
	    Staff s3 = new Staff("Anna", "Li", "ali", "pw", "OPS", 18.0, 12.0, 6);

	    int id1 = Integer.parseInt(s1.getEmployeeID());
	    int id2 = Integer.parseInt(s2.getEmployeeID());
	    int id3 = Integer.parseInt(s3.getEmployeeID());

	    assertEquals(id1 + 1, id2);
	    assertEquals(id2 + 1, id3);
	}

}
