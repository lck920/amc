package model;

public class Payment {
    private String paymentID;
    private String appointmentID;
    private double amount;   
    private String method;

    public Payment(String paymentID, String appointmentID, double amount, String method) {
        this.paymentID = paymentID;
        this.appointmentID = appointmentID;
        this.amount = amount;
        this.method = method;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public double getAmount() {   
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }

    public void setAmount(double amount) {   
        this.amount = amount;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
