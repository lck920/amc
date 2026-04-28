/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public class Staff extends User {
    public Staff(String username, String password, String name, String gender, String email, String phoneNumber, String dateOfBirth) {
        super(username, password, name, "Staff", gender, email, phoneNumber, dateOfBirth);
    }

    public void save() {
        UserFileHandler.saveUserToFile(this);
    }
    
    @Override public boolean canCreateUser()         { return true; }
    @Override public boolean canGenerateReport()     { return true; }
    @Override public boolean canApproveAppointment() { return true; }

}
