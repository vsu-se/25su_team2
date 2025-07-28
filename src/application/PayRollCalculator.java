package application;

import java.util.ArrayList;
import java.util.List;

public class PayRollCalculator {

	public static class PayStub {
		public double grossPay;
		public double taxes;
		public double netPay;
		public int totalHours;
		public int ptoUsed;

		public PayStub(double grossPay, double taxes, double netPay, int totalHours, int ptoUsed) {
			this.grossPay = grossPay;
			this.taxes = taxes;
			this.netPay = netPay;
			this.totalHours = totalHours;
			this.ptoUsed = ptoUsed;
		}

//toString for printing to GUI
		@Override
		public String toString() {
			return String.format(
					"Total Hours Worked: %d hrs\nPTO Used: %d hrs\nGross Pay: $%.2f\nTaxes: $%.2f\nNet Pay: $%.2f",
					totalHours, ptoUsed, grossPay, taxes, netPay);
		}
	}

//Main call for payment calculations
	public static PayStub calculatePay(Employee emp, Week week) {
		if (emp instanceof Manager) {
			return calculateManagerPay(emp);
		} else {
			return calculateStaffPay(emp, week);
		}
	}

	public static String generateReport(Employee emp, List<Week> history, Week current, String mode, Integer rangeStart,
			Integer rangeEnd) {

		List<Week> result = new ArrayList<>();

		switch (mode) {
		case "current":
			if (current != null)
				result.add(current);
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

		List<PayStub> stubs = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		for (Week w : result) {
			PayStub stub = calculatePay(emp, w);
			stubs.add(stub);
			sb.append("Week #").append(w.getWeekNumber()).append("\n");
			sb.append(stub).append("\n\n");
		}

		if (stubs.size() > 1) {
			double totalGross = 0, totalTax = 0, totalNet = 0;
			int totalHours = 0, totalPTO = 0;

			for (PayStub stub : stubs) {
				totalGross += stub.grossPay;
				totalTax += stub.taxes;
				totalNet += stub.netPay;
				totalHours += stub.totalHours;
				totalPTO += stub.ptoUsed;
			}

			sb.append("===== SUMMARY =====\n");
			sb.append("Total Weeks: ").append(stubs.size()).append("\n");
			sb.append("Total Hours Worked: ").append(totalHours).append(" hrs\n");
			sb.append("Total PTO Used: ").append(totalPTO).append(" hrs\n");
			sb.append(String.format("Total Gross Pay: $%.2f\n", totalGross));
			sb.append(String.format("Total Taxes: $%.2f\n", totalTax));
			sb.append(String.format("Total Net Pay: $%.2f\n", totalNet));
		}

		return sb.toString();
	}
	
//HELPERS

	private static PayStub calculateManagerPay(Employee emp) {
		double payRate = emp.getPayRate();
		double taxRate = emp.getTaxRate() / 100.0;

		double grossPay = payRate * 40;
		double taxes = grossPay * taxRate;
		double netPay = grossPay - taxes;

		return new PayStub(grossPay, taxes, netPay, 40, 0);
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

		for (int i = 0; i < 7; i++) {
			int dayHours = hours[i];
			boolean tookPTO = isPTO[i];

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

		return new PayStub(grossPay, taxes, netPay, totalWorked, totalPTO);
	}
}
