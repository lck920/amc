package amc;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import service.*;
import model.*;

public class CustomerMenu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CustomerMenu.class.getName());
    private User customer;

    
    public CustomerMenu(User customer) {
    initComponents();
    this.customer = customer;
    loadCustomerProfile(this.customer);   
    loadAppointments(customer.getUsername());
    WindowPositioner.apply(this);
       
    util.InactivityManager.attach(this, () -> {
        
        this.dispose();
        new amc.LoginPage().setVisible(true);
    });
}

    private void loadCustomerProfile(User customer) {
        usernameField.setText(customer.getUsername());
        nameField.setText(customer.getName());
        genderField.setText(customer.getGender());
        dateOfBirthField.setText(customer.getDateOfBirth());
        ageField.setText(String.valueOf(customer.getAge()));
        passwordField.setText(customer.getPassword());
        confirmPasswordField.setText(customer.getPassword());
        emailField.setText(customer.getEmail());
        phoneNumberField.setText(customer.getPhoneNumber());
    }

    private void saveProfileChanges() {
    String username        = usernameField.getText().trim();
    String newPassword     = new String(passwordField.getPassword()).trim();
    String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
    String email           = emailField.getText().trim();
    String phoneNumber     = phoneNumberField.getText().trim();

    
    if (username.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username is missing. Please check your profile info.");
        return;
    }
    if (email.isEmpty() || phoneNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill in Email and Phone Number.");
        return;
    }
    
    boolean wantsPasswordChange = !newPassword.isEmpty() || !confirmPassword.isEmpty();
    if (wantsPasswordChange && !newPassword.equals(confirmPassword)) {
        JOptionPane.showMessageDialog(this, "Passwords do not match.");
        return;
    }

  
    User existingCustomer = UserService.searchUser(username);
    if (existingCustomer == null) {
        JOptionPane.showMessageDialog(this, "Customer not found!");
        return;
    }

    
    if (wantsPasswordChange) {
        existingCustomer.setPassword(newPassword);
    }
    existingCustomer.setEmail(email);
    existingCustomer.setPhoneNumber(phoneNumber);

    
    try {
        boolean ok = UserService.updateUser(existingCustomer);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            loadCustomerProfile(existingCustomer); // refresh fields with saved values
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update Customer profile.");
        }
    } catch (IllegalArgumentException ex) {
        
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
    } catch (IllegalStateException ex) {
        
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Business Rule", JOptionPane.WARNING_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}


    private void loadAppointments(String customerUsername) {
        AppointmentService.getAppointment();
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        for (Appointment appt : AppointmentService.appointmentList) {
            if (appt.getCustomerUsername().equals(customerUsername)) {
                String doctorName = "";
                User doctor = UserService.searchUser(appt.getDoctorUsername());
                if (doctor != null) {
                    doctorName = doctor.getName();
                }

                Object[] row = {
                    appt.getAppointmentID(),
                    appt.getDoctorUsername(),
                    doctorName,
                    appt.getDate(),
                    appt.getTime(),
                    appt.getStatus(),
                    appt.getBookedBy()
                };
                model.addRow(row);
            }
        }
    }

    private void submitComment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        String appointmentID = appointmentTable.getValueAt(selectedRow, 0).toString();
        String doctorID = appointmentTable.getValueAt(selectedRow, 1).toString();
        String staffID = appointmentTable.getValueAt(selectedRow, 6).toString();
        String commentText = commentField.getText().trim();

        if (commentText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your comment.");
            return;
        }

        FeedbackService.getFeedbacks();

        boolean found = false;
        for (Feedback f : FeedbackService.feedbackList) {
            if (f.getAppointmentID().equals(appointmentID)
                    && f.getDoctorID().equals(doctorID)
                    && f.getStaffID().equals(staffID)
                    && f.getCustomerID().equals(customer.getUsername())) {

                if (f.getComment() != null && !f.getComment().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "You have already provided a comment for this appointment.");
                    return;
                }

                // Update existing feedback
                f.setComment(commentText);
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, "Doctor feedback not found yet. You can only comment after doctor feedback is created.");
            return;
        }

        FeedbackService.saveFeedback();
        JOptionPane.showMessageDialog(this, "Comment updated successfully!");

        commentField.setText("");
        appointmentIDField.setText("");
        doctorUsernameField.setText("");
        staffUsernameField.setText("");
    }

    private void loadCustomerHistories() {
        FeedbackService.getFeedbacks();
        AppointmentService.getAppointment();
        UserService.loadAllUsersFromRoles(); 
 

        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        String customerID = customer.getUsername();

        for (Appointment a : AppointmentService.appointmentList) {
            if (a.getCustomerUsername().equals(customerID)) {
                String doctorID = a.getDoctorUsername();
                String doctorName = "";

               
                for (User u : UserService.userList) {
                    if (u.getUsername().equals(doctorID) && u.getRole().equals("Doctor")) {
                        doctorName = u.getName();
                        break;
                    }
                }

                String feedback = "";
                String comment = "";
                String charge = "";

                for (Feedback f : FeedbackService.feedbackList) {
                    if (f.getAppointmentID().equals(a.getAppointmentID())) {
                        feedback = f.getFeedback();
                        comment = f.getComment();
                        charge = f.getCharge();
                        break;
                    }
                }

                Object[] row = {
                    a.getAppointmentID(),
                    doctorID,
                    doctorName,
                    comment,
                    feedback,
                    charge
                };
                model.addRow(row);
            }
        }
    }

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        logoutButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        nameField = new javax.swing.JTextField();
        genderField = new javax.swing.JTextField();
        emailField = new javax.swing.JTextField();
        phoneNumberField = new javax.swing.JTextField();
        ageField = new javax.swing.JTextField();
        dateOfBirthField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        refreshAccountButton = new javax.swing.JButton();
        saveAccountButton = new javax.swing.JButton();
        passwordField = new javax.swing.JPasswordField();
        confirmPasswordField = new javax.swing.JPasswordField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        refreshApptButton = new javax.swing.JButton();
        submitCommentButton = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        appointmentIDField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        doctorUsernameField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        staffUsernameField = new javax.swing.JTextField();
        commentField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AMC - Customer Menu");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Customer Menu");

        logoutButton.setText("Logout");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Edit My Profile");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        jLabel5.setText("Name:");

        jLabel6.setText("Gender:");

        jLabel7.setText("Email:");

        jLabel8.setText("Phone Number:");

        jLabel9.setText("Date Of Birth:");

        jLabel10.setText("Age:");

        usernameField.setEditable(false);

        nameField.setEditable(false);

        genderField.setEditable(false);

        ageField.setEditable(false);

        dateOfBirthField.setEditable(false);

        jLabel11.setText("Confirm Password:");

        refreshAccountButton.setText("Refresh");
        refreshAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshAccountButtonActionPerformed(evt);
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
                .addGap(336, 336, 336)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(62, 62, 62))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(usernameField)
                                .addComponent(nameField)
                                .addComponent(genderField)
                                .addComponent(emailField)
                                .addComponent(phoneNumberField)
                                .addComponent(jLabel4)
                                .addComponent(jLabel3)
                                .addComponent(jLabel5)
                                .addComponent(jLabel6)
                                .addComponent(jLabel7)
                                .addComponent(jLabel8)
                                .addComponent(jLabel11)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel9)
                                        .addComponent(dateOfBirthField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(34, 34, 34)
                                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(ageField)
                                        .addGroup(jPanel6Layout.createSequentialGroup()
                                            .addComponent(jLabel10)
                                            .addGap(0, 0, Short.MAX_VALUE))))
                                .addComponent(confirmPasswordField)
                                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(refreshAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(135, 135, 135)))
                        .addGap(20, 20, 20))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(155, 155, 155)
                        .addComponent(saveAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(373, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confirmPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(phoneNumberField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateOfBirthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshAccountButton)
                    .addComponent(saveAccountButton))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Edit My Profile", jPanel6);

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Doctor Username", "Doctor Name", "Date", "Time", "Status", "Booked By"
            }
        ));
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                appointmentTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(appointmentTable);

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setText("View Appointments");

        refreshApptButton.setText("Refresh");
        refreshApptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshApptButtonActionPerformed(evt);
            }
        });

        submitCommentButton.setText("Submit Comment");
        submitCommentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitCommentButtonActionPerformed(evt);
            }
        });

        jLabel15.setText("Selected Appointment:");

        appointmentIDField.setEditable(false);

        jLabel16.setText("Doctor Username:");

        doctorUsernameField.setEditable(false);

        jLabel17.setText("Staff Username:");

        staffUsernameField.setEditable(false);

        jLabel18.setText("Comment:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(210, 210, 210)
                .addComponent(jLabel12)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(doctorUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(appointmentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17)
                    .addComponent(staffUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(commentField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(77, 77, 77)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(refreshApptButton, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(submitCommentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(55, 55, 55))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel12)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(appointmentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doctorUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(staffUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(commentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(refreshApptButton)
                        .addGap(18, 18, 18)
                        .addComponent(submitCommentButton)))
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("View Appointments", jPanel3);

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setText("My History");

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Doctor ID", "Doctor Name", "Feedback", "Comment", "Charged Amount"
            }
        ));
        jScrollPane3.setViewportView(historyTable);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 85, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(80, 80, 80))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(417, 417, 417)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(432, 432, 432)
                        .addComponent(jLabel13)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(35, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshButton)
                .addGap(23, 23, 23))
        );

        jTabbedPane1.addTab("My History", jPanel5);

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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        new LoginPage().setVisible(true);
        dispose();
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void saveAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAccountButtonActionPerformed
        saveProfileChanges();
    }//GEN-LAST:event_saveAccountButtonActionPerformed

    private void refreshAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAccountButtonActionPerformed
        loadCustomerProfile(this.customer);
    }//GEN-LAST:event_refreshAccountButtonActionPerformed

    private void refreshApptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshApptButtonActionPerformed
        loadAppointments(customer.getUsername());
    }//GEN-LAST:event_refreshApptButtonActionPerformed

    private void submitCommentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitCommentButtonActionPerformed
        submitComment();
    }//GEN-LAST:event_submitCommentButtonActionPerformed

    private void appointmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_appointmentTableMouseClicked
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow != -1) {
            appointmentIDField.setText(appointmentTable.getValueAt(selectedRow, 0).toString());
            doctorUsernameField.setText(appointmentTable.getValueAt(selectedRow, 1).toString());
            staffUsernameField.setText(appointmentTable.getValueAt(selectedRow, 6).toString());
        }
    }//GEN-LAST:event_appointmentTableMouseClicked

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadCustomerHistories();
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ageField;
    private javax.swing.JTextField appointmentIDField;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JTextField commentField;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JTextField dateOfBirthField;
    private javax.swing.JTextField doctorUsernameField;
    private javax.swing.JTextField emailField;
    private javax.swing.JTextField genderField;
    private javax.swing.JTable historyTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton logoutButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JButton refreshAccountButton;
    private javax.swing.JButton refreshApptButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveAccountButton;
    private javax.swing.JTextField staffUsernameField;
    private javax.swing.JButton submitCommentButton;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
