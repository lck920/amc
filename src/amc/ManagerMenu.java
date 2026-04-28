package amc;

import model.*;
import service.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
import java.io.File;
import java.io.FileNotFoundException;
import validation.UserRules;
import validation.ValidationResult;


/**
 *
 * @author chunk
 */
public class ManagerMenu extends javax.swing.JFrame {

    private final User manager;
    
    /**
     * Creates new form StaffMenu
     */
   

    private void createUser() {
    String username    = usernameField.getText().trim();
    String password    = new String(passwordField.getPassword()).trim();
    String name        = nameField.getText().trim();
    String gender      = genderBox.getSelectedItem().toString();
    String email       = emailField.getText().trim();
    String phone       = phoneNumberField.getText().trim();
    String dateOfBirth = dateOfBirthField.getText().trim();
    String role        = roleBox.getSelectedItem().toString();

    // UI-level presence checks only (format checks are in UserRules)
    if (username.isEmpty() || password.isEmpty() || name.isEmpty()
            || email.isEmpty() || phone.isEmpty() || dateOfBirth.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all fields.");
        return;
        
    }

    User newUser = UserFactory.from(role, username, password, name, gender, email, phone, dateOfBirth);


    try {
        UserService.addUser(newUser); // will validate + throw if invalid/duplicate
        JOptionPane.showMessageDialog(this,
                "User with ID (" + username + ") and name (" + name + ") has been created successfully!");
        clearFields();
        loadUserTable();
    } catch (IllegalArgumentException ex) {
        // Validation errors (username/email/phone/password/DOB format etc.)
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
    } catch (IllegalStateException ex) {
        // Business rule (e.g., duplicate username)
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Business Rule", JOptionPane.WARNING_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
    
    
}

    
    private BufferedReader openData(String fileName) throws IOException {
    // 1) your absolute path (change if you move the project)
    File abs = new File("C:\\Users\\ASUS\\Desktop\\AMC\\src\\data", fileName);

    // 2) common relative locations when running from IDE/jar
    File[] candidates = new File[]{
        abs,
        new File("src\\data", fileName),
        new File("src/data", fileName),
        new File("data", fileName)
    };

    for (File f : candidates) {
        if (f.exists() && f.isFile()) {
            return new BufferedReader(new FileReader(f));
        }
    }
    throw new FileNotFoundException("Cannot open " + fileName
        + " (tried: " + candidates[0].getPath() + ", src/data, data).");
}

    private void deleteUser() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Get username from selected row
        String username = accountTable.getValueAt(selectedRow, 0).toString();

        // Remove user from list
        boolean deleted = UserService.deleteUser(username);

        if (deleted) {
            // Refresh table
            loadUserTable();

            // Clear fields
            clearFields();

            JOptionPane.showMessageDialog(this, "User deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "User not found or failed to delete.");
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
    String username    = usernameField2.getText().trim();
    String newPassword = new String(passwordField2.getPassword()).trim();
    String email       = emailField2.getText().trim();
    String phoneNumber = phoneNumberField2.getText().trim();

    if (username.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Username is missing. Please select a user from the table.");
        return;
    }
    if (email.isEmpty() || phoneNumber.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill Email and Phone fields.");
        return;
    }
    // Password change optional: only apply if non-empty
    boolean wantsPasswordChange = !newPassword.isEmpty();

    User existingUser = UserService.searchUser(username);
    if (existingUser == null) {
        JOptionPane.showMessageDialog(this, "User not found!");
        return;
    }

    if (wantsPasswordChange) {
        existingUser.setPassword(newPassword);
    }
    existingUser.setEmail(email);
    existingUser.setPhoneNumber(phoneNumber);

    try {
        boolean ok = UserService.updateUser(existingUser); // validates via UserRules
        if (ok) {
            JOptionPane.showMessageDialog(this, "User with ID (" + username + ") updated successfully!");
            loadUserTable();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update user.");
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


    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneNumberField.setText("");
        dateOfBirthField.setText("");
        genderBox.setSelectedIndex(0);
        roleBox.setSelectedIndex(0);
    }

    private void loadUserTable() {
        UserService.loadAllUsersFromRoles();
        DefaultTableModel model = (DefaultTableModel) accountTable.getModel();
        model.setRowCount(0);

        for (User user : UserService.userList) {
            if (user.getRole().equals("Manager") || user.getRole().equals("Staff") || user.getRole().equals("Doctor")) {
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

    private void loadAppointmentTable() {
        AppointmentService.getAppointment();
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);

        for (Appointment appt : AppointmentService.appointmentList) {
            String customerName = "";
            User customer = UserService.searchUser(appt.getCustomerUsername());
            if (customer != null) {
                customerName = customer.getName();
            }

            String doctorName = "";
            User doctor = UserService.searchUser(appt.getDoctorUsername());
            if (doctor != null) {
                doctorName = doctor.getName();
            }

            Object[] row = {
                appt.getAppointmentID(),
                appt.getCustomerUsername(),
                customerName,
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

    private void loadAllFeedbacks() {
        FeedbackService.getFeedbacks();
        DefaultTableModel model = (DefaultTableModel) feedbackTable.getModel();
        model.setRowCount(0); // Clear existing rows

        for (Feedback f : FeedbackService.feedbackList) {
            Object[] row = {
                f.getFeedbackID(),
                f.getDoctorID(),
                f.getStaffID(),
                f.getCustomerID(),
                f.getFeedback(),
                f.getComment(),
                f.getCharge()
            };
            model.addRow(row);
        }
    }

    @SuppressWarnings("unchecked")
    
   private String getAppointmentDateByID(String apptID) {
     try (BufferedReader br = openData("appointments.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",", -1);
            if (data.length >= 4 && data[0].trim().equals(apptID)) {
                return data[3].trim(); // DATE is column 3
            }
        }
    } catch (IOException e) {
        System.out.println("Cannot open appointments.txt: " + e.getMessage());
    }
    return null;
}
   
   private void clearReportDisplay() {
    jLabel22.setText("");
    jLabel23.setText("");
    jLabel24.setText("");

    jTextField3.setText("");
    jTextField4.setText("");
    jTextField5.setText("");

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);
}


   private javax.swing.ButtonGroup reportGroup;

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
        jLabel11 = new javax.swing.JLabel();
        roleBox = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountTable = new javax.swing.JTable();
        refreshAccountButton = new javax.swing.JButton();
        saveAccountButton = new javax.swing.JButton();
        deleteAccountButton = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        passwordField2 = new javax.swing.JPasswordField();
        usernameField2 = new javax.swing.JTextField();
        emailField2 = new javax.swing.JTextField();
        phoneNumberField2 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        refreshApptButton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        feedbackTable = new javax.swing.JTable();
        jLabel17 = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel22 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AMC - Manager Menu");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Manager Menu");

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

        jLabel11.setText("Role:");

        roleBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Manager", "Staff", "Doctor" }));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Create New Account");

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
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                        .addGap(86, 86, 86)
                                        .addComponent(genderBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(101, 101, 101)
                                .addComponent(roleBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(188, 188, 188))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(passwordField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(usernameField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(165, 165, 165)
                        .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
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
                .addGap(15, 15, 15)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(roleBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addGap(27, 27, 27)
                .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(362, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(329, 329, 329))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(102, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );

        jTabbedPane1.addTab("Create New Account", jPanel2);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Account Management");

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

        deleteAccountButton.setText("Delete User");
        deleteAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAccountButtonActionPerformed(evt);
            }
        });

        jLabel12.setText("Username:");

        jLabel13.setText("New Password:");

        jLabel15.setText("New Email:");

        jLabel16.setText("New Phone Number:");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(266, 266, 266)
                        .addComponent(jLabel6))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 732, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(passwordField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(usernameField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(phoneNumberField2, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel15)
                                        .addComponent(emailField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(refreshAccountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(saveAccountButton, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                            .addComponent(deleteAccountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGap(78, 78, 78)))
                            .addComponent(jLabel12))))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap(31, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(70, 70, 70)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                        .addGap(63, 63, 63)
                        .addComponent(refreshAccountButton)
                        .addGap(18, 18, 18)
                        .addComponent(deleteAccountButton)
                        .addGap(18, 18, 18)
                        .addComponent(saveAccountButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Account Management", jPanel4);

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

        refreshApptButton.setText("Refresh");
        refreshApptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshApptButtonActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel14.setText("View Appointments");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 943, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(462, 462, 462)
                        .addComponent(jLabel14))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(498, 498, 498)
                        .addComponent(refreshApptButton)))
                .addContainerGap(136, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshApptButton)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("View Appointments", jPanel3);

        feedbackTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Feedback ID", "Doctor ID", "Staff ID", "Customer ID", "Feedback", "Comment", "Charged Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(feedbackTable);

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel17.setText("View All Feedback");

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(407, 407, 407)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(417, 417, 417)
                        .addComponent(jLabel17))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(94, 94, 94)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 808, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(246, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel17)
                .addGap(14, 14, 14)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(refreshButton)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("View Feedbacks", jPanel5);

        jRadioButton2.setText("Total Users");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        jRadioButton3.setText("Total Appoimtments");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jRadioButton4.setText("Total Revenue");
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jLabel22.setText("N/A");

        jTextField3.setText("N/A");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel23.setText("N/A");

        jTextField4.setText("N/A");
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jLabel24.setText("N/A");

        jTextField5.setText("N/A");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(jTable1);

        jRadioButton1.setText("Appointments by Doctor");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        jRadioButton5.setText("Feedback Summary");
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(76, 76, 76)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 1026, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jRadioButton2)
                                .addGap(47, 47, 47)
                                .addComponent(jRadioButton3))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jRadioButton4)
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButton1)
                                .addGap(18, 18, 18)
                                .addComponent(jRadioButton5)))))
                .addContainerGap(46, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton4)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton5))
                .addGap(33, 33, 33)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(105, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Reports", jPanel6);

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
   public ManagerMenu(User manager) {
    initComponents();
    this.manager = manager;
    util.InactivityManager.attach(this, () -> {
            this.dispose();
            new amc.LoginPage().setVisible(true);
        });


    WindowPositioner.apply(this); 

   
    reportGroup = new javax.swing.ButtonGroup();
    reportGroup.add(jRadioButton2); // Total Users
    reportGroup.add(jRadioButton3); // Total Appointment
    reportGroup.add(jRadioButton4); // Total Revenue
    reportGroup.add(jRadioButton1); // Appointments by Doctor
    reportGroup.add(jRadioButton5); // Feedback

   
    jRadioButton2.setSelected(true);
    jRadioButton2ActionPerformed(null);
}


    private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed
        new LoginPage().setVisible(true);
        dispose();
    }//GEN-LAST:event_logoutButtonActionPerformed

    private void usernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernameFieldActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    private void dateOfBirthFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateOfBirthFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dateOfBirthFieldActionPerformed

    private void phoneNumberFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phoneNumberFieldActionPerformed

    private void emailFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_emailFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_emailFieldActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        createUser();
    }//GEN-LAST:event_createButtonActionPerformed

    private void refreshAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshAccountButtonActionPerformed
        loadUserTable();
    }//GEN-LAST:event_refreshAccountButtonActionPerformed

    private void saveAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAccountButtonActionPerformed
        saveUserChanges();
    }//GEN-LAST:event_saveAccountButtonActionPerformed

    private void deleteAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAccountButtonActionPerformed
        deleteUser();
    }//GEN-LAST:event_deleteAccountButtonActionPerformed

    private void accountTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_accountTableMouseClicked
        fillEditFields();
    }//GEN-LAST:event_accountTableMouseClicked

    private void refreshApptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshApptButtonActionPerformed
        loadAppointmentTable();
    }//GEN-LAST:event_refreshApptButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadAllFeedbacks();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed

    clearReportDisplay();
    jLabel22.setText("Total Users");
    jLabel23.setText("Male");
    jLabel24.setText("Female");

    int totalUsers = 0, maleCount = 0, femaleCount = 0;

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setColumnIdentifiers(new String[]{"Username","Name","Gender","Email","Phone","DOB"});
    model.setRowCount(0);

    try (BufferedReader br = openData("Customer.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] data = line.split(",", -1);
            if (data.length < 8) continue;

            totalUsers++;
            if ("male".equalsIgnoreCase(data[4])) maleCount++;
            else if ("female".equalsIgnoreCase(data[4])) femaleCount++;

            model.addRow(new Object[]{ data[0], data[2], data[4], data[5], data[6], data[7] });
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Cannot open data/Customer.txt: " + e.getMessage());
        return;
    }

    jTextField3.setText(String.valueOf(totalUsers));
    jTextField4.setText(String.valueOf(maleCount));
    jTextField5.setText(String.valueOf(femaleCount));

    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
       clearReportDisplay();
    jLabel22.setText("Total Revenue");
    jLabel23.setText("Total Payments");
    jLabel24.setText("Total Revenue (Filtered)");

    double totalRevenue = 0.0;
    int paymentCount = 0;

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setColumnIdentifiers(new String[]{"Payment ID","Appointment ID","Method","Amount"});
    model.setRowCount(0);

    try (BufferedReader br = openData("payments.txt")) {
        String line;
        boolean headerMaybe = true;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] d = line.split(",", -1);
            if (d.length < 4) continue;

            
            String amtRaw = d[2];

            
            if (headerMaybe && !isNumeric(cleanAmount(amtRaw))) {
                headerMaybe = false;
                continue;
            }
            headerMaybe = false;

            Double value = safeParseDouble(cleanAmount(amtRaw));
            if (value == null) continue;

            totalRevenue += value;
            paymentCount++;

            
            model.addRow(new Object[]{ d[0].trim(), d[1].trim(), d[3].trim(), String.format("%.2f", value) });
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
        return;
    }

    jTextField3.setText(String.format("%.2f", totalRevenue));
    jTextField4.setText(String.valueOf(paymentCount));
    jTextField5.setText(String.format("%.2f", totalRevenue)); 
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
      clearReportDisplay();
    jLabel22.setText("Total Appointments");
    jLabel23.setText("Unique Customers");
    jLabel24.setText("Unique Doctors");

    int totalAppointments = 0;
    java.util.HashSet<String> customers = new java.util.HashSet<>();
    java.util.HashSet<String> doctors   = new java.util.HashSet<>();

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setColumnIdentifiers(new String[]{
        "Appt ID","Customer","Doctor","Date","Time","Status","Booked By"
    });
    model.setRowCount(0);

    try (BufferedReader br = openData("appointments.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] d = line.split(",", -1);
            if (d.length < 7) continue;

            totalAppointments++;
            customers.add(d[1].trim()); 
            doctors.add(d[2].trim());   

            model.addRow(new Object[]{
                d[0].trim(), // id
                d[1].trim(), // customer
                d[2].trim(), // doctor
                d[3].trim(), // date
                d[4].trim(), // time
                d[5].trim(), // status
                d[6].trim()  // booked by
            });
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Cannot open appointments.txt: " + e.getMessage());
        return;
    }

    jTextField3.setText(String.valueOf(totalAppointments));
    jTextField4.setText(String.valueOf(customers.size()));
    jTextField5.setText(String.valueOf(doctors.size()));
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        clearReportDisplay();
    jLabel22.setText("Total Appointments");
    jLabel23.setText("Unique Doctors");
    jLabel24.setText("Avg per Doctor");

   
    java.util.Map<String, String> doctorNames = new java.util.HashMap<>();
    try (BufferedReader br = openData("Doctor.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] d = line.split(",", -1);
            if (d.length >= 3) {
                doctorNames.put(d[0].trim(), d[2].trim()); // ID → Name
            }
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Cannot open Doctor.txt: " + e.getMessage());
    }

    class Cnt { int total, completed, cancelled, booked; }
    java.util.Map<String, Cnt> byDoctor = new java.util.LinkedHashMap<>();
    int totalAppointments = 0;

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);
    model.setColumnIdentifiers(new String[]{
        "Doctor ID","Doctor Name","Total Appointments","Completed","Cancelled","Booked"
    });

    try (BufferedReader br = openData("appointments.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            
            String[] d = line.split(",", -1);
            if (d.length < 7) continue;

            String doctorId = d[2].trim();
            String status   = d[5].trim();

            Cnt c = byDoctor.computeIfAbsent(doctorId, k -> new Cnt());
            c.total++;
            totalAppointments++;

            if ("Completed".equalsIgnoreCase(status)) c.completed++;
            else if ("Cancelled".equalsIgnoreCase(status)) c.cancelled++;
            else c.booked++;
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Cannot open appointments.txt: " + e.getMessage());
        return;
    }

   
    for (var e : byDoctor.entrySet()) {
        String docId = e.getKey();
        String docName = doctorNames.getOrDefault(docId, "Unknown");
        Cnt c = e.getValue();
        model.addRow(new Object[]{ docId, docName, c.total, c.completed, c.cancelled, c.booked });
    }

    int uniqueDoctors = byDoctor.size();
    double avg = uniqueDoctors > 0 ? (double) totalAppointments / uniqueDoctors : 0.0;

    jTextField3.setText(String.valueOf(totalAppointments));
    jTextField4.setText(String.valueOf(uniqueDoctors));
    jTextField5.setText(String.format("%.2f", avg));   
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
                                              
    clearReportDisplay();
    jLabel22.setText("Total Feedback");
    jLabel23.setText("Doctors with Feedback");
    jLabel24.setText("Staff with Feedback");

    int totalFeedback = 0;
    java.util.Set<String> doctorSet = new java.util.HashSet<>();
    java.util.Set<String> staffSet  = new java.util.HashSet<>();

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);
    model.setColumnIdentifiers(new String[]{
        "Feedback ID","Doctor ID","Staff ID","Customer ID","Title","Comment","Amount","Appt ID"
    });

    try (BufferedReader br = openData("feedbacks.txt")) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

           
            String[] d = line.split(",", -1);
            if (d.length < 8) continue;

            String fid   = d[0].trim();
            String docId = d[1].trim();
            String stfId = d[2].trim();
            String cstId = d[3].trim();
            String title = d[4].trim();
            String comm  = d[5].trim();
            String amt   = d[6].trim();
            String appt  = d[7].trim();

            totalFeedback++;
            if (!docId.isEmpty()) doctorSet.add(docId);
            if (!stfId.isEmpty()) staffSet.add(stfId);

            model.addRow(new Object[]{ fid, docId, stfId, cstId, title, comm, amt, appt });
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Cannot open feedbacks.txt: " + e.getMessage());
        return;
    }

    jTextField3.setText(String.valueOf(totalFeedback));       // Total Feedback
    jTextField4.setText(String.valueOf(doctorSet.size()));    // Doctors with Feedback
    jTextField5.setText(String.valueOf(staffSet.size())); 
    }//GEN-LAST:event_jRadioButton5ActionPerformed

private String cleanAmount(String s) {
    if (s == null) return "";
    
    return s.replaceAll("[^0-9.\\-]", "").trim();
}
private boolean isNumeric(String s) {
    try { Double.parseDouble(s); return true; } catch (Exception e) { return false; }
}
private Double safeParseDouble(String s) {
    try { return Double.parseDouble(s); } catch (Exception e) { return null; }
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable accountTable;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton createButton;
    private javax.swing.JTextField dateOfBirthField;
    private javax.swing.JButton deleteAccountButton;
    private javax.swing.JTextField emailField;
    private javax.swing.JTextField emailField2;
    private javax.swing.JTable feedbackTable;
    private javax.swing.JComboBox<String> genderBox;
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
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
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
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JButton logoutButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JPasswordField passwordField2;
    private javax.swing.JTextField phoneNumberField;
    private javax.swing.JTextField phoneNumberField2;
    private javax.swing.JButton refreshAccountButton;
    private javax.swing.JButton refreshApptButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox<String> roleBox;
    private javax.swing.JButton saveAccountButton;
    private javax.swing.JTextField usernameField;
    private javax.swing.JTextField usernameField2;
    // End of variables declaration//GEN-END:variables
}

