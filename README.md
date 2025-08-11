# Payroll Management System

## Overview
The **Payroll Management System** is a JavaFX-based desktop application for managing employees, tracking hours, calculating pay, and generating payroll reports.  
It supports **Manager** and **Staff** roles with secure login, PTO tracking, overtime handling, and detailed reporting.

This project was created for a **Software Engineering** course to demonstrate Java programming, file handling, and GUI development.

---

## Features

### **User Authentication**
- Secure login with Manager and Staff roles.
- Role-based feature access.

### **Employee Management** *(Managers only)*
- Add, edit, and delete employees.
- Store details:
  - First name, last name, username, password
  - Department, pay rate, tax rate, PTO days, employee ID

### **Hours Entry**
- Input hours for each day of the week.
- PTO checkbox for weekday leave.
- Tracks **current week** in memory until archived.
- Overtime calculated automatically for Staff.

### **Payroll Calculation**
- **Staff**:
  - Time-and-a-half for weekday hours above 40 (excluding PTO).
  - Time-and-a-half for all weekend hours.
  - PTO is always 8 hours per weekday and deducted from PTO balance.
- **Managers**:
  - Paid for 40 hours per week regardless of hours worked.
- Calculates gross pay, taxes, and net pay.

### **Reporting**
- Generate pay stubs for:
  - Current week
  - All weeks
  - Range of weeks
- View single employee or **All Employees** reports (sorted by department, name, and ID).
- Save reports as `.txt` files.
- Reports include:
  - Hours per day
  - PTO usage
  - Total hours
  - Overtime hours
  - Gross pay, taxes, net pay
  - PTO remaining

### **Admin Tools**
- Edit daily hours for any employee/week.
- View audit logs for employee actions and edits.
- Edit employee details (department, pay rate, tax rate, password).

---

## Technology Stack
- **Java** 21
- **JavaFX** 21.0.3
- **Plain Text File Storage** for employee and week records
- **OOP Principles**: Encapsulation, inheritance, polymorphism

---

## Installation & Setup

### **Prerequisites**
- Java 21 or higher
- JavaFX SDK 21.0.3
- Compatible IDE (IntelliJ IDEA, Eclipse, or VS Code with JavaFX support)

### **Steps**
1. Download or clone the repository:
   ```bash
   git clone https://github.com/yourusername/payroll-system.git
   cd payroll-system
