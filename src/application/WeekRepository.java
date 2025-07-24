package application;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class WeekRepository {
	private List<Week> records = new ArrayList<>();
	private final String filePath = "hours.txt";

	public WeekRepository() {
	        load();
	    }

	public void addRecord(Week record) {
		records.add(record);
		save();
	}

	public List<Week> getRecordsForEmployee(String employeeId) {
		return records.stream().filter(r -> r.getEmployeeId().equals(employeeId)).sorted(Comparator.comparingInt(Week::getWeekNumber)).collect(Collectors.toList());
	}

	public Week getWeek(String employeeId, int weekNum) {
		return records.stream().filter(r -> r.getEmployeeId().equals(employeeId) && r.getWeekNumber() == weekNum)
				.findFirst().orElse(null);
	}
	
//SAVE hours
	private void save() {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			for (Week r : records) {
				writer.println(r.toFileString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//LOAD hours
	private void load() {
		File file = new File(filePath);
		if (!file.exists())
			return;

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				records.add(Week.fromLine(scanner.nextLine()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
