package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;

import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.*;

public class Main extends Application {
	private final DataHandler handler = new DataHandler("employees.txt");
	private final WeekRepository weekRepo = new WeekRepository(); 
	private Employee loggedInUser = null;
	private Tab tabLogin;
	private Tab tabEmployeeMgmt;
	private Tab tabHoursEntry;
	private Tab tabPayrollReports;
	private Tab tabEmployeeAddHours;
	private Tab tabAdminTools;
	private final Button btnLogout = new Button("Log out");

	// GUI base structure
	// --------------------------------------------------------------------------------------
	protected TabPane tabPane = new TabPane();

	// Tab 1: Login
	// ----------------------------------------------------------------------------------------------------
	protected TextField txtLoginUsername = new TextField();
	protected PasswordField txtLoginPassword = new PasswordField();
	protected Button btnLogin = new Button("Login");
	protected Button btnChangePassword = new Button("Change Password");

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
	protected Button btnLoadBulkHours = new Button("Load Bulk Hours");
	// Tab 4: Payroll Reports, both Employee and Manager
	// -------------------------------------------------------------
	protected ComboBox<String> cmbReportEmployee = new ComboBox<>();
	protected ComboBox<String> cmbReportWeek = new ComboBox<>();
	protected Button btnViewReport = new Button("View Report");
	protected Button btnSaveReport = new Button("Save Report");
	protected TextArea txaReport = new TextArea();
	protected RadioButton rbCurrent = new RadioButton("Current Week");
	protected RadioButton rbAll = new RadioButton("All Weeks");
	protected RadioButton rbRange = new RadioButton("Select Range");
	protected ToggleGroup reportGroup = new ToggleGroup();
	protected String selectedRangeStart = null;
	protected String selectedRangeEnd = null;

	// Tab 4: Employee Hours Entry for Staff Employees
	// ------------------------------------------------------------------
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

		// Log out button in the top bar
		HBox topBar = new HBox();
		topBar.setPadding(new Insets(5));
		topBar.setSpacing(10);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		btnLogout.setVisible(false);
		topBar.getChildren().addAll(spacer, btnLogout);

		brdPane.setTop(topBar);

		// Create login tab
		tabLogin = new Tab("Login", buildLoginTab());
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
		grid.add(btnChangePassword, 1, 3);

		// Login Button Action
		btnLogin.setOnAction(e -> {
		    String inputUsername = txtLoginUsername.getText().trim();
		    String inputPassword = txtLoginPassword.getText().trim();

		    Employee emp = handler.findEmployeeByUsername(inputUsername);

		    if (emp == null) {
		        showError("Username not found.");
		        return;
		    }

		    if (!emp.authenticate(inputPassword)) {
		        showError("Incorrect password.");
		        return;
		    }

		    tabPane.getTabs().clear();
		    tabPane.getTabs().add(tabLogin);
		    loggedInUser = emp;

		    if (emp instanceof Manager) {
		        Node mgmt = buildEmployeeManagementTab();
		        Node hours = buildHoursEntryTab();
		        Node report = buildPayrollReportsTab();
		        Node admin = buildAdminToolsTab();

		        clearAllFields(mgmt);
		        clearAllFields(hours);
		        clearAllFields(report);
		        clearAllFields(admin);

		        tabEmployeeMgmt = new Tab("Employee Management", mgmt);
		        tabHoursEntry = new Tab("Hours Entry", hours);
		        tabPayrollReports = new Tab("Payroll Reports", report);
		        tabAdminTools = new Tab("Admin Tools", admin);

		        tabPane.getTabs().addAll(tabEmployeeMgmt, tabHoursEntry, tabPayrollReports, tabAdminTools);
		        tabPane.getSelectionModel().select(tabEmployeeMgmt);

		    } else if (emp instanceof Staff) {
		        Node empHours = buildHoursEntryEmployeeTab();
		        Node report = buildPayrollReportsTab();

		        clearAllFields(empHours);
		        clearAllFields(report);

		        tabEmployeeAddHours = new Tab("Add My Hours", empHours);
		        tabPayrollReports = new Tab("Payroll Reports", report);

		        tabPane.getTabs().addAll(tabEmployeeAddHours, tabPayrollReports);
		        tabPane.getSelectionModel().select(tabEmployeeAddHours);
		    }

		    btnLogout.setVisible(true);
		});

		// Log Out button
		btnLogout.setOnAction(e -> {
			weekRepo.saveCurrentWeekMap();
			tabPane.getTabs().clear();
			tabPane.getTabs().add(tabLogin);
			tabPane.getSelectionModel().select(tabLogin);
			loggedInUser = null;

			// Clear login text fields
			txtLoginUsername.clear();
			txtLoginPassword.clear();
			txaReport.clear();

			btnLogout.setVisible(false);
		});

		// ChangePassword Button Action-
		btnChangePassword.setOnAction(e -> {
			// Stage pop up 1 (username entry)
			Stage popup1 = new Stage();
			popup1.setTitle("Enter username");

			VBox popupBox1 = new VBox(10);
			popupBox1.setPadding(new Insets(10));
			TextField txtUsername = new TextField();
			Button btnConfirm1 = new Button("Confirm");

			// Stage pop up 2 (old and new password entry)
			Stage popup2 = new Stage();
			popup2.setTitle("Enter Old and New Password");

			VBox popupBox2 = new VBox(10);
			popupBox2.setPadding(new Insets(10));

			PasswordField txtOldPassword = new PasswordField();
			PasswordField txtNewPassword = new PasswordField();
			Button btnConfirm2 = new Button("Confirm");

			// show popup1
			popupBox1.getChildren().addAll(new Label("Username:"), txtUsername, btnConfirm1);
			popup1.setScene(new Scene(popupBox1, 250, 200));
			popup1.show();

			// Confirm1 button action
			btnConfirm1.setOnAction(ev -> {
				String username = txtUsername.getText();
				Employee emp = handler.findEmployeeByUsername(username);

				// check if username is valid
				if (username == null || username.isBlank() || emp == null) {
					showError("Please enter a valid username.");
					return;
				}

				// if checks pass close popup1 and open popup2
				popup1.close();
				popupBox2.getChildren().addAll(new Label("Old Password:"), txtOldPassword, new Label("New Password"),
						txtNewPassword, btnConfirm2);
				popup2.setScene(new Scene(popupBox2, 250, 200));
				popup2.show();
			});

			// Confirm2 button action
			btnConfirm2.setOnAction(event -> {
				String username = txtUsername.getText();
				String oldPassword = txtOldPassword.getText();
				String newPassword = txtNewPassword.getText();
				Employee emp = handler.findEmployeeByUsername(username);

				// check for valid old and new password. if valid save to employee.txt and close
				// popup2
				if (newPassword == null || newPassword.isBlank()) {
					showError("New password cannot be empty.");
				} else if (!emp.changePassword(oldPassword, newPassword)) {
					showError("Incorrect old password.");
				} else {
					handler.saveToFile("employees.txt");
					popup2.close();
				}
			});
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
		buttonRow.getChildren().addAll(btnSubmitHours, btnViewCurrentWeek, btnViewArchiveWeek, btnLoadBulkHours);
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
				Week week = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());
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
				weekRepo.getCurrentWeekMap().put(empID, week);

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
			Week week = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());
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
			Week week = weekRepo.getCurrentWeekMap().get(empID);

			if (week == null) {
				txaHoursMessage.setText("No current week to archive.");
				return;
			}

			weekRepo.addRecord(week);
			weekRepo.getCurrentWeekMap().remove(empID);

			txaHoursMessage.setText("Week archived for " + emp.getFullName() + " (Week #" + week.getWeekNumber() + ")");
			txaHoursMessage.setStyle("-fx-text-fill: blue;");
		});

		btnDisplayAllCurrentWeek.setOnAction(e -> {
			List<Employee> allEmployees = getSortedEmployees();
			StringBuilder sb = new StringBuilder();
			String[] daysArr = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

			for (Employee emp : allEmployees) {
				sb.append(formatEmployeeHours(emp)).append("\n\n");
				Week week = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());
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

		btnLoadBulkHours.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select Bulk Hours File");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

			File selectedFile = fileChooser.showOpenDialog(null);
			if (selectedFile != null) {
				List<String> messages = weekRepo.loadBulkHours(selectedFile);

				if (!messages.isEmpty()) {
					showError(String.join("\n", messages));
				} else {
					showError("Bulk load completed successfully.");
				}
			}
		});

		return hbox;
	}

// ----- Payroll Reports Tab ----------------------------------
	private Pane buildPayrollReportsTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));

		// select employee drop down
		vbox.getChildren().add(new Label("Select Employee:"));
		vbox.getChildren().add(cmbReportEmployee);

		// radio buttons
		rbCurrent.setToggleGroup(reportGroup);
		rbAll.setToggleGroup(reportGroup);
		rbRange.setToggleGroup(reportGroup);
		rbCurrent.setSelected(true);
		HBox radioBox = new HBox(10, rbCurrent, rbAll, rbRange);
		vbox.getChildren().add(radioBox);

		// view report button and text area
		HBox buttonBox = new HBox(10, btnViewReport, btnSaveReport);
		vbox.getChildren().addAll(buttonBox, txaReport);
		txaReport.setPrefHeight(600);
		txaReport.setStyle("-fx-font-family: 'monospaced';");
		

		// load the current items depending on instance of employee
		cmbReportEmployee.getItems().clear();

		if (loggedInUser instanceof Manager) {
			cmbReportEmployee.getItems().add("All Employees");
			for (Manager m : handler.getManagers()) {
				cmbReportEmployee.getItems().add(m.getUsername());
			}
			for (Staff s : handler.getStaff()) {
				cmbReportEmployee.getItems().add(s.getUsername());
			}
			cmbReportEmployee.setDisable(false); // let manager choose
		} else {
			// Staff: only see themselves
			cmbReportEmployee.getItems().add(loggedInUser.getUsername());
			cmbReportEmployee.setValue(loggedInUser.getUsername());
			cmbReportEmployee.setDisable(true); // lock selection
		}

		// Radio for Week range selection
		rbRange.setOnMouseClicked(e -> {
			// Allow button to reopen pop if already selected vs having to click on and off
			// to refresh.
			if (rbRange.isSelected()) {
				String username = cmbReportEmployee.getValue();
				if (username == null) {
					txaReport.setText("Please select an employee first.");
					reportGroup.selectToggle(rbCurrent);
					return;
				}

				// load selected employee and their week repo
				Employee emp = handler.findEmployeeByUsername(username);
				List<Week> weeks = weekRepo.getRecordsForEmployee(emp.getEmployeeID());

				if (weeks.isEmpty()) {
					txaReport.setText("No archived weeks available.");
					reportGroup.selectToggle(rbCurrent);
					return;
				}

				// Stage pop up (implemented to prevent clutter on GUI)
				Stage popup = new Stage();
				popup.setTitle("Select Week Range");

				VBox popupBox = new VBox(10);
				popupBox.setPadding(new Insets(10));

				ComboBox<String> cmbStart = new ComboBox<>();
				ComboBox<String> cmbEnd = new ComboBox<>();

				// list available week for the selected employee
				for (Week w : weeks) {
					String label = "Week #" + w.getWeekNumber();
					cmbStart.getItems().add(label);
					cmbEnd.getItems().add(label);
				}

				Button btnConfirm = new Button("Confirm");
				// Confirm button action
				btnConfirm.setOnAction(ev -> {
					// store the start and end values for later call args
					selectedRangeStart = cmbStart.getValue();
					selectedRangeEnd = cmbEnd.getValue();

					// automatically display report after confirm is clicked given that the ranges
					// are not null.
					if (rbRange.isSelected() && selectedRangeStart != null && selectedRangeEnd != null) {
						btnViewReport.fire();
						popup.close();
					}
				});

				// SHOW pop up
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

		    String mode;
		    if (rbCurrent.isSelected()) mode = "current";
		    else if (rbAll.isSelected()) mode = "all";
		    else if (rbRange.isSelected()) mode = "range";
		    else {
		        txaReport.setText("Please select a report type.");
		        return;
		    }

		    final Integer start;
		    final Integer end;
		    if (mode.equals("range")) {
		        if (selectedRangeStart == null || selectedRangeEnd == null) {
		            txaReport.setText("Please select a valid week range.");
		            return;
		        }
		        start = Integer.parseInt(selectedRangeStart.replace("Week #", ""));
		        end = Integer.parseInt(selectedRangeEnd.replace("Week #", ""));
		    } else {
		        start = null;
		        end = null;
		    }

		    if (username.equals("All Employees")) {
		        StringBuilder reportAll = new StringBuilder();
		        double totalGross = 0, totalTax = 0, totalNet = 0;

		        List<Employee> sorted = new ArrayList<>(handler.getAllEmps());
		        sorted.sort(Comparator.comparing(Employee::getDepartment)
		            .thenComparing(Employee::getLastName)
		            .thenComparing(Employee::getFirstName)
		            .thenComparing(Employee::getEmployeeID));

		        for (Employee emp : sorted) {

		            List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
		            Week current = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());

		            String report = PayRollCalculator.generateReport(emp, history, current, mode, start, end);
		            if (report.equals("No week data found.")) continue; // Skip employee with no data
		            reportAll.append(report).append("\n");

		            List<Week> filtered = switch (mode) {
		                case "current" -> current != null ? List.of(current) : List.of();
		                case "all" -> history;
		                case "range" -> history.stream()
		                        .filter(w -> w.getWeekNumber() >= start && w.getWeekNumber() <= end)
		                        .toList();
		                default -> List.of();
		            };

		            if (!filtered.isEmpty()) {
		                PayRollCalculator.PayStub stub = PayRollCalculator.calculatePay(emp, filtered);
		                totalGross += stub.grossPay;
		                totalTax += stub.taxes;
		                totalNet += stub.netPay;
		            } else {
		                reportAll.append("No data for selected range.\n");
		            }

		            reportAll.append("\n");
		        }

		        reportAll.append("========  Total ========\n");
		        reportAll.append(String.format("Total Gross Pay: $%.2f\n", totalGross));
		        reportAll.append(String.format("Total Taxes: $%.2f\n", totalTax));
		        reportAll.append(String.format("Total Net Pay: $%.2f\n", totalNet));
		        txaReport.setText(reportAll.toString());
		        return;
		    }

		    // Single employee
		    Employee emp = handler.findEmployeeByUsername(username);
		    List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
		    Week current = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());

		    String report = PayRollCalculator.generateReport(emp, history, current, mode, start, end);
		    txaReport.setText(report);
		});

		btnSaveReport.setOnAction(e -> {
		    String username = cmbReportEmployee.getValue();
		    if (username == null) {
		        showError("Please select an employee first.");
		        return;
		    }

		    FileChooser fileChooser = new FileChooser();
		    fileChooser.setTitle("Save Paystub Report");
		    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
		    File file = fileChooser.showSaveDialog(null);
		    if (file == null) return;

		    try (PrintWriter writer = new PrintWriter(file)) {
		        String mode;
		        if (rbCurrent.isSelected()) mode = "current";
		        else if (rbAll.isSelected()) mode = "all";
		        else if (rbRange.isSelected()) mode = "range";
		        else {
		            showError("Please select a report type.");
		            return;
		        }

		        final Integer start;
		        final Integer end;
		        if (mode.equals("range")) {
		            if (selectedRangeStart == null || selectedRangeEnd == null) {
		                showError("Please select a valid week range.");
		                return;
		            }
		            start = Integer.parseInt(selectedRangeStart.replace("Week #", ""));
		            end = Integer.parseInt(selectedRangeEnd.replace("Week #", ""));
		        } else {
		            start = null;
		            end = null;
		        }

		        if (username.equals("All Employees")) {
		            double totalGross = 0, totalTax = 0, totalNet = 0;

		            List<Employee> sorted = new ArrayList<>(handler.getAllEmps());
		            sorted.sort(Comparator.comparing(Employee::getDepartment)
		                .thenComparing(Employee::getLastName)
		                .thenComparing(Employee::getFirstName)
		                .thenComparing(Employee::getEmployeeID));

		            for (Employee emp : sorted) {
		                writer.println("===== " + emp.getFirstName() + " " + emp.getLastName() +
		                        " (" + emp.getEmployeeID() + ") - " + emp.getDepartment() + " =====");

		                List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
		                Week current = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());

		                String report = PayRollCalculator.generateReport(emp, history, current, mode, start, end);
		                writer.println(report);

		                List<Week> filtered = switch (mode) {
		                    case "current" -> current != null ? List.of(current) : List.of();
		                    case "all" -> history;
		                    case "range" -> history.stream()
		                            .filter(w -> w.getWeekNumber() >= start && w.getWeekNumber() <= end)
		                            .toList();
		                    default -> List.of();
		                };

		                if (!filtered.isEmpty()) {
		                    PayRollCalculator.PayStub stub = PayRollCalculator.calculatePay(emp, filtered);
		                    totalGross += stub.grossPay;
		                    totalTax += stub.taxes;
		                    totalNet += stub.netPay;
		                } else {
		                    writer.println("No data for selected range.");
		                }

		                writer.println();
		            }

		            writer.println("======== Total ========");
		            writer.printf("Total Gross Pay: $%.2f\n", totalGross);
		            writer.printf("Total Taxes: $%.2f\n", totalTax);
		            writer.printf("Total Net Pay: $%.2f\n", totalNet);
		        } else {
		            Employee emp = handler.findEmployeeByUsername(username);
		            List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
		            Week current = weekRepo.getCurrentWeekMap().get(emp.getEmployeeID());

		            String report = PayRollCalculator.generateReport(emp, history, current, mode, start, end);
		            writer.println(report);
		        }

		        showError("Report saved successfully.");
		    } catch (Exception ex) {
		        showError("Error saving report: " + ex.getMessage());
		    }
		});

		return vbox;
	}

// -----Employees Hour Entry Tab -----------------------------------------------------
	private Pane buildHoursEntryEmployeeTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));

		if (loggedInUser != null) {
			lblEmployeeHoursHeader.setText(
					"Employee: " + loggedInUser.getFullNameNoUser() + " | ID: " + loggedInUser.getEmployeeID());
		} else {
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
			if (i >= 5)
				ptoEmployeePerDay[i].setDisable(true);
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

		// Submit hours for employee
		btnEmployeeSubmitHours.setOnAction(e -> {
			if (!(loggedInUser instanceof Staff)) {
				txaEmployeeHoursMessage.setText("Only staff employees can use this form.");
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: red;");
				return;
			}

			String empID = loggedInUser.getEmployeeID();
			Week existingWeek = weekRepo.getCurrentWeekMap().get(empID);

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
				weekRepo.getCurrentWeekMap().put(empID, newWeek);

				txaEmployeeHoursMessage.setText("Hours submitted successfully for this week.");
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: green;");
			} catch (NumberFormatException ex) {
				txaEmployeeHoursMessage.setText("Error: " + ex.getMessage());
				txaEmployeeHoursMessage.setStyle("-fx-text-fill: red;");
			}
		});

		return vbox;
	}
	
	// -----Manager Admin Tools Tab -----------------------------------------------------
	
private ScrollPane buildAdminToolsTab() {
    VBox vbox = new VBox(20);
    vbox.setPadding(new Insets(20));

    // Helper: get sorted usernames
    List<String> usernames = new ArrayList<>();
    for (Employee emp : getSortedEmployees()) {
        usernames.add(emp.getUsername());
    }

 // --- Edit Daily Entry Section (Multiple Days) ---
    ComboBox<String> cmbEditDailyEmp = new ComboBox<>();
    cmbEditDailyEmp.getItems().addAll(usernames);
    cmbEditDailyEmp.setPromptText("Select Employee");

    ComboBox<String> cmbEditWeek = new ComboBox<>();
    cmbEditWeek.setPromptText("Select Week");
    TextField[] txtHoursEditFields = new TextField[7];
    CheckBox[] chkPTOEditFields = new CheckBox[7];
    
 // Day labels row
    HBox rowDays = new HBox(10);
    rowDays.getChildren().add(new Label(String.format("%-6s", "Day:")));
    String[] dayNames = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    for (String day : dayNames) {
    	Label lbl = new Label(day);
        lbl.setPrefWidth(50); // ensures even spacing for each day
        lbl.setStyle("-fx-font-family: 'Courier New';"); // optional: clean alignment
        rowDays.getChildren().add(lbl); // <- this is where lbl goes
    }

    // Hours row
    HBox rowHours = new HBox(10);
    rowHours.getChildren().add(new Label(String.format("%-6s", "Hours:")));
    for (int i = 0; i < 7; i++) {
        txtHoursEditFields[i] = new TextField();
        txtHoursEditFields[i].setPrefWidth(50);
        rowHours.getChildren().add(txtHoursEditFields[i]);
    }

    // PTO row
    HBox rowPTO = new HBox(10);
    rowPTO.getChildren().add(new Label(String.format("%-6s", "PTO:")));
    for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
        chkPTOEditFields[dayIndex] = new CheckBox();
        chkPTOEditFields[dayIndex].setPrefWidth(50);
        if (dayIndex < 5) {
            rowPTO.getChildren().add(chkPTOEditFields[dayIndex]); // Only add Mon–Fri checkboxes
        }
    }

    // Load available weeks when employee is selected
    cmbEditDailyEmp.setOnAction(e -> {
        cmbEditWeek.getItems().clear();
        String username = cmbEditDailyEmp.getValue();
        Employee emp = handler.findEmployeeByUsername(username);
        if (emp != null) {
            List<Week> history = weekRepo.getRecordsForEmployee(emp.getEmployeeID());
            for (Week w : history) {
                cmbEditWeek.getItems().add("Week #" + w.getWeekNumber());
            }
        }
    });

    // Prefill hours/PTO when week is selected
    cmbEditWeek.setOnAction(e -> {
        String username = cmbEditDailyEmp.getValue();
        String selectedWeek = cmbEditWeek.getValue();
        if (username == null || selectedWeek == null) return;

        Employee emp = handler.findEmployeeByUsername(username);
        if (emp == null) return;

        int weekNum = Integer.parseInt(selectedWeek.replace("Week #", ""));
        Week week = weekRepo.getWeek(emp.getEmployeeID(), weekNum);
        if (week != null) {
            for (int i = 0; i < 7; i++) {
                txtHoursEditFields[i].setText(String.valueOf(week.getHours()[i]));
                chkPTOEditFields[i].setSelected(week.getIsPTO()[i]);
            }
        }
    });

    Button btnEditFullWeek = new Button("Submit Edits");
    btnEditFullWeek.setOnAction(e -> {
        if (!(loggedInUser instanceof Manager)) {
            showError("Only managers can edit entries.");
            return;
        }

        String username = cmbEditDailyEmp.getValue();
        String selectedWeek = cmbEditWeek.getValue();
        if (username == null || selectedWeek == null) {
            showError("Select employee and week.");
            return;
        }

        Employee emp = handler.findEmployeeByUsername(username);
        int weekNum = Integer.parseInt(selectedWeek.replace("Week #", ""));
        Week week = weekRepo.getWeek(emp.getEmployeeID(), weekNum);

        int[] newHours = new int[7];
        boolean[] newPTOs = new boolean[7];
        try {
            for (int i = 0; i < 7; i++) {
                newHours[i] = Integer.parseInt(txtHoursEditFields[i].getText().trim());
                newPTOs[i] = chkPTOEditFields[i].isSelected();
            }
        } catch (Exception ex) {
            showError("Invalid hour input.");
            return;
        }

        Manager manager = (Manager) loggedInUser;
        boolean success = manager.editSelectiveDays(
        	    emp,
        	    week,
        	    newHours,
        	    newPTOs,
        	    weekRepo,
        	    "audit_trail.txt"
        	);

        showError(success ? "Edits saved." : "Enter a change or Invaild Hours.");
    });
    

    // Layout
    VBox vbEditFullWeek = new VBox(10,
    	    new HBox(10, new Label("Employee:"), cmbEditDailyEmp),
    	    new HBox(10, new Label("Week:"), cmbEditWeek),
    	    rowDays,
    	    rowHours,
    	    rowPTO,
    	    btnEditFullWeek
    	);
    	vbEditFullWeek.setPadding(new Insets(10));
    	TitledPane tpEditDailyEntry = new TitledPane("Edit Daily Entry", vbEditFullWeek);

    // --- Audit Employee Section ---
    ComboBox<String> cmbAuditEmp = new ComboBox<>();
    cmbAuditEmp.getItems().addAll(usernames);
    cmbAuditEmp.setPromptText("Select Employee");
    Button btnAuditEmployee = new Button("Audit");
    Button btnDeleteEmp = new Button("Delete Employee");
    TextArea txtAuditOutput = new TextArea();
    txtAuditOutput.setEditable(false);
    txtAuditOutput.setPrefHeight(250);
    txtAuditOutput.setWrapText(false);
    txtAuditOutput.setFont(javafx.scene.text.Font.font("Monospaced", 12));
    
    
    btnDeleteEmp.setOnAction(e -> {
        String username = cmbAuditEmp.getValue();

        if (username == null) {
            showError("Please select an employee to delete.");
            return;
        }

        boolean confirmed = showConfirmation("Are you sure you want to delete this employee?");
        if (!confirmed) return;

        boolean deleted = handler.deleteEmployeeInFile(username); // your method

        if (deleted) {
            showError("Employee successfully deleted.");

            // Also update internal memory (optional)
            Employee emp = handler.findEmployeeByUsername(username);
            if (emp instanceof Manager) {
                handler.getManagers().remove(emp);
            } else if (emp instanceof Staff) {
                handler.getStaff().remove(emp);
            }

            handler.saveToFile("employees.txt"); // make sure the file reflects current memory

            // Refresh UI ComboBoxes
            usernames.remove(username); // if your list is mutable
            cmbAuditEmp.getItems().remove(username);
            cmbEditDailyEmp.getItems().remove(username);
 

        } else {
            showError("Employee could not be deleted.");
        }
    });
    
    btnAuditEmployee.setOnAction(e -> {
        if (!(loggedInUser instanceof Manager)) {
            showError("Only managers can audit employees.");
            return;
        }

        Manager manager = (Manager) loggedInUser;
        String employeeUsername = cmbAuditEmp.getValue();
        Employee emp = handler.findEmployeeByUsername(employeeUsername);

        if (emp != null) {
        	String result = manager.auditEmployeeFull(emp, "audit_trail.txt");
        	txtAuditOutput.setText(result);
        }
    });
    
    VBox vbAuditEmployee = new VBox(10,
    	    new HBox(10, new Label("Employee:"), cmbAuditEmp),
    	    new HBox(10, btnAuditEmployee, btnDeleteEmp),  // Audit and Delete side by side
    	    txtAuditOutput
    	);
    	vbAuditEmployee.setPadding(new Insets(10));
    	TitledPane tpAuditEmployee = new TitledPane("Audit / Delete Employee", vbAuditEmployee);

    // --- Audit Editor Section ---
    ComboBox<String> cmbAuditEditorEmp = new ComboBox<>();
    cmbAuditEditorEmp.getItems().addAll(usernames);
    cmbAuditEditorEmp.setPromptText("Select Employee");
    ComboBox<String> cmbMode = new ComboBox<>();
    cmbMode.getItems().addAll("single", "range", "all");
    cmbMode.setValue("all");
    TextField txtWeek = new TextField();
    txtWeek.setPromptText("Week # (for single)");
    TextField txtRangeStart = new TextField();
    txtRangeStart.setPromptText("Range Start");
    TextField txtRangeEnd = new TextField();
    txtRangeEnd.setPromptText("Range End");
    Button btnAuditEditor = new Button("Audit Editor");


    btnAuditEditor.setOnAction(e -> {
        if (!(loggedInUser instanceof Manager)) {
            showError("Only managers can audit editor.");
            return;
        }
        Manager manager = (Manager) loggedInUser;
        String mode = cmbMode.getValue();
        Integer week = txtWeek.getText().isBlank() ? null : Integer.parseInt(txtWeek.getText());
        Integer rangeStart = txtRangeStart.getText().isBlank() ? null : Integer.parseInt(txtRangeStart.getText());
        Integer rangeEnd = txtRangeEnd.getText().isBlank() ? null : Integer.parseInt(txtRangeEnd.getText());
        String result = manager.auditEditor("audit_trail.txt", mode, week, rangeStart, rangeEnd);
        showError(result);
    });

    GridPane gpAuditEditor = new GridPane();
    gpAuditEditor.setHgap(10);
    gpAuditEditor.setVgap(10);
    gpAuditEditor.addRow(0, new Label("Employee:"), cmbAuditEditorEmp);
    gpAuditEditor.addRow(1, new Label("Mode:"), cmbMode);
    gpAuditEditor.addRow(2, new Label("Week:"), txtWeek);
    gpAuditEditor.addRow(3, new Label("Range Start:"), txtRangeStart);
    gpAuditEditor.addRow(4, new Label("Range End:"), txtRangeEnd);
    gpAuditEditor.addRow(5, btnAuditEditor);
    TitledPane tpAuditEditor = new TitledPane("Audit Editor", gpAuditEditor);

    // --- Edit Employee Section ---
    ComboBox<String> cmbEditEmp = new ComboBox<>();
    cmbEditEmp.getItems().addAll(usernames);
    cmbEditEmp.setPromptText("Select Employee");
    TextField txtFirstName = new TextField();
    TextField txtLastName = new TextField();
    TextField txtDepartment = new TextField();
    TextField txtPayRate = new TextField();
    TextField txtTaxRate = new TextField();
    PasswordField txtPassword = new PasswordField();
    Button btnUpdateEmployee = new Button("Update Employee");

    cmbEditEmp.setOnAction(e -> {
        String username = cmbEditEmp.getValue();
        Employee emp = handler.findEmployeeByUsername(username);
        if (emp != null) {
            txtFirstName.setText(emp.getFirstName());
            txtLastName.setText(emp.getLastName());
            txtDepartment.setText(emp.getDepartment());
            txtPayRate.setText(String.valueOf(emp.getPayRate()));
            txtTaxRate.setText(String.valueOf(emp.getTaxRate()));
            txtPassword.setText(""); // Do not autofill password
        }
    });

    btnUpdateEmployee.setOnAction(e -> {
        if (!(loggedInUser instanceof Manager)) {
            showError("Only managers can edit employees.");
            return;
        }
        Manager manager = (Manager) loggedInUser;
        String username = cmbEditEmp.getValue();
        Employee emp = handler.findEmployeeByUsername(username);
        if (emp == null) {
            showError("Employee not found.");
            return;
        }
        boolean changed = false;
        try {
            changed = manager.editEmployee(
                emp,
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtPassword.getText().trim(),
                txtDepartment.getText().trim(),
                txtPayRate.getText().isBlank() ? null : Double.parseDouble(txtPayRate.getText().trim()),
                txtTaxRate.getText().isBlank() ? null : Double.parseDouble(txtTaxRate.getText().trim())
            );
        } catch (Exception ex) {
            showError("Error editing employee: " + ex.getMessage());
            return;
        }
        if (changed) {
            handler.saveToFile("employees.txt");
            showError("Employee updated successfully.");
        } else {
            showError("No changes made.");
        }
    });

    GridPane gpEditEmployee = new GridPane();
    gpEditEmployee.setHgap(10);
    gpEditEmployee.setVgap(10);
    gpEditEmployee.addRow(0, new Label("Employee:"), cmbEditEmp);
    gpEditEmployee.addRow(1, new Label("First Name:"), txtFirstName);
    gpEditEmployee.addRow(2, new Label("Last Name:"), txtLastName);
    gpEditEmployee.addRow(3, new Label("Department:"), txtDepartment);
    gpEditEmployee.addRow(4, new Label("Pay Rate:"), txtPayRate);
    gpEditEmployee.addRow(5, new Label("Tax Rate:"), txtTaxRate);
    gpEditEmployee.addRow(6, new Label("Password:"), txtPassword);
    gpEditEmployee.addRow(7, btnUpdateEmployee);
    TitledPane tpEditEmployee = new TitledPane("Edit Employee", gpEditEmployee);

    vbox.getChildren().addAll(
        tpEditDailyEntry,
        tpAuditEmployee,
        tpAuditEditor,
        tpEditEmployee
    );

    ScrollPane scrollPane = new ScrollPane(vbox);
    scrollPane.setFitToWidth(true);
    return scrollPane;
}



//Helpers to format info to be displayed as the stories required, I kinda came up with a way to make it look organized.-----------

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.NONE, message);
		alert.getButtonTypes().add(ButtonType.OK);
		alert.showAndWait();
	}
	
	private boolean showConfirmation(String msg) {
	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
	    alert.setHeaderText(null);
	    alert.showAndWait();
	    return alert.getResult() == ButtonType.YES;
	}

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
		} else {
			sb.insert(0, ("STAFF | "));
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
	
	private void clearAllFields(Node node) {
	    if (node instanceof TextField) {
	        ((TextField) node).clear();
	    } else if (node instanceof CheckBox) {
	        ((CheckBox) node).setSelected(false);
	    } else if (node instanceof ComboBox) {
	        ((ComboBox<?>) node).getSelectionModel().clearSelection();
	    } else if (node instanceof TextArea) {
	        ((TextArea) node).clear();
	    } else if (node instanceof Pane) {
	        for (Node child : ((Pane) node).getChildren()) {
	            clearAllFields(child);
	        }
	    }
	}
}
