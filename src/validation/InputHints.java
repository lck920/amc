/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package validation;

import java.util.LinkedHashMap;
import java.util.Map;

public class InputHints {
    public static Map<String, String> user() {
        Map<String,String> m = new LinkedHashMap<>();
        m.put("username", "4–20 characters; letters, digits, underscore only.");
        m.put("password", "At least 8 chars, include UPPER, lower, and a number.");
        m.put("email",    "Format: name@example.com.");
        m.put("phone",    "9–15 digits; optional + at the start.");
        m.put("dob",      "YYYY-MM-DD (e.g., 2001-03-15).");
        m.put("role",     "SuperManager / Manager / Staff / Doctor / Customer.");
        return m;
    }
}
