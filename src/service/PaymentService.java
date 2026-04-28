package service;

import model.Payment;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PaymentService {

   
    private static final String ABS_DIR = "C:\\Users\\ASUS\\Desktop\\AMC\\src\\data";
    private static final String FILE_NAME = "payments.txt";

    public static final List<Payment> paymentList = new ArrayList<>();

    private static File paymentsFile() {
        return new File(ABS_DIR, FILE_NAME);
    }

   
    public static void loadPayments() {
        paymentList.clear();

        File file = paymentsFile();
        if (!file.exists() || !file.isFile()) {
            JOptionPane.showMessageDialog(null,
                    "Cannot open " + file.getPath() + " (file not found).");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

              
                String[] d = line.split(",", -1);
                if (d.length < 4) continue;

                String paymentId     = d[0].trim();
                String appointmentId = d[1].trim();
                String amountRaw     = d[2].trim();
                String method        = d[3].trim();

                double amount = parseAmount(amountRaw);
                if (Double.isNaN(amount)) continue; 
               
                paymentList.add(new Payment(paymentId, appointmentId, amount, method));

               
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error reading " + file.getPath() + ": " + ex.getMessage());
        }
    }

   
    public static void savePaymentsToFile() {
        File file = paymentsFile();
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            for (Payment p : paymentList) {
              
                String row = String.join(",",
                        p.getPaymentID(),
                        p.getAppointmentID(),
                        String.format("%.2f", p.getAmount()),
                        p.getMethod()
                );
               
                bw.write(row);
                bw.newLine();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error writing " + file.getPath() + ": " + ex.getMessage());
        }
    }

  
    public static void addPayment(Payment p) {
        paymentList.add(p);
    }

   
    public static List<Payment> getPayments() {
        if (paymentList.isEmpty()) loadPayments();
        return paymentList;
    }

 
    public static double getTotalRevenue() {
        double sum = 0.0;
        for (Payment p : getPayments()) {
            sum += p.getAmount(); 
          
        }
        return sum;
    }

    public static int getTotalPayments() {
        return getPayments().size();
    }

    public static List<Payment> getAll() {
        return new ArrayList<>(getPayments());
    }

 
    private static double parseAmount(String s) {
        if (s == null) return Double.NaN;
        String cleaned = s.replaceAll("[^0-9.\\-]", "");
        try { return Double.parseDouble(cleaned); }
        catch (Exception e) { return Double.NaN; }
    }
}
