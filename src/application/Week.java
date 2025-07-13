package application;

public class Week {
	private String employeeId;
	private int weekNumber;
	private int[] hours;
	private boolean[] isPTO;

	public Week(String employeeId, int weekNumber, int[] hours, boolean[] isPTO) {
		this.employeeId = employeeId;
		this.weekNumber = weekNumber;
		this.hours = hours.clone();
		this.isPTO = isPTO.clone();
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public int getWeekNumber() {
		return weekNumber;
	}

	public int[] getHours() {
		return hours;
	}

	public boolean[] getIsPTO() {
		return isPTO;
	}

	public String toFileString() {
		StringBuilder sb = new StringBuilder();
		sb.append(employeeId).append(",").append(weekNumber).append(",");
		for (int h : hours) sb.append(h).append(",");
		for (int i = 0; i < isPTO.length; i++) {
			sb.append(isPTO[i]);
			if (i < isPTO.length - 1) sb.append(",");
		}
		return sb.toString();
	}

	public static Week fromLine(String line) {
		String[] parts = line.split(",");
		String id = parts[0];
		int week = Integer.parseInt(parts[1]);

		int[] hrs = new int[7];
		for (int i = 0; i < 7; i++) {
			hrs[i] = Integer.parseInt(parts[i + 2]);
		}

		boolean[] pto = new boolean[7];
		for (int i = 0; i < 7; i++) {
			pto[i] = Boolean.parseBoolean(parts[i + 9]);
		}

		return new Week(id, week, hrs, pto);
	}
}

