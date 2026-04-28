package model;

/**
 *
 * @author chunk
 */
public class Feedback {

    private String feedbackID;
    private String doctorID;
    private String staffID;
    private String customerID;
    private String feedback;
    private String comment;
    private String charge;
    private String appointmentID;

    public Feedback(String feedbackID, String doctorID, String staffID, String customerID, String feedback, String comment, String charge, String appointmentID) {
        this.feedbackID = feedbackID;
        this.doctorID = doctorID;
        this.staffID = staffID;
        this.customerID = customerID;
        this.feedback = feedback;
        this.comment = comment;
        this.charge = charge;
        this.appointmentID = appointmentID;
    }

    public String getFeedbackID() {
        return feedbackID;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public String getStaffID() {
        return staffID;
    }
    
    public String getCustomerID() {
        return customerID;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getComment() {
        return comment;
    }

    public String getCharge() {
        return charge;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public void setFeedbackID(String feedbackID) {
        this.feedbackID = feedbackID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }
    
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public void setAppointmentID(String appointmentID) {
        this.appointmentID = appointmentID;
    }
}
