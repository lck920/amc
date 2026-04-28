package model;

/**
 *
 * @author chunk
 */
public class Appointment {

    private String appointmentID;
    private String customerUsername;
    private String doctorUsername;
    private String date;
    private String time;
    private String status; 
    private String bookedBy;

    public Appointment(String appointmentID, String customerUsername, String doctorUsername, String date, String time, String status, String bookedBy) {
        this.appointmentID = appointmentID;
        this.customerUsername = customerUsername;
        this.doctorUsername = doctorUsername;
        this.date = date;
        this.time = time;
        this.status = status;
        this.bookedBy = bookedBy;
    }

    // getter
    public String getAppointmentID() {
        return appointmentID;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getBookedBy() {
        return bookedBy;
    }


    public void setStatus(String status) {
        this.status = status;
    }
}
