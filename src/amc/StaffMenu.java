package amc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalTime;
import validation.UserRules;
import validation.ValidationResult;



import model.*;
import service.*;

public class StaffMenu extends javax.swing.JFrame {
    
    private final String staffUsername;

    public StaffMenu(User staff) {
        this.staffUsername = staff.getUsername();
        initComponents();
        loadPaymentTable();
        WindowPositioner.apply(this);     
        UserService.loadAllUsersFromRoles(); 
        loadPaymentTable();
        util.InactivityManager.attach(this, () -> {
     
        this.dispose();
        new amc.LoginPage().setVisible(true);
    });

        
        for (User user : UserService.userList) {
            if ("Customer".equals(user.getRole())) {
                customerList.addItem(user.getUsername());
            }
        }
        
        for (User user : UserService.userList) {
            if ("Doctor".equals(user.getRole())) {
                doctorList.addItem(user.getUsername());
            }
        }

        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (int i = 0; i < 7; i++) {
            String formattedDate = today.plusDays(i).format(formatter);
            dateList.addItem(formattedDate);
        }

       
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        while (!startTime.isAfter(endTime)) {
            timeList.addItem(startTime.toString());
            startTime = startTime.plusMinutes(30);
        }

    }

    private void createCustomer() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String gender = genderBox.getSelectedItem().toString();
        String email = emailField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String dateOfBirth = dateOfBirthField.getText().trim();
        String role = "Customer"; 

        User newUser = new Customer(username, password, name, gender, email, phoneNumber, dateOfBirth);

        ValidationResult vr = UserRules.validateCreate(newUser);

        if (!vr.isOk()) {
        JOptionPane.showMessageDialog(this, String.join("\n", vr.getErrors()));
        return;
        }


        if (UserService.addUser(newUser)) {
            JOptionPane.showMessageDialog(this, "Customer with ID (" + username + ") and name (" + name + ") has been created successfully!");
            clearFields();
            loadCustomerTable();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists!");
        }
    }

    private void deleteCustomer() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

       
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

      
        String username = accountTable.getValueAt(selectedRow, 0).toString();

    
        boolean deleted = UserService.deleteUser(username);

        if (deleted) {
            
            loadCustomerTable();

           
            clearFields();

            JOptionPane.showMessageDialog(this, "User deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "User not found or failed to delete.");
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneNumberField.setText("");
        dateOfBirthField.setText("");
        genderBox.setSelectedIndex(0);
    }

    private void clearAppointmentFields() {
        customerList.setSelectedIndex(0);
        customerNameField.setText("");
        doctorList.setSelectedIndex(0);
        doctorNameField.setText("");
        dateList.setSelectedIndex(0);
        timeList.setSelectedIndex(0);
        statusField.setText("Booked"); 
    }

    private void loadAppointmentTable() {
        AppointmentService.getAppointment();
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        for (Appointment appointment : AppointmentService.appointmentList) {
           
            String customerName = "";
            User customer = UserService.searchUser(appointment.getCustomerUsername());
            if (customer != null) {
                customerName = customer.getName();
            }

           
            String doctorName = "";
            User doctor = UserService.searchUser(appointment.getDoctorUsername());
            if (doctor != null) {
                doctorName = doctor.getName();
            }

            Object[] row = {
                appointment.getAppointmentID(),
                appointment.getCustomerUsername(),
                customerName,
                appointment.getDoctorUsername(),
                doctorName,
                appointment.getDate(),
                appointment.getTime(),
                appointment.getStatus(),
                appointment.getBookedBy()
            };
            model.addRow(row);
        }
    }

    private void loadCustomerTable() {
        UserService.loadAllUsersFromRoles();

        DefaultTableModel model = (DefaultTableModel) accountTable.getModel();
        model.setRowCount(0);

        for (User user : UserService.userList) {
            if ("Customer".equals(user.getRole())) {
                Object[] row = {
                    user.getUsername(),
                    user.getPassword(),
                    user.getName(),
                    user.getRole(),
                    user.getGender(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getDateOfBirth()
                };
                model.addRow(row);
            }
        }
    }

    private void loadPaymentTable() {
        AppointmentService.getAppointment();
        FeedbackService.getFeedbacks();

        DefaultTableModel model = (DefaultTableModel) paymentTable.getModel();
        model.setRowCount(0); 

        for (Appointment a : AppointmentService.appointmentList) {
            
            String charge = "";
            for (Feedback f : FeedbackService.feedbackList) {
                if (f.getAppointmentID().equals(a.getAppointmentID())) {
                    charge = f.getCharge();
                    break;
                }
            }

            Object[] row = {
                a.getAppointmentID(),
                a.getCustomerUsername(),
                a.getDoctorUsername(),
                charge
            };
            model.addRow(row);
        }
    }

    private void fillEditFields() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String username = accountTable.getValueAt(selectedRow, 0).toString();
        String password = accountTable.getValueAt(selectedRow, 1).toString();
        String email = accountTable.getValueAt(selectedRow, 5).toString();
        String phone = accountTable.getValueAt(selectedRow, 6).toString();

        usernameField2.setText(username);
        passwordField2.setText(password);
        emailField2.setText(email);
        phoneNumberField2.setText(phone);
    }

    private void saveUserChanges() {
        String username = usernameField2.getText().trim();
        String password = new String(passwordField2.getPassword()).trim();
        String email = emailField2.getText().trim();
        String phoneNumber = phoneNumberField2.getText().trim();

       User existingUser = UserService.searchUser(username);
        if (existingUser == null) {
        JOptionPane.showMessageDialog(this, "User not found!");
         return;
        }

        existingUser.setPassword(password);
        existingUser.setEmail(email);
        existingUser.setPhoneNumber(phoneNumber);

        ValidationResult vr = UserRules.validateUpdate(existingUser);
        if (!vr.isOk()) {
            JOptionPane.showMessageDialog(this, String.join("\n", vr.getErrors()));
            return;
        }

        if (UserService.updateUser(existingUser)) {
            JOptionPane.showMessageDialog(this, "User with ID (" + username + ") updated successfully!");
            loadCustomerTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user.");
        }
    }

    private String generateAppointmentID() {
        AppointmentService.getAppointment();
        int nextId = AppointmentService.appointmentList.size() + 1;
        return "A" + String.format("%03d", nextId); 
    }

    private void createAppointment() {
        String appointmentID = appointmentIDField.getText().trim();
        String customerUsername = (String) customerList.getSelectedItem();
        String doctorUsername = (String) doctorList.getSelectedItem();
        String date = (String) dateList.getSelectedItem();
        String time = (String) timeList.getSelectedItem();
        String status = statusField.getText();
        String bookedBy = bookedByField.getText();

      
        if (customerUsername == null || customerUsername.isEmpty()
                || doctorUsername == null || doctorUsername.isEmpty()
                || date == null || date.isEmpty()
                || time == null || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        Appointment newAppointment = new Appointment(
                appointmentID,
                customerUsername,
                doctorUsername,
                date,
                time,
                status,
                bookedBy
        );

        AppointmentService.addAppointment(newAppointment);
        AppointmentService.saveAppointment();

        JOptionPane.showMessageDialog(this, "Appointment " + appointmentID + " booked successfully by " + bookedBy + "!");

        clearAppointmentFields();
        appointmentIDField.setText(generateAppointmentID());

    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.");
            return;
        }

        String appointmentID = appointmentTable.getValueAt(selectedRow, 0).toString();

        
        Appointment appt = AppointmentService.searchAppointment(appointmentID);
        if (appt == null) {
            JOptionPane.showMessageDialog(this, "Appointment not found.");
            return;
        }

       
        if (!"Booked".equals(appt.getStatus())) {
            JOptionPane.showMessageDialog(this, "This appointment is already cancelled or completed.");
            return;
        }

       
        appt.setStatus("Cancelled");
        AppointmentService.saveAppointment();

        JOptionPane.showMessageDialog(this, "Appointment " + appointmentID + " has been cancelled.");
        loadAppointmentTable();
    }

    private void processPayment() {
    int selectedRow = paymentTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select an appointment first.");
        return;
    }

    String appointmentID = paymentTable.getValueAt(selectedRow, 0).toString();
    String amountText = amountField.getText().trim();         
    String paymentMethod = paymentMethodList.getSelectedItem() == null
            ? "" : paymentMethodList.getSelectedItem().toString();

    if (amountText.isEmpty() || paymentMethod.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter payment amount and select a method.");
        return;
    }

    if (!amountText.matches("\\d+(\\.\\d{1,2})?")) {
        JOptionPane.showMessageDialog(this, "Amount must be a numeric value.");
        return;
    }

    
    double amountValue;
    try {
        amountValue = Double.parseDouble(amountText);
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Amount must be a numeric value.");
        return;
    }

    PaymentService.loadPayments(); // or PaymentService.getPayments(); if you prefer

    
    for (model.Payment p : PaymentService.paymentList) {
        if (p.getAppointmentID().equals(appointmentID)) {
            JOptionPane.showMessageDialog(this, "Payment already processed for this appointment.");
            return;
        }
    }

    String paymentID = "P" + String.format("%03d", PaymentService.paymentList.size() + 1);

   
    model.Payment newPayment = new model.Payment(paymentID, appointmentID, amountValue, paymentMethod);
    PaymentService.addPayment(newPayment);
    PaymentService.savePaymentsToFile();

    JOptionPane.showMessageDialog(this, "Payment processed successfully!");

    amountField.setText("");
    appointmentIDField.setText("");
}


   
    private void generateReceipt() {
    int selectedRow = paymentTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select an appointment first.");
        return;
    }

    String appointmentID = paymentTable.getValueAt(selectedRow, 0).toString();
    String customerID = paymentTable.getValueAt(selectedRow, 1).toString();
    String doctorID = paymentTable.getValueAt(selectedRow, 2).toString();
    String charge = paymentTable.getValueAt(selectedRow, 3).toString();

   
    String customerName = "";
    String doctorName = "";

    User c = UserService.searchUser(customerID);
    User d = UserService.searchUser(doctorID);
    if (c != null) customerName = c.getName();
    if (d != null) doctorName = d.getName();

    
    String receipt = "----- APU Medical Centre Receipt -----\n"
            + "Customer Name  : " + customerName + "\n"
            + "Doctor Name    : " + doctorName + "\n"
            + "Appointment ID : " + appointmentID + "\n"
            + "Amount Paid    : RM " + charge + "\n"
            + "Payment Method : " + paymentMethodList.getSelectedItem() + "\n"
            + "Date           : " + java.time.LocalDate.now() + "\n"
            + "----------------------------------------\n"
            + "Thank you for choosing our service!\n";

    JTextArea textArea = new JTextArea(receipt);
    textArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setPreferredSize(new Dimension(400, 220));

    
    JButton printButton = new JButton("Print Receipt");

   

    printButton.addActionListener(e -> {
        try {
            boolean done = textArea.print(); 
            if (done) {
                JOptionPane.showMessageDialog(this, "Receipt sent to printer.");
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.");
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing error: " + ex.getMessage());
        }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout());
    //buttonPanel.add(downloadButton);
    buttonPanel.add(printButton);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(buttonPanel, BorderLayout.SOUTH);

    JOptionPane.showMessageDialog(this, panel, "Receipt", JOptionPane.PLAIN_MESSAGE);
}



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        logoutButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        dateOfBirthField = new javax.swing.JTextField();
        phoneNumberField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        createButton = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();
        genderBox = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        usernameField2 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        passwordField2 = new javax.swing.JPasswordField();
        jLabel15 = new javax.swing.JLabel();
        emailField2 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        phoneNumberField2 = new javax.swing.JTextField();
        refreshAccountButton = new javax.swing.JButton();
        deleteAccountButton = new javax.swing.JButton();
        saveAccountButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        appointmentIDField = new javax.swing.JTextField();
        customerNameField = new javax.swing.JTextField();
        statusField = new javax.swing.JTextField();
        doctorNameField = new javax.swing.JTextField();
        bookedByField = new javax.swing.JTextField();
        doctorList = new javax.swing.JComboBox<>();
        customerList = new javax.swing.JComboBox<>();
        dateList = new javax.swing.JComboBox<>();
        timeList = new javax.swing.JComboBox<>();
        createAppointmentButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        jLabel24 = new javax.swing.JLabel();
        refreshApptButton = new javax.swing.JButton();
        cancelApptButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        paymentTable = new javax.swing.JTable();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        appointmentField = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        amountField = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        paymentMethodList = new javax.swing.JComboBox<>();
        processPaymentButton = new javax.swing.JButton();
        generateReceiptButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Staff Menu");

        logoutButton.setText("Logout");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Username:");

        usernameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Password:");

        nameField.setToolTipText("");
        nameField.setName("Name"); // NOI18N
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("Name:");

        dateOfBirthField.setName(""); // NOI18N
        dateOfBirthField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateOfBirthFieldActionPerformed(evt);
            }
        });

        phoneNumberField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Phone Number:");

        jLabel8.setText("Date of Birth:");

        jLabel9.setText("Email:");

        jLabel10.setText("Gender:");

        emailField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                emailFieldActionPerformed(evt);
            }
        });

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        genderBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Create New Customer");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(passwordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(usernameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(phoneNumberField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dateOfBirthField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel9))
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addGap(101, 101, 101)
                                        .addComponent(genderBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(165, 165, 165)
                        .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(144, 144, 144))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(genderBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(phoneNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(dateOfBirthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62)
                .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(255, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(325, 325, 325))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Create New Customer", jPanel2);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Customer Management");

        accountTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Username", "Password", "Name", "Role", "Gender", "Email", "Phone Number", "Age"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        accountTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                accountTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(accountTable);

        jLabel12.setText("Username:");

        jLabel13.setText("New Password:");

        jLabel15.setText("New Email:");

        jLabel16.setText("New Phone Number:");

        refreshAccountButton.setText("Refresh");
        refreshAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAccountButtonActionPerformed(evt);
            }
        });

        deleteAccountButton.setText("Delete User");
        deleteAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAccountButtonActionPerformed(evt);
            }
        });

        saveAccountButton.setText("Save Changes");
        saveAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAccountButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 696, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel12)
                                    .addComponent(passwordField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(usernameField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel16)
                                        .addComponent(phoneNumberField2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel15)
                                            .addComponent(emailField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(131, 131, 131)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(refreshAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(saveAccountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(deleteAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(244, 244, 244)
                        .addComponent(jLabel6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(5, 5, 5)
                        .addComponent(usernameField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passwordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel15)
                        .addGap(4, 4, 4)
                        .addComponent(emailField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phoneNumberField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshAccountButton)
                        .addGap(18, 18, 18)
                        .addComponent(deleteAccountButton)
                        .addGap(18, 18, 18)
                        .addComponent(saveAccountButton)
                        .addGap(45, 45, 45))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(107, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Customer Management", jPanel6);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel11.setText("Book Appointment");

        jLabel14.setText("Appointment ID:");

        jLabel17.setText("Customer Username:");

        jLabel18.setText("Customer Name:");

        jLabel19.setText("Doctor Username:");

        jLabel20.setText("Doctor Name:");

        jLabel21.setText("Date:");

        jLabel22.setText("Time:");

        jLabel23.setText("Status:");

        jLabel25.setText("Booked By:");

        appointmentIDField.setEditable(false);
        appointmentIDField.setText(generateAppointmentID());
        appointmentIDField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appointmentIDFieldActionPerformed(evt);
            }
        });

        customerNameField.setEditable(false);

        statusField.setEditable(false);
        statusField.setText("Booked");

        doctorNameField.setEditable(false);

        bookedByField.setEditable(false);
        bookedByField.setText(this.staffUsername);
        bookedByField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookedByFieldActionPerformed(evt);
            }
        });

        doctorList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doctorListActionPerformed(evt);
            }
        });

        customerList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerListActionPerformed(evt);
            }
        });

        createAppointmentButton.setText("Create Appointment");
        createAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAppointmentButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 381, Short.MAX_VALUE)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel17)
                                .addComponent(jLabel14)
                                .addComponent(jLabel18)
                                .addComponent(jLabel19)
                                .addComponent(jLabel20)
                                .addComponent(jLabel21)
                                .addComponent(jLabel22)
                                .addComponent(jLabel23)
                                .addComponent(jLabel25))
                            .addGap(59, 59, 59)
                            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(appointmentIDField)
                                .addComponent(customerNameField)
                                .addComponent(statusField)
                                .addComponent(doctorNameField)
                                .addComponent(bookedByField)
                                .addComponent(doctorList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(customerList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(dateList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(timeList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addGap(104, 104, 104)
                            .addComponent(createAppointmentButton))
                        .addGroup(jPanel9Layout.createSequentialGroup()
                            .addGap(100, 100, 100)
                            .addComponent(jLabel11)))
                    .addContainerGap(11, Short.MAX_VALUE)))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
            .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel9Layout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(jLabel11)
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel14)
                        .addComponent(appointmentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel17)
                        .addComponent(customerList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(customerNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel19)
                        .addComponent(doctorList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel20)
                        .addComponent(doctorNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel21)
                        .addComponent(dateList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel22)
                        .addComponent(timeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel23)
                        .addComponent(statusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel25)
                        .addComponent(bookedByField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addComponent(createAppointmentButton)
                    .addContainerGap(8, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(338, Short.MAX_VALUE)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(333, 333, 333))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(70, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Book Appointment", jPanel3);

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Customer Username", "Customer Name", "Doctor Username", "Doctor Name", "Date", "Time", "Status", "Booked By"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(appointmentTable);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel24.setText("View Appointments");

        refreshApptButton.setText("Refresh");
        refreshApptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshApptButtonActionPerformed(evt);
            }
        });

        cancelApptButton.setText("Cancel Appointment");
        cancelApptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelApptButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(426, 426, 426)
                        .addComponent(jLabel24))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 924, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(317, 317, 317)
                        .addComponent(refreshApptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(cancelApptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(67, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel24)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 484, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshApptButton)
                    .addComponent(cancelApptButton))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Cancel Appointment", jPanel8);

        paymentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Customer ID", "Doctor ID", "Charged Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        paymentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                paymentTableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(paymentTable);

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel26.setText("Process Payment");

        jLabel27.setText("Selected Appointment:");

        appointmentField.setEditable(false);
        appointmentField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appointmentFieldActionPerformed(evt);
            }
        });

        jLabel28.setText("Paid Amount:");

        jLabel29.setText("Payment Method:");

        paymentMethodList.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "Online Transfer", "QRPay", "Credit/Debit Card" }));
        paymentMethodList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paymentMethodListActionPerformed(evt);
            }
        });

        processPaymentButton.setText("Process Payment");
        processPaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processPaymentButtonActionPerformed(evt);
            }
        });

        generateReceiptButton.setText("Generate Receipt");
        generateReceiptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReceiptButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 563, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(79, 79, 79)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel27)
                                    .addComponent(appointmentField)
                                    .addComponent(jLabel28)
                                    .addComponent(amountField, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                    .addComponent(jLabel29)
                                    .addComponent(paymentMethodList, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(123, 123, 123)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(generateReceiptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(processPaymentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(245, 245, 245)
                        .addComponent(jLabel26)))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel26)
                .addGap(29, 29, 29)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(appointmentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amountField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paymentMethodList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(processPaymentButton)
                        .addGap(18, 18, 18)
                        .addComponent(generateReceiptButton)))
                .addContainerGap(95, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Process Payment", jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logoutButton)
                .addContainerGap())
            .addComponent(jTabbedPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(logoutButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        new LoginPage().setVisible(true);
        dispose();
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void cancelApptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelApptButtonActionPerformed
        cancelAppointment();
    }//GEN-LAST:event_cancelApptButtonActionPerformed

    private void refreshApptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshApptButtonActionPerformed
        loadAppointmentTable();
    }//GEN-LAST:event_refreshApptButtonActionPerformed

    private void createAppointmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createAppointmentButtonActionPerformed
        createAppointment();
    }//GEN-LAST:event_createAppointmentButtonActionPerformed

    private void customerListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerListActionPerformed
        String selectedUsername = (String) customerList.getSelectedItem();
        User selectedUser = UserService.searchUser(selectedUsername);
        if (selectedUser != null) {
            customerNameField.setText(selectedUser.getName());
        } else {
            customerNameField.setText("");
        }
    }//GEN-LAST:event_customerListActionPerformed

    private void doctorListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doctorListActionPerformed
        String selectedDoctorUsername = (String) doctorList.getSelectedItem();
        User selectedDoctor = UserService.searchUser(selectedDoctorUsername);
        if (selectedDoctor != null) {
            doctorNameField.setText(selectedDoctor.getName());
        } else {
            doctorNameField.setText("");
        }

        String selectedDate = (String) dateList.getSelectedItem();

        // Refresh time list
        timeList.removeAllItems();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        List<String> allTimes = new ArrayList<>();
        while (!startTime.isAfter(endTime)) {
            allTimes.add(startTime.toString());
            startTime = startTime.plusMinutes(30);
        }

        // Remove already booked times
        AppointmentService.getAppointment();
        for (Appointment appt : AppointmentService.appointmentList) {
            if (appt.getDoctorUsername().equals(selectedDoctorUsername) && appt.getDate().equals(selectedDate)) {
                allTimes.remove(appt.getTime());
            }
        }

        for (String t : allTimes) {
            timeList.addItem(t);
        }
    }//GEN-LAST:event_doctorListActionPerformed

    private void bookedByFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookedByFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bookedByFieldActionPerformed

    private void saveAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAccountButtonActionPerformed
        saveUserChanges();
    }//GEN-LAST:event_saveAccountButtonActionPerformed

    private void deleteAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAccountButtonActionPerformed
        deleteCustomer();
    }//GEN-LAST:event_deleteAccountButtonActionPerformed

    private void refreshAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAccountButtonActionPerformed
        loadCustomerTable();
    }//GEN-LAST:event_refreshAccountButtonActionPerformed

    private void accountTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_accountTableMouseClicked
        fillEditFields();
    }//GEN-LAST:event_accountTableMouseClicked

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        createCustomer();
    }//GEN-LAST:event_createButtonActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_emailFieldActionPerformed

    private void phoneNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phoneNumberFieldActionPerformed

    private void dateOfBirthFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateOfBirthFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateOfBirthFieldActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameFieldActionPerformed

    private void paymentMethodListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paymentMethodListActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_paymentMethodListActionPerformed

    private void processPaymentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processPaymentButtonActionPerformed
        processPayment();
    }//GEN-LAST:event_processPaymentButtonActionPerformed

    private void paymentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_paymentTableMouseClicked
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow != -1) {
            Object value = paymentTable.getValueAt(selectedRow, 0);
            if (value != null) {
                appointmentField.setText(value.toString());
            }
        }
    }//GEN-LAST:event_paymentTableMouseClicked

    private void generateReceiptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateReceiptButtonActionPerformed
        generateReceipt();
    }//GEN-LAST:event_generateReceiptButtonActionPerformed

    private void appointmentFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentFieldActionPerformed


    }//GEN-LAST:event_appointmentFieldActionPerformed

    private void appointmentIDFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appointmentIDFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_appointmentIDFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable accountTable;
    private javax.swing.JTextField amountField;
    private javax.swing.JTextField appointmentField;
    private javax.swing.JTextField appointmentIDField;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JTextField bookedByField;
    private javax.swing.JButton cancelApptButton;
    private javax.swing.JButton createAppointmentButton;
    private javax.swing.JButton createButton;
    private javax.swing.JComboBox<String> customerList;
    private javax.swing.JTextField customerNameField;
    private javax.swing.JComboBox<String> dateList;
    private javax.swing.JTextField dateOfBirthField;
    private javax.swing.JButton deleteAccountButton;
    private javax.swing.JComboBox<String> doctorList;
    private javax.swing.JTextField doctorNameField;
    private javax.swing.JTextField emailField;
    private javax.swing.JTextField emailField2;
    private javax.swing.JComboBox<String> genderBox;
    private javax.swing.JButton generateReceiptButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton logoutButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JPasswordField passwordField2;
    private javax.swing.JComboBox<String> paymentMethodList;
    private javax.swing.JTable paymentTable;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JTextField phoneNumberField2;
    private javax.swing.JButton processPaymentButton;
    private javax.swing.JButton refreshAccountButton;
    private javax.swing.JButton refreshApptButton;
    private javax.swing.JButton saveAccountButton;
    private javax.swing.JTextField statusField;
    private javax.swing.JComboBox<String> timeList;
    private javax.swing.JTextField usernameField;
    private javax.swing.JTextField usernameField2;
    // End of variables declaration//GEN-END:variables
}
