package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class Main extends Application {
	private final DataHandler handler = new DataHandler("employees.txt");
	private final Map<String, Week> currentWeekMap = new HashMap<>();
	private final WeekRepository weekRepo = new WeekRepository();
	private Employee loggedInUser = null;
	private Tab tabEmployeeMgmt;
	private Tab tabHoursEntry;
	private Tab tabPayrollReports;
	private Tab tabEmployeeAddHours;


	// GUI base structure
	// --------------------------------------------------------------------------------------
	protected TabPane tabPane = new TabPane();

	// Tab 1: Login
	// ----------------------------------------------------------------------------------------------------
	protected TextField txtLoginUsername = new TextField();
	protected PasswordField txtLoginPassword = new PasswordField();
	protected Button btnLogin = new Button("Login");
	protected Label lblLoginMessage = new Label();

	// Tab 2: Employee Management
	// -------------------------------------------------------------------------------------
	protected Label lblWelcomeMng = new Label();
	protected Button btnAddEmployee = new Button("Add Employee");
	protected Label lblAddEmpMessage = new Label();
	protected Button btnShowEmps = new Button("Show Employees");
	protected Button btnSearchByDept = new Button("Search");
	protected Button btnShowManags = new Button("Show Managers");
	protected ListView<String> listEmps = new ListView<>();
	protected ListView<String> listManags = new ListView<>();
	protected TextField txtFFirstName = new TextField();
	protected TextField txtFLastName = new TextField();
	protected TextField txtFUsername = new TextField();
	protected PasswordField txtFPassword = new PasswordField();
	protected TextField txtFDepartment = new TextField();
	protected TextField txtFPayRate = new TextField();
	protected TextField txtFTaxRate = new TextField();
	protected TextField txtFPTO = new TextField();
	protected ComboBox<String> cmbRole = new ComboBox<>();
	protected TextArea txaEmpMessage = new TextArea();
	protected TextArea txaManagsMessage = new TextArea();

	// Tab 3: Hours Entry, both Employee and Manager
	// -------------------------------------------------------------------
	protected ComboBox<String> EmpSelected = new ComboBox<>();
	protected TextField[] txtHoursPerDay = new TextField[7];
	protected CheckBox[] PTOPerDay = new CheckBox[7];
	protected Button btnSubmitHours = new Button("Submit Hours");
	protected Button btnViewCurrentWeek = new Button("View Current Week");
	protected Button btnViewArchiveWeek = new Button("Archive Week");
	protected TextArea txaHoursMessage = new TextArea();
	protected Label lblAllEmployees = new Label("All Employees in Hours in Current Week");
	protected Button btnDisplayAllCurrentWeek = new Button("Display");
	protected TextArea txaAllEmployeesCurrentWeek = new TextArea();
	// Tab 4: Payroll Reports, both Employee and Manager
	// -------------------------------------------------------------
	protected ComboBox<String> cmbReportEmployee = new ComboBox<>();
	protected ComboBox<String> cmbReportWeek = new ComboBox<>();
	protected Button btnViewReport = new Button("View Report");
	protected TextArea txaReport = new TextArea();
	protected RadioButton rbCurrent = new RadioButton("Current Week");
	protected RadioButton rbAll = new RadioButton("All Weeks");
	protected RadioButton rbRange = new RadioButton("Select Range");
	protected ToggleGroup reportGroup = new ToggleGroup();
	protected String selectedRangeStart = null;
	protected String selectedRangeEnd = null;

	// Tab 4: Employee Hours Entry for Staff Employees
	//------------------------------------------------------------------
	protected Label lblEmployeeHoursHeader = new Label();
	protected TextField[] txtEmployeeHoursPerDay = new TextField[7];
	protected CheckBox[] ptoEmployeePerDay = new CheckBox[7];
	protected Button btnEmployeeSubmitHours = new Button("Submit Hours");
	protected TextArea txaEmployeeHoursMessage = new TextArea();


	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = buildGui();
			Scene scene = new Scene(root, 980, 700);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Payroll System");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Pane buildGui() {
		BorderPane brdPane = new BorderPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

		// Create
		// Tabs---------------------------------------------------------------------------
		Tab tabLogin = new Tab("Login", buildLoginTab());
		tabEmployeeMgmt = new Tab("Employee Management", buildEmployeeManagementTab());
		tabHoursEntry = new Tab("Hours Entry", buildHoursEntryTab());
		tabPayrollReports = new Tab("Payroll Reports", buildPayrollReportsTab());
		tabEmployeeAddHours = new Tab("Add My Hours", buildHoursEntryEmployeeTab());
		tabEmployeeAddHours.setClosable(false);


		//Only login tab
		// --------------------------------------------------

		tabPane.getTabs().add(tabLogin);

		brdPane.setCenter(tabPane);
		return brdPane;
	}

// ----- Log in Tab --------------------------
	private Pane buildLoginTab() {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(20));
		grid.setVgap(10);
		grid.setHgap(10);

		grid.add(new Label("Username:"), 0, 0);
		grid.add(txtLoginUsername, 1, 0);
		grid.add(new Label("Password:"), 0, 1);
		grid.add(txtLoginPassword, 1, 1);
		grid.add(btnLogin, 1, 2);
		grid.add(lblLoginMessage, 1, 3);

		btnLogin.setOnAction(e -> {
			String inputUsername = txtLoginUsername.getText().trim();
			String inputPassword = txtLoginPassword.getText().trim();

			boolean found = false;

			tabPane.getTabs().clear();

			for (Manager m : handler.getManagers()) {
				if (m.getUsername().equalsIgnoreCase(inputUsername)) {
					if (m.authenticate(inputPassword)) {
						lblLoginMessage.setText("Welcome Manager: " + m.getFullName());
						lblLoginMessage.setStyle("-fx-text-fill: green;");
						loggedInUser = m;

						tabPane.getTabs().clear();

						tabPane.getTabs().addAll(
								new Tab("Login", buildLoginTab()),
								tabEmployeeMgmt,
								tabHoursEntry,
								tabPayrollReports
						);

						tabPane.getSelectionModel().select(tabEmployeeMgmt);

						found = true;
						break;
					} else {
						lblLoginMessage.setText("Incorrect password.");
						lblLoginMessage.setStyle("-fx-text-fill: red;");
						tabPane.getTabs().add(new Tab("Login", buildLoginTab()));
						return;
					}
				}
			}

			if (!found) {
				for (Staff s : handler.getStaff()) {
					if (s.getUsername().equalsIgnoreCase(inputUsername)) {
						if (s.authenticate(inputPassword)) {
							lblLoginMessage.setText("Welcome Staff: " + s.getFullName());
							lblLoginMessage.setStyle("-fx-text-fill: green;");
							loggedInUser = s;

							tabPane.getTabs().clear();


							Tab tabLogin = new Tab("Login", buildLoginTab());
							tabLogin.setClosable(false);

							Tab tabStaffHoursEntry = new Tab("Add My Hours", buildHoursEntryEmployeeTab());
							tabStaffHoursEntry.setClosable(false);

							tabPane.getTabs().addAll(
									tabLogin,
									tabStaffHoursEntry
							);

							tabPane.getSelectionModel().select(tabStaffHoursEntry);

							found = true;
							break;
						} else {
							lblLoginMessage.setText(" Incorrect password.");
							lblLoginMessage.setStyle("-fx-text-fill: red;");
							tabEmployeeMgmt.setDisable(true);
							tabHoursEntry.setDisable(true);
							tabPayrollReports.setDisable(true);
							return;
						}
					}
				}
			}

			if (!found) {
				lblLoginMessage.setText("Username not found.");
				lblLoginMessage.setStyle("-fx-text-fill: red;");
				tabEmployeeMgmt.setDisable(true);
				tabHoursEntry.setDisable(true);
				tabPayrollReports.setDisable(true);
			}
		});

		return grid;
	}

// ----- Employee Management Tab ---------------------------
	private Pane buildEmployeeManagementTab() {
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(10));
		gp.setVgap(10);
		gp.setHgap(10);

		cmbRole.getItems().addAll("Staff", "Manager");

		gp.add(new Label("First Name:"), 0, 0);
		gp.add(txtFFirstName, 1, 0);
		gp.add(new Label("Last Name:"), 0, 1);
		gp.add(txtFLastName, 1, 1);
		gp.add(new Label("Username:"), 0, 2);
		gp.add(txtFUsername, 1, 2);
		gp.add(new Label("Password:"), 0, 3);
		gp.add(txtFPassword, 1, 3);
		gp.add(new Label("Department:"), 0, 4);
		gp.add(txtFDepartment, 1, 4);
		gp.add(new Label("Pay Rate ($/hr):"), 0, 5);
		gp.add(txtFPayRate, 1, 5);
		gp.add(new Label("Tax Rate (%):"), 0, 6);
		gp.add(txtFTaxRate, 1, 6);
		gp.add(new Label("PTO Days:"), 0, 7);
		gp.add(txtFPTO, 1, 7);
		gp.add(new Label("Role:"), 0, 8);
		gp.add(cmbRole, 1, 8);
		gp.add(btnAddEmployee, 1, 9);
		gp.add(lblAddEmpMessage, 1, 10);

		Label lblSearchByDept = new Label("Search by Department:");
		ToggleGroup deptGroup = new ToggleGroup();
		RadioButton rbSales = new RadioButton("Sales");
		RadioButton rbHR = new RadioButton("HR");
		RadioButton rbIT = new RadioButton("IT");
		RadioButton rbFinance = new RadioButton("Finance");
		RadioButton rbMarketing = new RadioButton("Marketing");

		rbSales.setToggleGroup(deptGroup);
		rbHR.setToggleGroup(deptGroup);
		rbIT.setToggleGroup(deptGroup);
		rbFinance.setToggleGroup(deptGroup);
		rbMarketing.setToggleGroup(deptGroup);

		VBox vboxDept = new VBox(5, lblSearchByDept, rbSales, rbHR, rbIT, rbFinance, rbMarketing, btnSearchByDept);
		vboxDept.setPadding(new Insets(10));

		HBox hboxTop = new HBox(30, btnShowEmps, btnShowManags, vboxDept);

		VBox vboxEmps = new VBox(10, new Label("Employees:"), hboxTop, listEmps);
		vboxEmps.setPadding(new Insets(10));
		vboxEmps.setPrefWidth(500);

		VBox vboxManag = new VBox(10, new Label("Managers:"), btnShowManags, listManags);
		vboxManag.setPadding(new Insets(10));
		vboxManag.setPrefWidth(500);

		btnAddEmployee.setOnAction(event -> {
			if (loggedInUser == null || !(loggedInUser instanceof Manager)) {
				lblAddEmpMessage.setText("Access denied: Only managers can add employees.");
				lblAddEmpMessage.setStyle("-fx-text-fill: red;");
				return;
			}
			try {
				String firstName = txtFFirstName.getText().trim();
				String lastName = txtFLastName.getText().trim();
				String username = txtFUsername.getText().trim();
				String password = txtFPassword.getText().trim();
				String department = txtFDepartment.getText().trim();
				double payRate = Double.parseDouble(txtFPayRate.getText().trim());
				double taxRate = Double.parseDouble(txtFTaxRate.getText().trim());
				int ptoDays = Integer.parseInt(txtFPTO.getText().trim());
				String role = cmbRole.getValue();

				Employee newEmp;
				if ("Manager".equalsIgnoreCase(role)) {
					newEmp = new Manager(firstName, lastName, username, password, department, payRate, taxRate,
							ptoDays);
				} else {
					newEmp = new Staff(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
				}

				boolean added = handler.addEmployee(newEmp);

				if (added) {
					// Updates both mangers and staff employees
					listEmps.getItems().clear();
					for (Manager m : handler.getManagers()) {
						listEmps.getItems().add(m.getFullName() + " (Manager)");
					}
					for (Staff s : handler.getStaff()) {
						listEmps.getItems().add(s.getFullName() + " (Staff)");
					}

					// Update only managers employees
					listManags.getItems().clear();
					for (Manager m : handler.getManagers()) {
						listManags.getItems().add(m.getFullName() + " (Manager)");
					}

					handler.saveToFile("employees.txt");

					lblAddEmpMessage.setText("Employee added: " + newEmp.getFullName() + " (" + role + ")");
					lblAddEmpMessage.setStyle("-fx-text-fill: green;");
				} else {
					lblAddEmpMessage.setText("Username already exists. Choose another one.");
					lblAddEmpMessage.setStyle("-fx-text-fill: red;");
				}
			} catch (Exception ex) {
				lblAddEmpMessage.setText("Error adding employee: " + ex.getMessage());
				lblAddEmpMessage.setStyle("-fx-text-fill: red;");
			}
		});

		btnShowEmps.setOnAction(e -> {
			listEmps.getItems().clear();
			for (Employee emp : getSortedEmployees()) {
				listEmps.getItems().add(formatEmployeeDisplay(emp));
			}
		});

		btnSearchByDept.setOnAction(e -> {
			listEmps.getItems().clear();
			Toggle selectedToggle = deptGroup.getSelectedToggle();
			if (selectedToggle != null) {
				RadioButton selectedRadio = (RadioButton) selectedToggle;
				String department = selectedRadio.getText();
				for (Employee emp : handler.getEmployeesInDepartment(department)) {
					listEmps.getItems().add(formatEmployeeDisplay(emp));
				}
			} else {
				listEmps.getItems().add("Please select a department.");
			}
		});

		btnShowManags.setOnAction(e -> {
			listManags.getItems().clear();
			List<Manager> sortedManagers = new ArrayList<>(handler.getManagers());
			sortedManagers.sort((m1, m2) -> {
				int cmp = m1.getLastName().compareToIgnoreCase(m2.getLastName());
				if (cmp != 0)
					return cmp;
				cmp = m1.getFirstName().compareToIgnoreCase(m2.getFirstName());
				if (cmp != 0)
					return cmp;
				cmp = m1.getDepartment().compareToIgnoreCase(m2.getDepartment());
				if (cmp != 0)
					return cmp;
				return m1.getEmployeeID().compareTo(m2.getEmployeeID());
			});
			for (Manager m : sortedManagers) {
				listManags.getItems().add(formatEmployeeDisplay(m));
			}
		});

		return new HBox(20, gp, vboxEmps, vboxManag);

	}

// ----- Hours Entry Tab ----------------------------------------
	private Pane buildHoursEntryTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));

		vbox.getChildren().add(new Label("Select Employee:"));
		vbox.getChildren().add(EmpSelected);

		GridPane daysGrid = new GridPane();
		daysGrid.setHgap(10);
		daysGrid.setVgap(10);

		String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
		for (int i = 0; i < 7; i++) {
			daysGrid.add(new Label(days[i]), 0, i);

			TextField txtHours = new TextField();
			txtHours.setPromptText("Hours");
			txtHoursPerDay[i] = txtHours;
			daysGrid.add(txtHours, 1, i);

			CheckBox chkPTO = new CheckBox("PTO");
			PTOPerDay[i] = chkPTO;

			if (i >= 5) {
				chkPTO.setDisable(true);
			}

			daysGrid.add(chkPTO, 2, i);
		}

		vbox.getChildren().add(daysGrid);
		HBox buttonRow = new HBox(10);
		buttonRow.getChildren().addAll(btnSubmitHours, btnViewCurrentWeek, btnViewArchiveWeek);
		vbox.getChildren().add(buttonRow);
		vbox.getChildren().add(txaHoursMessage);

		txaAllEmployeesCurrentWeek.setEditable(false);
		txaAllEmployeesCurrentWeek.setPrefHeight(350);
		txaAllEmployeesCurrentWeek.setPrefWidth(400);

		VBox vboxAll = new VBox(10);
		vboxAll.setPadding(new Insets(0, 0, 0, 10));
		vboxAll.getChildren().addAll(lblAllEmployees, btnDisplayAllCurrentWeek, txaAllEmployeesCurrentWeek);

		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(10));
		hbox.getChildren().addAll(vbox, vboxAll);

		// Populate employee ComboBox
		EmpSelected.getItems().clear();
		for (Manager m : handler.getManagers()) {
			EmpSelected.getItems().add(m.getUsername());
		}
		for (Staff s : handler.getStaff()) {
			EmpSelected.getItems().add(s.getUsername());
		}

		// SubmitHours Button - used to store current week hours in a hash map.
		btnSubmitHours.setOnAction(e -> {
			String selectedUsername = EmpSelected.getValue();
			if (selectedUsername == null) {
				txaHoursMessage.setText("Please select an employee.");
				return;
			}

			Employee emp = handler.findEmployeeByUsername(selectedUsername);
			if (emp == null) {
				txaHoursMessage.setText("Employee not found.");
				return;
			}

			try {
				// add current week to currentWeekMap (EmployeeID:Week obj) if key has no value
				// construct the week obj
				String empID = emp.getEmployeeID();
				Week week = currentWeekMap.get(emp.getEmployeeID());
				if (week == null) {
					int nextWeekNum = weekRepo.getRecordsForEmployee(empID).size() + 1;
					week = new Week(empID, nextWeekNum, new int[7], new boolean[7]);
				}

				// fill the array within week obj with valid hours and PTO booleans
				int[] hours = new int[7];
				boolean[] pto = new boolean[7];

				for (int i = 0; i < 7; i++) {
					String input = txtHoursPerDay[i].getText().trim();
					hours[i] = input.isEmpty() ? 0 : Integer.parseInt(input);
					if (hours[i] < 0 || hours[i] > 24) {
						throw new NumberFormatException("Hours must be between 0 and 24 for day " + (i + 1));
					}
					pto[i] = PTOPerDay[i].isSelected();
				}
				// add the values into the week obj and store in the HashMap
				week.setHours(hours);
				week.setIsPTO(pto);
				currentWeekMap.put(empID, week);

				// validate success for user
				txaHoursMessage.setText("Hours recorded for " + emp.getFullName());
				txaHoursMessage.setStyle("-fx-text-fill: green;");

			} catch (NumberFormatException ex) {
				txaHoursMessage.setText("Invalid input: " + ex.getMessage());
				txaHoursMessage.setStyle("-fx-text-fill: red;");
			}

		});

		// Current Week Button - view current week for employee
		btnViewCurrentWeek.setOnAction(e -> {
			String selectedUsername = EmpSelected.getValue();
			if (selectedUsername == null) {
				txaHoursMessage.setText("Please select an employee.");
				return;
			}

			Employee emp = handler.findEmployeeByUsername(selectedUsername);
			if (emp == null) {
				txaHoursMessage.setText("Employee not found.");
				return;
			}
			// use hashMap (EmployeeID:week) to show the week;
			Week week = currentWeekMap.get(emp.getEmployeeID());
			if (week == null) {
				txaHoursMessage.setText("No current week in progress.");
			} else {
				displayCurrentWeek(emp, week);
			}
		});

		// Archive Button - store to hours.txt via WeekReposoitory class
		btnViewArchiveWeek.setOnAction(e -> {
			String selectedUsername = EmpSelected.getValue();
			if (selectedUsername == null) {
				txaHoursMessage.setText("Please select an employee.");
				return;
			}

			Employee emp = handler.findEmployeeByUsername(selectedUsername);
			if (emp == null) {
				txaHoursMessage.setText("Employee not found.");
				return;
			}

			String empID = emp.getEmployeeID();
			Week week = currentWeekMap.get(empID);

			if (week == null) {
				txaHoursMessage.setText("No current week to archive.");
				return;
			}

			weekRepo.addRecord(week);
			currentWeekMap.remove(empID);

			txaHoursMessage.setText("Week archived for " + emp.getFullName() + " (Week #" + week.getWeekNumber() + ")");
			txaHoursMessage.setStyle("-fx-text-fill: blue;");
		});

		btnDisplayAllCurrentWeek.setOnAction(e -> {
			List<Employee> allEmployees = getSortedEmployees();
			StringBuilder sb = new StringBuilder();
			String[] daysArr = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

			for (Employee emp : allEmployees) {
				sb.append(formatEmployeeHours(emp)).append("\n\n");
				Week week = currentWeekMap.get(emp.getEmployeeID());
				if (week == null) {
					sb.append("  No current week data.\n\n");
					continue;
				}
				int[] hours = week.getHours();
				boolean[] pto = week.getIsPTO();
				int totalHours = 0;
				for (int i = 0; i < 7; i++) {
					sb.append("  ").append(daysArr[i]).append(": ").append(hours[i]);
					if (pto[i])
						sb.append(" (PTO)");
					sb.append("\n");
					totalHours += hours[i];
				}
				sb.append("  Total Hours: ").append(totalHours).append("\n\n");
			}
			txaAllEmployeesCurrentWeek.setText(sb.toString());
		});

		return hbox;
	}

// ----- Payroll Reports Tab ----------------------------------
	private Pane buildPayrollReportsTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));
		
		//select employee drop down
		vbox.getChildren().add(new Label("Select Employee:"));
		vbox.getChildren().add(cmbReportEmployee);
		
		//radio buttons 
		rbCurrent.setToggleGroup(reportGroup);
		rbAll.setToggleGroup(reportGroup);
		rbRange.setToggleGroup(reportGroup);
		rbCurrent.setSelected(true);
		HBox radioBox = new HBox(10, rbCurrent, rbAll, rbRange);
		vbox.getChildren().add(radioBox);
		
		//view report button and text area
		vbox.getChildren().add(btnViewReport);
		vbox.getChildren().add(txaReport);

		//load the current items
		cmbReportEmployee.getItems().clear();
		for (Manager m : handler.getManagers()) {
		    cmbReportEmployee.getItems().add(m.getUsername());
		}
		for (Staff s : handler.getStaff()) {
		    cmbReportEmployee.getItems().add(s.getUsername());
		}
		
		//Radio for Week range selection-----------------------------------------------------------------------
		rbRange.setOnMouseClicked(e -> {
		    // Allow button to reopen pop if already selected vs having to click on and off to refresh.
		    if (rbRange.isSelected()) {
		        String username = cmbReportEmployee.getValue();
		        if (username == null) {
		            txaReport.setText("Please select an employee first.");
		            reportGroup.selectToggle(rbCurrent);
		            return;
		        }
		        
		        //load selected employee week repo
		        Employee emp = handler.findEmployeeByUsername(username);
		        List<Week> weeks = weekRepo.getRecordsForEmployee(emp.getEmployeeID());

		        if (weeks.isEmpty()) {
		            txaReport.setText("No archived weeks available.");
		            reportGroup.selectToggle(rbCurrent);
		            return;
		        }
		        
		        //Stage pop up (implemented to prevent clutter on GUI)---------------------------------------------
		        Stage popup = new Stage();
		        popup.setTitle("Select Week Range");

		        VBox popupBox = new VBox(10);
		        popupBox.setPadding(new Insets(10));

		        ComboBox<String> cmbStart = new ComboBox<>();
		        ComboBox<String> cmbEnd = new ComboBox<>();
		        
		        //list available week for the selected employee
		        for (Week w : weeks) {
		            String label = "Week #" + w.getWeekNumber();
		            cmbStart.getItems().add(label);
		            cmbEnd.getItems().add(label);
		        }
		        
		        //confirm selection button and init two variable to hold the selected values
		        Button btnConfirm = new Button("Confirm");
		        btnConfirm.setOnAction(ev -> {
		            selectedRangeStart = cmbStart.getValue();
		            selectedRangeEnd = cmbEnd.getValue();
		            popup.close();
		        });
		        
		        //SHOW pop up -----------------------------------------------------------------------------------------
		        popupBox.getChildren().addAll(new Label("From:"), cmbStart, new Label("To:"), cmbEnd, btnConfirm);
		        popup.setScene(new Scene(popupBox, 250, 200));
		        popup.show();
		    }
		});
		

	    // View report button logic
		btnViewReport.setOnAction(e -> {
		    String username = cmbReportEmployee.getValue();
		    if (username == null) {
		        txaReport.setText("Please select an employee.");
		        return;
		    }

		    Employee emp = handler.findEmployeeByUsername(username);
		    List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
		    List<Week> result = new ArrayList<>();

		    if (rbCurrent.isSelected()) {
		        Week curr = currentWeekMap.get(emp.getEmployeeID());
		        if (curr != null) result.add(curr);
		    } else if (rbAll.isSelected()) {
		        result.addAll(history);
		    } else if (rbRange.isSelected()) {
		        if (selectedRangeStart == null || selectedRangeEnd == null) {
		            txaReport.setText("Please select a valid week range.");
		            return;
		        }

		        int start = Integer.parseInt(selectedRangeStart.replace("Week #", ""));
		        int end = Integer.parseInt(selectedRangeEnd.replace("Week #", ""));
		        if (start > end) {
		            txaReport.setText("Invalid range: start > end.");
		            return;
		        }

		        for (Week w : history) {
		            int num = w.getWeekNumber();
		            if (num >= start && num <= end) {
		                result.add(w);
		            }
		        }
		    }

		    if (result.isEmpty()) {
		        txaReport.setText("No week data found.");
		        return;
		    }
		    
		    List<PayRollCalculator.PayStub> stubs = new ArrayList<>();
	        StringBuilder sb = new StringBuilder();

	        for (Week w : result) {
	            PayRollCalculator.PayStub stub = PayRollCalculator.calculatePay(emp, w);
	            stubs.add(stub);
	            sb.append("Week #").append(w.getWeekNumber()).append("\n");
	            sb.append(stub.toString()).append("\n\n");
	        }

	        if (stubs.size() > 1) {
	            double totalGross = 0, totalTax = 0, totalNet = 0;
	            int totalHours = 0, totalPTO = 0;

	            for (PayRollCalculator.PayStub stub : stubs) {
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

	        txaReport.setText(sb.toString());
	    });

	    cmbReportEmployee.setOnAction(e -> {
	        selectedRangeStart = null;
	        selectedRangeEnd = null;
	        txaReport.clear();
	        reportGroup.selectToggle(rbCurrent);
	        txaReport.setText("Select a view option and click 'View Report'.");
	    });
	    
	    return vbox;
	}
	
// -----Employees Hour Entry Tab -----------------------------------------------------
	private Pane buildHoursEntryEmployeeTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));

		if (loggedInUser != null) {
			lblEmployeeHoursHeader.setText("Employee: " + loggedInUser.getFullNameNoUser() +
					" | ID: " + loggedInUser.getEmployeeID());
		}
		else {
			lblEmployeeHoursHeader.setText("No user logged in.");
		}
		lblEmployeeHoursHeader.setStyle("-fx-font-size: 14pt; -fx-font-weight: bold;");
		vbox.getChildren().add(lblEmployeeHoursHeader);

		GridPane daysGrid = new GridPane();
		daysGrid.setHgap(10);
		daysGrid.setVgap(10);

		String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
		for (int i = 0; i < 7; i++) {
			daysGrid.add(new Label(days[i] + ":"), 0, i);

			if (txtEmployeeHoursPerDay[i] == null) {
				txtEmployeeHoursPerDay[i] = new TextField();
			}
			txtEmployeeHoursPerDay[i].setPromptText("Hours");
			daysGrid.add(txtEmployeeHoursPerDay[i], 1, i);

			if (ptoEmployeePerDay[i] == null)
				ptoEmployeePerDay[i] = new CheckBox("PTO");

			// TPO click boxes
			if (i >= 5) ptoEmployeePerDay[i].setDisable(true);
			daysGrid.add(ptoEmployeePerDay[i], 2, i);
		}
		vbox.getChildren().add(daysGrid);
		vbox.getChildren().add(btnEmployeeSubmitHours);

		// Message label and button reset
		txaEmployeeHoursMessage.setEditable(false);
		txaEmployeeHoursMessage.setWrapText(true);
		txaEmployeeHoursMessage.setPrefRowCount(4);
		txaEmployeeHoursMessage.setPrefHeight(120);

		vbox.getChildren().add(txaEmployeeHoursMessage);

		//Submit hours for employee
		btnEmployeeSubmitHours.setOnAction(e -> {
			if (!(loggedInUser instanceof Staff)) {
				txaEmployeeHoursMessage.setText("Only staff employees can use this form.");
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: red;");
				return;
			}

			String empID = loggedInUser.getEmployeeID();
			Week existingWeek = currentWeekMap.get(empID);

			if (existingWeek != null) {
				txaEmployeeHoursMessage.setText("You already submitted hours for this week.");
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: red;");
				return;
			}

			try {
				int[] hoursArray = new int[7];
				boolean[] ptoArray = new boolean[7];

				for (int i = 0; i < 7; i++) {
					String input = txtEmployeeHoursPerDay[i].getText().trim();
					int hours = (input.isEmpty()) ? 0 : Integer.parseInt(input);

					if (hours < 0 || hours > 24)
						throw new NumberFormatException("Invalid hours for " + days[i]);

					hoursArray[i] = hours;
					ptoArray[i] = ptoEmployeePerDay[i].isSelected();
				}

				int weekNumber = weekRepo.getRecordsForEmployee(empID).size() + 1;
				Week newWeek = new Week(empID, weekNumber, hoursArray, ptoArray);
				currentWeekMap.put(empID, newWeek);

				txaEmployeeHoursMessage.setText("Hours submitted successfully for this week.");
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: green;");
			} catch (NumberFormatException ex) {
				txaEmployeeHoursMessage.setText("Error: " + ex.getMessage());
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: red;");
			}
		});

		return vbox;
	}


//Helpers to format info to be displayed as the stories required, I kinda came up with a way to make it look organized.-----------
	private void displayCurrentWeek(Employee emp, Week week) {
		String[] days = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		StringBuilder sb = new StringBuilder("Current Week: " + emp.getFullName() + "\n");

		int totalHours = 0;

		for (int i = 0; i < 7; i++) {
			int hrs = week.getHours()[i];
			boolean isPTO = week.getIsPTO()[i];
			totalHours += hrs;
			sb.append(String.format("%s: %d hrs %s\n", days[i], hrs, isPTO ? "(PTO)" : ""));
		}

		sb.append("\nTotal Hours: ").append(totalHours);

		txaHoursMessage.setText(sb.toString());
	}
	
	private String formatEmployeeHours(Employee emp) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(" | Name: ").append(emp.getLastName()).append(", ").append(emp.getFirstName());
	    sb.append("| Department: ").append(emp.getDepartment());
	    sb.append("| ID: ").append(emp.getEmployeeID());
	    if (emp instanceof Manager) {
	       sb.insert(0, ("MANAGER | "));
	    }
	    else{
	       sb.insert(0,("STAFF | "));
	    }
	    return sb.toString();
	}

	private String formatEmployeeDisplay(Employee emp) {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(emp.getEmployeeID());
		sb.append(" | Name: ").append(emp.getLastName()).append(", ").append(emp.getFirstName());
		sb.append(" | Username: ").append(emp.getUsername());
		sb.append(" | Department: ").append(emp.getDepartment());
		sb.append(" | Pay Rate: $").append(String.format("%.2f", emp.getPayRate()));
		sb.append(" | Tax Rate: ").append(String.format("%.2f", emp.getTaxRate())).append("%");
		sb.append(" | PTO: ").append(emp.getPtoDays()).append(" days");
		if (emp instanceof Manager) {
			sb.insert(0, ("MANAGER | "));
		} else {
			sb.insert(0, ("STAFF | "));
		}
		return sb.toString();
	}

	private String formatEmployeeByDep(Employee emp) {
		StringBuilder sb = new StringBuilder();
		sb.append(" | Name: ").append(emp.getLastName()).append(", ").append(emp.getFirstName());
		sb.append("| ID: ").append(emp.getEmployeeID());
		sb.append("| Department: ").append(emp.getDepartment());
		if (emp instanceof Manager) {
			sb.insert(0, ("MANAGER | "));
		} else {
			sb.insert(0, ("STAFF | "));
		}
		return sb.toString();
	}

	private List<Employee> getSortedEmployees() {
		List<Employee> allEmployees = new ArrayList<>();
		allEmployees.addAll(handler.getManagers());
		allEmployees.addAll(handler.getStaff());
		allEmployees.sort((e1, e2) -> {
			int cmp = e1.getLastName().compareToIgnoreCase(e2.getLastName());
			if (cmp != 0)
				return cmp;
			cmp = e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
			if (cmp != 0)
				return cmp;
			cmp = e1.getDepartment().compareToIgnoreCase(e2.getDepartment());
			if (cmp != 0)
				return cmp;
			return e1.getEmployeeID().compareTo(e2.getEmployeeID());
		});
		return allEmployees;
	}

	public static void main(String[] args) {
		System.out.println("");

		launch(args);
	}
}
