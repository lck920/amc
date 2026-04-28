/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class UserFileHandler {

    public static void saveUserToFile(User user) {
    String filePath = "C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\" + user.getRole() + ".txt";

    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
        writer.println(user.toFileString());
    } catch (IOException e) {
        System.out.println("❌ Error writing to " + filePath);
        System.out.println("Exception: " + e.getMessage());

     }
    }

}   
