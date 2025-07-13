package application;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.ArrayList;


public class Main extends Application {
	private DataHandler handler = new DataHandler("employees.txt");
	private WeekRepository weekRepo = new WeekRepository();
	private Employee loggedInUser = null;
	private Tab tabEmployeeMgmt;
	private Tab tabHoursEntry;
	private Tab tabPayrollReports;


	// public static Manager empManager = new Manager(employee);

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
	protected Label lblWelcomeMng= new Label();
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
	protected TextArea txaHoursMessage = new TextArea();

	// Tab 4: Payroll Reports, both Employee and Manager
	// -------------------------------------------------------------
	protected ComboBox<String> cmbReportEmployee = new ComboBox<>();
	protected Button btnViewReport = new Button("View Report");
	protected TextArea txaReport = new TextArea();

	// on application in the Employee class)

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

		// Add tabs to TabPane
		// ------------------------------------------------------------------
		tabPane.getTabs().addAll(tabLogin, tabEmployeeMgmt, tabHoursEntry, tabPayrollReports);

		// Disable tabs except for the log in tab
		// --------------------------------------------------
		tabEmployeeMgmt.setDisable(true);
		tabHoursEntry.setDisable(true);
		tabPayrollReports.setDisable(true);

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

			for (Manager m : handler.getManagers()) {
				if (m.getUsername().equalsIgnoreCase(inputUsername)) {
					if (m.authenticate(inputPassword)) {
						lblLoginMessage.setText("Welcome Manager: " + m.getFullName());
						lblLoginMessage.setStyle("-fx-text-fill: green;");
						loggedInUser = m;

						tabEmployeeMgmt.setDisable(false);
						tabHoursEntry.setDisable(false);
						tabPayrollReports.setDisable(false);

						tabPane.getSelectionModel().select(tabEmployeeMgmt);

						found = true;
						break;
					}
					else {
						lblLoginMessage.setText("Incorrect password.");
						lblLoginMessage.setStyle("-fx-text-fill: red;");
						tabEmployeeMgmt.setDisable(true);
						tabHoursEntry.setDisable(true);
						tabPayrollReports.setDisable(true);
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

							tabEmployeeMgmt.setDisable(true);
							tabHoursEntry.setDisable(true);
							tabPayrollReports.setDisable(true);

							found = true;
							break;
						}
						else {
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
					newEmp = new Manager(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
				} else {
					newEmp = new Staff(firstName, lastName, username, password, department, payRate, taxRate, ptoDays);
				}

				boolean added = handler.addEmployee(newEmp);

				if (added) {
					//Updates both mangers and staff employees
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
				}
				else {
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
				if (cmp != 0) return cmp;
				cmp = m1.getFirstName().compareToIgnoreCase(m2.getFirstName());
				if (cmp != 0) return cmp;
				cmp = m1.getDepartment().compareToIgnoreCase(m2.getDepartment());
				if (cmp != 0) return cmp;
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
		vbox.getChildren().add(btnSubmitHours);
		vbox.getChildren().add(txaHoursMessage);

		// Populate employee ComboBox
		EmpSelected.getItems().clear();
		for (Manager m : handler.getManagers()) {
			EmpSelected.getItems().add(m.getUsername());
		}
		for (Staff s : handler.getStaff()) {
			EmpSelected.getItems().add(s.getUsername());
		}

		// Submit hours action
		btnSubmitHours.setOnAction(e -> {
			String selectedUsername = EmpSelected.getValue();
			if (selectedUsername == null) {
				txaHoursMessage.setText("Please select an employee.");
				return;
			}

			Employee emp = null;
			for (Manager m : handler.getManagers()) {
				if (m.getUsername().equalsIgnoreCase(selectedUsername)) {
					emp = m;
					break;
				}
			}
			if (emp == null) {
				for (Staff s : handler.getStaff()) {
					if (s.getUsername().equalsIgnoreCase(selectedUsername)) {
						emp = s;
						break;
					}
				}
			}

			if (emp == null) {
				txaHoursMessage.setText("Employee not found.");
				return;
			}

			int[] hours = new int[7];
			boolean[] pto = new boolean[7];

			try {
				//fill the array with valid hours and PTO day
				for (int i = 0; i < 7; i++) {
					String input = txtHoursPerDay[i].getText().trim();
					hours[i] = input.isEmpty() ? 0 : Integer.parseInt(input);
					if (hours[i] < 0 || hours[i] > 24) {
						throw new NumberFormatException("Hours must be between 0 and 24 for day " + (i + 1));
					}
					pto[i] = PTOPerDay[i].isSelected();
				}
				
				//calculate week, construct the week obj, and then store in weekRepo
				int nextWeekNum = weekRepo.getRecordsForEmployee(emp.getEmployeeID()).size() + 1;
				Week week = new Week(emp.getEmployeeID(), nextWeekNum, hours, pto);
				weekRepo.addRecord(week);
				
				//validate success for user
				txaHoursMessage.setText("Hours for Week " + nextWeekNum + " recorded for " + emp.getFullName());
				txaHoursMessage.setStyle("-fx-text-fill: green;");
				
				//clear week obj; ready for next week input
				for (int i = 0; i < 7; i++) {
					txtHoursPerDay[i].clear();
					PTOPerDay[i].setSelected(false);
				}
			
		
			} catch (NumberFormatException ex) {
				txaHoursMessage.setText("Invalid input: " + ex.getMessage());
				txaHoursMessage.setStyle("-fx-text-fill: red;");
			}
		});

		return vbox;
	}


	// ----- Payroll Reports Tab ----------------------------------
	private Pane buildPayrollReportsTab() {
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10));

		vbox.getChildren().add(new Label("Select Employee:"));
		vbox.getChildren().add(cmbReportEmployee);
		vbox.getChildren().add(btnViewReport);
		vbox.getChildren().add(txaReport);

//		btnViewReport.setOnAction(new ViewReportHandler());

		return vbox;
	}

//	Helpers to format info to be displayed as the stories required, I kinda came up with a way to make it look organized.
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
		}
		else{
			sb.insert(0,("STAFF | "));
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
		}
		else{
			sb.insert(0,("STAFF | "));
		}
		return sb.toString();
	}

	private List<Employee> getSortedEmployees() {
		List<Employee> allEmployees = new ArrayList<>();
		allEmployees.addAll(handler.getManagers());
		allEmployees.addAll(handler.getStaff());
		allEmployees.sort((e1, e2) -> {
			int cmp = e1.getLastName().compareToIgnoreCase(e2.getLastName());
			if (cmp != 0) return cmp;
			cmp = e1.getFirstName().compareToIgnoreCase(e2.getFirstName());
			if (cmp != 0) return cmp;
			cmp = e1.getDepartment().compareToIgnoreCase(e2.getDepartment());
			if (cmp != 0) return cmp;
			return e1.getEmployeeID().compareTo(e2.getEmployeeID());
		});
		return allEmployees;
	}



	public static void main(String[] args) {
		System.out.println("");

		launch(args);
	}
}
