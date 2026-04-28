package service;

import java.io.*;
import java.util.ArrayList;
import model.*;

/**
 *
 * @author chunk
 */
public class FeedbackService {

    public static ArrayList<Feedback> feedbackList = new ArrayList<>();

    public static void getFeedbacks() {
        feedbackList.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\feedbacks.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 8) {
                    continue;
                }

                String feedbackID = parts[0];
                String doctorID = parts[1];
                String staffID = parts[2];
                String customerID = parts[3];
                String feedback = parts[4];
                String comment = parts[5];
                String charge = parts[6];
                String appointmentID = parts[7];

                Feedback f = new Feedback(feedbackID, doctorID, staffID, customerID, feedback, comment, charge, appointmentID);
                feedbackList.add(f);

            }
        } catch (Exception e) {
            System.out.println("Error loading feedbacks: " + e.getMessage());
        }
    }

    public static void saveFeedback() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\feedbacks.txt"))) {
            for (Feedback f : feedbackList) {
                String line = String.join(",",
                        f.getFeedbackID(),
                        f.getDoctorID(),
                        f.getStaffID(),
                        f.getCustomerID(),
                        f.getFeedback(),
                        f.getComment(),
                        f.getCharge(),
                        f.getAppointmentID()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error saving feedbacks: " + e.getMessage());
        }
    }

    public static void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);
    }
}
