package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class Main extends Application {
	private DataHandler handler = new DataHandler("employees.txt");

	// Data from Manager class
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
	protected Button btnAddEmployee = new Button("Add Employee");
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

	// To store current log in user (Not use if I'll keep this variable yet, depends
	// on application in the Employee class)
	// private Employee loggedInUser = null;

	@Override
	public void start(Stage primaryStage) {
		try {
			Pane root = buildGui();
			Scene scene = new Scene(root, 900, 700);
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
		Tab tabEmployeeMgmt = new Tab("Employee Management", buildEmployeeManagementTab());
		Tab tabHoursEntry = new Tab("Hours Entry", buildHoursEntryTab());
		Tab tabPayrollReports = new Tab("Payroll Reports", buildPayrollReportsTab());

		// Add tabs to TabPane
		// ------------------------------------------------------------------
		tabPane.getTabs().addAll(tabLogin, tabEmployeeMgmt, tabHoursEntry, tabPayrollReports);

		// Disable tabs except for the log in tab
		// --------------------------------------------------
		tabEmployeeMgmt.setDisable(false);
		tabHoursEntry.setDisable(false);
		tabPayrollReports.setDisable(false);

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
						found = true;
						break;
					} else {
						lblLoginMessage.setText("Incorrect password.");
						return;
					}
				}
			}

			if (!found) {
				for (Staff s : handler.getStaff()) {
					if (s.getUsername().equalsIgnoreCase(inputUsername)) {
						if (s.authenticate(inputPassword)) {
							lblLoginMessage.setText("Welcome Staff: " + s.getFullName());
							found = true;
							break;
						} else {
							lblLoginMessage.setText(" Incorrect password.");
							return;
						}
					}
				}
			}

			if (!found) {
				lblLoginMessage.setText("Username not found.");
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

//		btnAddEmployee.setOnAction(new AddEmployeeHandler());

		VBox vboxEmps = new VBox(10, new Label("Employees:"), listEmps, txaEmpMessage);
		vboxEmps.setPadding(new Insets(10));
		vboxEmps.setPrefWidth(300);

		VBox vboxManag = new VBox(10, new Label("Managers:"), listManags, txaManagsMessage);
		vboxManag.setPadding(new Insets(10));
		vboxManag.setPrefWidth(300);

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
			// PTO only allowed Mon-Fri
			if (i >= 5)
				chkPTO.setDisable(true);
			daysGrid.add(chkPTO, 2, i);
		}

		vbox.getChildren().add(daysGrid);
		vbox.getChildren().add(btnSubmitHours);
		vbox.getChildren().add(txaHoursMessage);

//		btnSubmitHours.setOnAction(new SubmitHoursHandler());

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

	// --- Event Handlers --- (Can work on these yet since Manager, Employee and
	// Staff classes aren't done yet.)

	// Login Handler
	// Add Employee Handler
	// Submit Hours Handler
	// View Report Handler
	// Others: Any additional method not found in the main classes

	public static void main(String[] args) {
		System.out.println(" Passwords have been hashed and saved. You can now remove this block.");

		launch(args);
	}
}
