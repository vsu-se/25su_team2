package application;

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
                totalHours, ptoUsed, grossPay, taxes, netPay
            );
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
