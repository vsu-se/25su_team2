package application;

import java.util.ArrayList;
import java.util.List;

public class PayRollCalculator {

	public static class PayStub {
	    public double grossPay, taxes, netPay;
	    public int totalHours, ptoUsed;
	    public int[] dailyHours;       // hours for Mon–Sun
	    public boolean[] ptoFlags;     // true if PTO was used that day

	    public PayStub(double grossPay, double taxes, double netPay,
	                   int totalHours, int ptoUsed,
	                   int[] dailyHours, boolean[] ptoFlags) {
	        this.grossPay = grossPay;
	        this.taxes = taxes;
	        this.netPay = netPay;
	        this.totalHours = totalHours;
	        this.ptoUsed = ptoUsed;
	        this.dailyHours = dailyHours;
	        this.ptoFlags = ptoFlags;
	    }

//toString for printing to GUI
	    @Override
	    public String toString() {
	        StringBuilder sb = new StringBuilder();
	        sb.append(String.format("Total Hours Worked: %d hrs\n", totalHours));
	        sb.append(String.format("PTO Used: %d hrs\n", ptoUsed));
	        sb.append(String.format("Gross Pay: $%.2f\nTaxes: $%.2f\nNet Pay: $%.2f\n", grossPay, taxes, netPay));
	        sb.append("Hours:\n");
	        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
	        for (int i = 0; i < 7; i++) {
	            sb.append(String.format("  %s: %d hrs", days[i], dailyHours[i]));
	            if (ptoFlags[i]) sb.append(" (PTO)");
	            sb.append("\n");
	        }
	        return sb.toString();
	    }
	}

//Main call for payment calculations
	public static PayStub calculatePay(Employee emp, Week week) {
	    if (emp instanceof Manager) {
	        return calculateManagerPay(emp, week); // <- Pass week here
	    } else {
	        return calculateStaffPay(emp, week);
	    }
	}
	
	public static PayStub calculatePay(Employee emp, List<Week> weeks) {
	    if (weeks == null || weeks.isEmpty()) return null;

	    double totalGross = 0, totalTax = 0, totalNet = 0;
	    int totalHours = 0, totalPTO = 0;

	    int[] combinedDailyHours = new int[7];
	    boolean[] combinedPtoFlags = new boolean[7];

	    for (Week w : weeks) {
	        PayStub stub = calculatePay(emp, w);
	        if (stub != null) {
	            totalGross += stub.grossPay;
	            totalTax += stub.taxes;
	            totalNet += stub.netPay;
	            totalHours += stub.totalHours;
	            totalPTO += stub.ptoUsed;

	            for (int i = 0; i < 7; i++) {
	                combinedDailyHours[i] += stub.dailyHours[i];
	                combinedPtoFlags[i] |= stub.ptoFlags[i];
	            }
	        }
	    }

	    return new PayStub(totalGross, totalTax, totalNet, totalHours, totalPTO, combinedDailyHours, combinedPtoFlags);
	}
	
	public static String generateReport(Employee emp, List<Week> history, Week current, String mode, Integer rangeStart, Integer rangeEnd) {
	    List<Week> result = new ArrayList<>();

	    switch (mode) {
	        case "current":
	            if (current != null) result.add(current);
	            break;
	        case "all":
	            result.addAll(history);
	            break;
	        case "range":
	            if (rangeStart == null || rangeEnd == null || rangeStart > rangeEnd)
	                return "Invalid range.";
	            for (Week w : history) {
	                int num = w.getWeekNumber();
	                if (num >= rangeStart && num <= rangeEnd)
	                    result.add(w);
	            }
	            break;
	    }

	    if (result.isEmpty())
	        return "No week data found.";

	    StringBuilder sb = new StringBuilder();
	    sb.append(String.format("Employee: %s %s  |  ID: %s  | Dept: %s \n", emp.getFirstName(), emp.getLastName(), emp.getEmployeeID(), emp.getDepartment()));
	    sb.append("====================================================================\n");

	    double totalGross = 0, totalTaxes = 0, totalNet = 0;
	    int totalWeekdayHours = 0, totalOvertime = 0, totalPTO = 0;

	    for (Week w : result) {
	        PayStub stub = calculatePay(emp, w);
	        sb.append(String.format("Week #%d:\n", w.getWeekNumber()));
	        sb.append(String.format("%-8s%-6s%-6s%-6s%-6s%-6s%-6s%-6s\n",
	            "Day", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));

	        sb.append(String.format("%-8s", "Hours"));
	        for (int h : stub.dailyHours) {
	            sb.append(String.format("%-6d", h));
	        }
	        sb.append("\n");

	        sb.append(String.format("%-8s", "PTO"));
	        for (boolean pto : stub.ptoFlags) {
	            sb.append(String.format("%-6s", pto ? "Yes" : "No"));
	        }
	        sb.append("\n");

	        // Calculate weekday and overtime
	        int weekdayHours = 0;
	        int weekendHours = 0;
	        int nonPTOWeekdayHours = 0;
	        for (int i = 0; i < 7; i++) {
	            if (i < 5) {
	                weekdayHours += stub.dailyHours[i];
	                if (!stub.ptoFlags[i]) nonPTOWeekdayHours += stub.dailyHours[i];
	            } else {
	                weekendHours += stub.dailyHours[i];
	            }
	        }

	        int overtimeHours = 0;
	        if (nonPTOWeekdayHours > 40)
	            overtimeHours += nonPTOWeekdayHours - 40;
	        overtimeHours += weekendHours;

	        // Week summary
	        sb.append("\n");
	        sb.append(String.format("%-20s %3d hrs\n", "Weekday Hours:", weekdayHours));
	        sb.append(String.format("%-20s %3d hrs\n", "Overtime Hours:", overtimeHours));
	        sb.append(String.format("%-20s $%7.2f\n", "Gross Pay:", stub.grossPay));
	        sb.append(String.format("%-20s $%7.2f\n", "Taxes:", stub.taxes));
	        sb.append(String.format("%-20s $%7.2f\n", "Net Pay:", stub.netPay));
	        sb.append(String.format("%-20s %3d hrs\n", "PTO Used:", stub.ptoUsed));
	        sb.append("====================================================================\n\n");

	        // Accumulate totals
	        totalGross += stub.grossPay;
	        totalTaxes += stub.taxes;
	        totalNet += stub.netPay;
	        totalWeekdayHours += weekdayHours;
	        totalOvertime += overtimeHours;
	        totalPTO += stub.ptoUsed;
	    }
	    
	    if (result.size() > 1) {
	        sb.append(" TOTAL :\n");
	        sb.append(String.format("%-20s %3d hrs\n", "Total Weekday Hours:", totalWeekdayHours));
	        sb.append(String.format("%-20s %3d hrs\n", "Total Overtime:", totalOvertime));
	        sb.append(String.format("%-20s $%7.2f\n", "Total Gross Pay:", totalGross));
	        sb.append(String.format("%-20s $%7.2f\n", "Total Taxes:", totalTaxes));
	        sb.append(String.format("%-20s $%7.2f\n", "Total Net Pay:", totalNet));
	        sb.append(String.format("%-20s %3d hrs\n", "Total PTO Used:", totalPTO));
	        sb.append("====================================================================\n");
	    }

	    return sb.toString();
	}


	

	
//HELPERS

	private static PayStub calculateManagerPay(Employee emp, Week week) {
	    int[] hours = week.getHours();
	    boolean[] isPTO = week.getIsPTO();

	    double payRate = emp.getPayRate();
	    double taxRate = emp.getTaxRate() / 100.0;

	    int totalWorked = 0;
	    int totalPTO = 0;

	    for (int i = 0; i < 7; i++) {
	        totalWorked += hours[i];
	        if (isPTO[i]) totalPTO += 8;
	    }

	    double grossPay = payRate * 40;
	    double taxes = grossPay * taxRate;
	    double netPay = grossPay - taxes;

	    return new PayStub(grossPay, taxes, netPay, totalWorked, totalPTO, hours, isPTO);
	}

	private static PayStub calculateStaffPay(Employee emp, Week week) {
	    int[] hours = week.getHours();
	    boolean[] isPTO = week.getIsPTO();

	    double payRate = emp.getPayRate();
	    double taxRate = emp.getTaxRate() / 100.0;

	    int totalWorked = 0;
	    int totalPTO = 0;
	    int weekdayWorked = 0;
	    double weekendPay = 0;

	    int[] dailyHours = new int[7];
	    boolean[] ptoFlags = new boolean[7];

	    for (int i = 0; i < 7; i++) {
	        int dayHours = hours[i];
	        boolean tookPTO = isPTO[i];

	        dailyHours[i] = dayHours;
	        ptoFlags[i] = tookPTO;

	        if (i < 5) { // Weekday
	            if (tookPTO) {
	                totalPTO += 8;
	                continue;
	            }
	            weekdayWorked += dayHours;
	            totalWorked += dayHours;
	        } else { // Weekend
	            weekendPay += dayHours * payRate * 1.5;
	            totalWorked += dayHours;
	        }
	    }

	    double weekdayPay;
	    if (weekdayWorked <= 40) {
	        weekdayPay = weekdayWorked * payRate;
	    } else {
	        int regular = 40;
	        int overtime = weekdayWorked - 40;
	        weekdayPay = regular * payRate + overtime * payRate * 1.5;
	    }

	    double grossPay = weekdayPay + weekendPay;
	    double taxes = grossPay * taxRate;
	    double netPay = grossPay - taxes;

	    return new PayStub(grossPay, taxes, netPay, totalWorked, totalPTO, dailyHours, ptoFlags);
	}
}