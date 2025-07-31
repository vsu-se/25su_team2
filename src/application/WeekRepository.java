package application;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class WeekRepository {
	private List<Week> records = new ArrayList<>();
	private final String filePath = "hours.txt";
	private Map<String, Week> currentWeekMap = new HashMap<>();
	private final String currentMapPath = "current_week_map.txt";

	public WeekRepository() {
		load();
		loadCurrentWeekMap();

	}

	public void addRecord(Week record) {
		records.add(record);
		save();
	}

	public List<Week> getRecordsForEmployee(String employeeId) {
		return records.stream().filter(r -> r.getEmployeeId().equals(employeeId))
				.sorted(Comparator.comparingInt(Week::getWeekNumber)).collect(Collectors.toList());
	}

	public Week getWeek(String employeeId, int weekNum) {
		return records.stream().filter(r -> r.getEmployeeId().equals(employeeId) && r.getWeekNumber() == weekNum)
				.findFirst().orElse(null);
	}

	public Map<String, Week> getCurrentWeekMap() {
	    return currentWeekMap;
	}

	public void setCurrentWeek(String employeeId, Week week) {
	    currentWeekMap.put(employeeId, week);
	    saveCurrentWeekMap();
	}
//SAVE hours
	public void save() {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			for (Week r : records) {
				writer.println(r.toFileString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveCurrentWeekMap() {
	    try (PrintWriter writer = new PrintWriter(new FileWriter(currentMapPath))) {
	        for (Week w : currentWeekMap.values()) {
	            writer.println(w.toFileString());
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

	public void loadCurrentWeekMap() {
		File file = new File(currentMapPath);
		if (!file.exists())
			return;

		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				Week week = Week.fromLine(scanner.nextLine());
				currentWeekMap.put(week.getEmployeeId(), week);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// bulk hours via text files
	public List<String> loadBulkHours(File file) {
		List<String> messages = new ArrayList<>();
		List<String> successful = new ArrayList<>();
		List<String> failed = new ArrayList<>();
		Map<String, Integer> latestWeekMap = new HashMap<>();

		try (Scanner scanner = new Scanner(file)) {
			int lineNumber = 1;

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				String[] parts = line.split(",");

				if (parts.length != 15) {
					failed.add("Line " + lineNumber + ": Expected 15 values (ID + 7 hours + 7 PTOs)");
					lineNumber++;
					continue;
				}

				try {
					String employeeId = parts[0];

					// Get latest week number from memory or archive.
					int latestWeek = latestWeekMap.getOrDefault(employeeId,
							getRecordsForEmployee(employeeId).stream().mapToInt(Week::getWeekNumber).max().orElse(0));

					int nextWeek = latestWeek + 1;

					int[] hours = new int[7];
					for (int i = 0; i < 7; i++) {
						hours[i] = Integer.parseInt(parts[1 + i]);
					}

					boolean[] pto = new boolean[7];
					for (int i = 0; i < 7; i++) {
						pto[i] = Boolean.parseBoolean(parts[8 + i]);
					}

					Week week = new Week(employeeId, nextWeek, hours, pto);
					addRecord(week);
					latestWeekMap.put(employeeId, nextWeek);

					successful.add("Line " + lineNumber + ": Added as week " + nextWeek + " for " + employeeId);

				} catch (Exception e) {
					failed.add("Line " + lineNumber + ": Invalid data type or parse error");
				}

				lineNumber++;
			}
		} catch (Exception e) {
			failed.add("Failed to read file: " + e.getMessage());
		}

		if (!successful.isEmpty()) {
			messages.add("Successfully loaded:");
			messages.addAll(successful);
		}

		if (!failed.isEmpty()) {
			messages.add("\n Failed to load:");
			messages.addAll(failed);
			messages.add(""); // spacing
			messages.add("Expected Format:");
			messages.add("Employee ID, Hours (7), PTO (7)");
			messages.add("Example:");
			messages.add("0001,8,8,8,8,8,0,0,true,true,true,true,true,false,false");
		}

		return messages;
	}
}
