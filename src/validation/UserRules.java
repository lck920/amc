/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package validation;

import model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserRules {
    
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9]{5,10}$"); // 5–10 alphanumeric
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[^a-zA-Z0-9\\s]).{8,15}$"); // 8–15, at least 1 special char
    private static final Pattern EMAIL    = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,6}$");
    private static final Pattern PHONE    = Pattern.compile("^\\+?\\d{9,15}$"); // 9–15 digits, optional +
    private static final Pattern DOB      = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$"); // DD-MM-YYYY

   
    public static ValidationResult validateCreate(User u) {
        List<String> errs = new ArrayList<>();

        if (u == null) {
            errs.add("User is null.");
            return ValidationResult.fail(errs);
        }
        if (u.getUsername() == null || !USERNAME.matcher(u.getUsername().trim()).matches())
            errs.add("Username must be 5–10 characters (letters and digits only).");

        if (u.getPassword() == null || !PASSWORD.matcher(u.getPassword()).matches())
            errs.add("Password must be 8–15 chars and include at least one special symbol.");

        if (u.getEmail() == null || !EMAIL.matcher(u.getEmail().trim()).matches())
            errs.add("Email format is invalid (e.g., name@example.com).");

        if (u.getPhoneNumber() == null || !PHONE.matcher(u.getPhoneNumber().trim()).matches())
            errs.add("Phone must be 9–15 digits (optional leading +).");

        if (u.getDateOfBirth() != null && !u.getDateOfBirth().isBlank()
                && !DOB.matcher(u.getDateOfBirth().trim()).matches())
            errs.add("Date of birth must be DD-MM-YYYY.");

        return errs.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errs);
    }

    
    public static ValidationResult validateUpdate(User u) {
        List<String> errs = new ArrayList<>();

        if (u == null) {
            errs.add("User is null.");
            return ValidationResult.fail(errs);
        }
        if (u.getUsername() == null || !USERNAME.matcher(u.getUsername().trim()).matches())
            errs.add("Username must be 5–10 characters (letters and digits only).");

        if (u.getPassword() != null && !u.getPassword().isBlank()
                && !PASSWORD.matcher(u.getPassword()).matches())
            errs.add("If changing password, it must be 8–15 chars and include at least one special symbol.");

        if (u.getEmail() == null || !EMAIL.matcher(u.getEmail().trim()).matches())
            errs.add("Email format is invalid (e.g., name@example.com).");

        if (u.getPhoneNumber() == null || !PHONE.matcher(u.getPhoneNumber().trim()).matches())
            errs.add("Phone must be 9–15 digits (optional leading +).");

        if (u.getDateOfBirth() != null && !u.getDateOfBirth().isBlank()
                && !DOB.matcher(u.getDateOfBirth().trim()).matches())
            errs.add("Date of birth must be DD-MM-YYYY.");

        return errs.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errs);
    }
}
