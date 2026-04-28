package model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class User {
    private String username;
    private String password;
    private String name;
    private String role;
    private String gender;
    private String email;
    private String phoneNumber;
    private String dateOfBirth; 

    public User(String username, String password, String name, String role, String gender, String email, String phoneNumber, String dateOfBirth) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

   
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate dob = LocalDate.parse(dateOfBirth, formatter);
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0; 
        }
    }

   
    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    
    public String toFileString() {
        return username + "," + password + "," + name + "," + role + "," + gender + "," + email + "," + phoneNumber + "," + dateOfBirth;
    }

    @Override
    public String toString() {
        return "[" + role + "] " + name + " (" + username + ")";
    }
    
        // --- Polymorphism hooks (override in subclasses) ---
    public boolean canCreateUser()        { return false; }
    public boolean canGenerateReport()    { return false; }
    public boolean canApproveAppointment(){ return false; }

}
