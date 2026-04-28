package amc;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import service.*;
import model.*;
import validation.UserRules;
import validation.ValidationResult;

/**
 *
 * @author chunk
 */
public class DoctorMenu extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DoctorMenu.class.getName());
    private User doctor;

   
    public DoctorMenu(User doctor) {
        initComponents();
        WindowPositioner.apply(this);
        this.doctor = doctor;
        loadAppointments(doctor.getUsername()); 
        loadDoctorProfile(this.doctor);
        util.InactivityManager.attach(this, () -> {
        
        this.dispose();
        new amc.LoginPage().setVisible(true);
    });
    }

    private void loadDoctorProfile(User doctor) {
        usernameField.setText(doctor.getUsername());
        nameField.setText(doctor.getName());
        genderField.setText(doctor.getGender());
        dateOfBirthField.setText(doctor.getDateOfBirth());
        ageField.setText(String.valueOf(doctor.getAge()));
        passwordField.setText(doctor.getPassword());
        confirmPasswordField.setText(doctor.getPassword());
        emailField.setText(doctor.getEmail());
        phoneNumberField.setText(doctor.getPhoneNumber());
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

    User existingDoctor = UserService.searchUser(username);
    if (existingDoctor == null) {
        JOptionPane.showMessageDialog(this, "Doctor not found!");
        return;
    }

    if (wantsPasswordChange) {
        existingDoctor.setPassword(newPassword);
    }
    existingDoctor.setEmail(email);
    existingDoctor.setPhoneNumber(phoneNumber);

    try {
        boolean ok = UserService.updateUser(existingDoctor);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            loadDoctorProfile(existingDoctor); 
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update doctor profile.");
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


    private void loadAppointments(String doctorUsername) {
        AppointmentService.getAppointment();
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        for (Appointment appt : AppointmentService.appointmentList) {
            if (appt.getDoctorUsername().equals(doctorUsername)) {
                String customerName = "";
                User customer = UserService.searchUser(appt.getCustomerUsername());
                if (customer != null) {
                    customerName = customer.getName();
                }

                Object[] row = {
                    appt.getAppointmentID(),
                    appt.getCustomerUsername(),
                    customerName,
                    appt.getDate(),
                    appt.getTime(),
                    appt.getStatus(),
                    appt.getBookedBy()
                };
                model.addRow(row);
            }
        }
    }

    private void saveDoctorFeedback() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return;
        }

        String appointmentID = appointmentTable.getValueAt(selectedRow, 0).toString();
        String customerUsername = appointmentTable.getValueAt(selectedRow, 1).toString();
        String staffUsername = appointmentTable.getValueAt(selectedRow, 6).toString();
        String status = appointmentTable.getValueAt(selectedRow, 5).toString();

        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Cannot provide feedback on a cancelled appointment.");
            return;
        }

        FeedbackService.getFeedbacks();
        for (Feedback f : FeedbackService.feedbackList) {
            if (f.getAppointmentID() != null && !f.getAppointmentID().isEmpty() && f.getAppointmentID().equals(appointmentID) && f.getFeedback() != null && !f.getFeedback().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Feedback already exists for this appointment.");
                return;
            }
        }

        String feedbackText = feedbackField.getText().trim();
        String charge = chargeField.getText().trim();

        if (feedbackText.isEmpty() || charge.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both feedback and charges.");
            return;
        }

        if (!charge.matches("\\d+(\\.\\d{1,2})?")) {
            JOptionPane.showMessageDialog(this, "Charge must be a numeric value.");
            return;
        }

        String feedbackID = "F" + String.format("%03d", FeedbackService.feedbackList.size() + 1);
        String doctorID = doctor.getUsername(); 
        String staffID = staffUsername; 
        String customerID = customerUsername;
        String comment = ""; 

        Feedback feedback = new Feedback(feedbackID, doctorID, staffID, customerID, feedbackText, comment, charge, appointmentID);
        FeedbackService.addFeedback(feedback);
        FeedbackService.saveFeedback();

        JOptionPane.showMessageDialog(this, "Charges and feedback saved successfully!");

        feedbackField.setText("");
        chargeField.setText("");
        appointmentIDField.setText("");
        customerUsernameField.setText("");
    }

    private void loadDoctorHistories() {
        FeedbackService.getFeedbacks();
        AppointmentService.getAppointment();
        UserService.loadAllUsersFromRoles(); 


        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);

        String doctorID = doctor.getUsername();

        for (Appointment a : AppointmentService.appointmentList) {
            if (a.getDoctorUsername().equals(doctorID)) {
                String customerID = a.getCustomerUsername();
                String customerName = "";

               
                for (User u : UserService.userList) {
                    if (u.getUsername().equals(customerID) && u.getRole().equals("Customer")) {
                        customerName = u.getName();
                        break;
                    }
                }

                String feedbackText = "";
                String commentText = "";
                String charge = "";

                for (Feedback f : FeedbackService.feedbackList) {
                    if (f.getAppointmentID().equals(a.getAppointmentID())) {
                        feedbackText = f.getFeedback();
                        commentText = f.getComment();
                        charge = f.getCharge();
                        break;
                    }
                }

                Object[] row = {
                    a.getAppointmentID(),
                    customerID,
                    customerName,
                    charge,
                    feedbackText,
                    commentText
                };
                model.addRow(row);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPasswordField1 = new javax.swing.JPasswordField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        logoutButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
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
        confirmPasswordField = new javax.swing.JPasswordField();
        passwordField = new javax.swing.JPasswordField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        refreshApptButton = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        feedbackField = new javax.swing.JTextField();
        chargeField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        appointmentIDField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        customerUsernameField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        JScrollPane = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        refreshButton = new javax.swing.JButton();

        jPasswordField1.setText("jPasswordField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AMC - Doctor Menu");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Doctor Menu");

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(342, 342, 342)
                        .addComponent(refreshAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(356, 356, 356)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLabel2)
                                    .addGap(54, 54, 54))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
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
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel9)
                                            .addComponent(dateOfBirthField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(34, 34, 34)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(ageField)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(jLabel10)
                                                .addGap(0, 0, Short.MAX_VALUE))))
                                    .addComponent(confirmPasswordField)
                                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(124, 124, 124)
                                .addComponent(saveAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(344, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confirmPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateOfBirthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshAccountButton)
                    .addComponent(saveAccountButton))
                .addGap(27, 27, 27))
        );

        jTabbedPane1.addTab("Edit My Profile", jPanel2);

        appointmentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Customer Username", "Customer Name", "Date", "Time", "Status", "Booked By"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                appointmentTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(appointmentTable);

        refreshApptButton.setText("Refresh");
        refreshApptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshApptButtonActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel12.setText("View Appointments");

        jLabel13.setText("Enter Charge Amount:");

        jLabel14.setText("Enter Feedback:");

        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel15.setText("Selected Appointment:");

        appointmentIDField.setEditable(false);

        jLabel16.setText("Selected Customer:");

        customerUsernameField.setEditable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(feedbackField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chargeField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(refreshApptButton)
                                .addGap(70, 70, 70)
                                .addComponent(jButton1))
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(customerUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(appointmentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(221, 221, 221)
                        .addComponent(jLabel12)))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(appointmentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customerUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chargeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(feedbackField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(refreshApptButton)
                            .addComponent(jButton1))
                        .addGap(182, 182, 182))))
        );

        jTabbedPane1.addTab("View Appointments", jPanel3);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setText("View History");

        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Appointment ID", "Customer ID", "Customer Name", "Charged Amount", "Feedback", "Comment"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        JScrollPane.setViewportView(historyTable);

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(415, 415, 415)
                        .addComponent(jLabel17))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(143, 143, 143)
                        .addComponent(JScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 651, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(401, 401, 401)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(151, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel17)
                .addGap(18, 18, 18)
                .addComponent(JScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshButton)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("View History", jPanel6);

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
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 636, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        new LoginPage().setVisible(true);
        dispose();
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        saveDoctorFeedback();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void refreshApptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshApptButtonActionPerformed
        loadAppointments(doctor.getUsername());
    }//GEN-LAST:event_refreshApptButtonActionPerformed

    private void appointmentTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_appointmentTableMouseClicked
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow != -1) {
            appointmentIDField.setText(appointmentTable.getValueAt(selectedRow, 0).toString());
            customerUsernameField.setText(appointmentTable.getValueAt(selectedRow, 1).toString());
        }
    }//GEN-LAST:event_appointmentTableMouseClicked

    private void saveAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAccountButtonActionPerformed
        saveProfileChanges();
    }//GEN-LAST:event_saveAccountButtonActionPerformed

    private void refreshAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAccountButtonActionPerformed
        loadDoctorProfile(doctor);
    }//GEN-LAST:event_refreshAccountButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadDoctorHistories();
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane JScrollPane;
    private javax.swing.JTextField ageField;
    private javax.swing.JTextField appointmentIDField;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JTextField chargeField;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JTextField customerUsernameField;
    private javax.swing.JTextField dateOfBirthField;
    private javax.swing.JTextField emailField;
    private javax.swing.JTextField feedbackField;
    private javax.swing.JTextField genderField;
    private javax.swing.JTable historyTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton logoutButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JButton refreshAccountButton;
    private javax.swing.JButton refreshApptButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveAccountButton;
    private javax.swing.JTextField usernameField;
    // End of variables declaration//GEN-END:variables
}
