package service;

import java.io.*;
import java.util.ArrayList;
import model.*;

/**
 *
 * @author chunk
 */
public class AppointmentService {

    public static ArrayList<Appointment> appointmentList = new ArrayList<>();

    public static void getAppointment() {
        appointmentList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\appointments.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] appointmentDetails = line.split(",");
                if (appointmentDetails.length < 7) {
                    continue;
                }
                String appointmentID = appointmentDetails[0];
                String customerUsername = appointmentDetails[1];
                String doctorUsername = appointmentDetails[2];
                String date = appointmentDetails[3];
                String time = appointmentDetails[4];
                String status = appointmentDetails[5];
                String bookedBy = appointmentDetails[6];

                Appointment appt = new Appointment(appointmentID, customerUsername, doctorUsername, date, time, status, bookedBy);
                appointmentList.add(appt);
            }
        } catch (Exception e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
    }

    public static void saveAppointment() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\appointments.txt"))) {
            for (Appointment appointment : appointmentList) {
                String line = String.join(",",
                        appointment.getAppointmentID(),
                        appointment.getCustomerUsername(),
                        appointment.getDoctorUsername(),
                        appointment.getDate(),
                        appointment.getTime(),
                        appointment.getStatus(),
                        appointment.getBookedBy()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }

    public static Appointment searchAppointment(String appointmentID) {
        for (Appointment appt : appointmentList) {
            if (appt.getAppointmentID().equals(appointmentID)) {
                return appt;
            }
        }
        return null;
    }

    public static void addAppointment(Appointment appointment) {
        appointmentList.add(appointment);
    }

}
