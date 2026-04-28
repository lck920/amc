/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

public final class UserFactory {
    private UserFactory() {}

    public static User from(
            String role,
            String username, String password, String name,
            String gender, String email, String phoneNumber, String dateOfBirth) {

        String r = role == null ? "" : role.trim().toLowerCase();
        switch (r) {
            case "supermanager":
                return new SuperManager(username, password, name, gender, email, phoneNumber, dateOfBirth);
            case "manager":
                return new Manager(username, password, name, gender, email, phoneNumber, dateOfBirth);
            case "staff":
                return new Staff(username, password, name, gender, email, phoneNumber, dateOfBirth);
            case "doctor":
                return new Doctor(username, password, name, gender, email, phoneNumber, dateOfBirth);
            case "customer":
                return new Customer(username, password, name, gender, email, phoneNumber, dateOfBirth);
            default:
                
                return new User(username, password, name, role, gender, email, phoneNumber, dateOfBirth);
        }
    }
}

