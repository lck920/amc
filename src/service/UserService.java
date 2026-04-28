package service;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import model.*;                       // includes User and all subclasses
import validation.UserRules;
import validation.ValidationResult;

public class UserService {

    // ---- Config ----
    private static final String BASE_PATH = "C:\\Users\\ASUS\\Desktop\\AMC\\src\\data\\";
    private static final String[] ROLE_FILES = { "SuperManager", "Manager", "Staff", "Doctor", "Customer" };

    // In-memory cache of users (all roles)
    public static ArrayList<User> userList = new ArrayList<>();

    // Helper: build absolute path for a role file
    private static String getRoleFilePath(String roleName) {
        // roleName may contain spaces from UI; file names don't
        return BASE_PATH + roleName.replace(" ", "") + ".txt";
    }

    // ✅ Load all users from each role-specific .txt into memory (polymorphic objects)
    public static void loadAllUsersFromRoles() {
        userList.clear();

        for (String roleFile : ROLE_FILES) {
            String filePath = getRoleFilePath(roleFile);
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] userDetails = line.split(",");
                    if (userDetails.length < 8) continue;

                    // Columns expected:
                    // 0=username, 1=password, 2=name, 3=role, 4=gender, 5=email, 6=phone, 7=dob
                    String username    = userDetails[0].trim();
                    String password    = userDetails[1].trim();
                    String name        = userDetails[2].trim();
                    String role        = userDetails[3].trim();
                    String gender      = userDetails[4].trim();
                    String email       = userDetails[5].trim();
                    String phone       = userDetails[6].trim();
                    String dateOfBirth = userDetails[7].trim();

                    // ✨ Polymorphic construction
                    User user = UserFactory.from(role, username, password, name, gender, email, phone, dateOfBirth);
                    userList.add(user);
                }
            } catch (IOException e) {
                System.out.println("⚠️ Could not read " + roleFile + ".txt: " + e.getMessage());
            }
        }
    }

    // ✅ Add user (validates, checks duplicate, appends to role file)
    public static boolean addUser(User user) {
        // 1) Validate first
        ValidationResult vr = UserRules.validateCreate(user);
        if (!vr.isOk()) {
            throw new IllegalArgumentException(String.join("\n", vr.getErrors()));
        }

        // 2) Business rule: no duplicate username
        if (searchUser(user.getUsername()) != null) {
            throw new IllegalStateException("Username already exists.");
        }

        // 3) Save
        userList.add(user);
        saveToRoleFile(user);
        return true;
    }

    // ✅ Append a single user record to their role file
    public static void saveToRoleFile(User user) {
        String filePath = getRoleFilePath(user.getRole());

        try {
            File file = new File(filePath);
            if (!file.exists()) file.createNewFile();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                String line = String.join(",",
                        user.getUsername(),
                        user.getPassword(),
                        user.getName(),
                        user.getRole(),
                        user.getGender(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getDateOfBirth()
                );
                bw.write(line);
                bw.newLine(); // ensure next user starts on a new line
            }
        } catch (IOException e) {
            System.out.println("❌ Error writing to " + filePath + ": " + e.getMessage());
        }
    }

    // ✅ Find by username in the in-memory list
    public static User searchUser(String username) {
        for (User user : userList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    // ✅ Update an existing user (validate, update memory, rewrite that role file)
    public static boolean updateUser(User updatedUser) {
        // 1) Validate update (password can be optional depending on your rule)
        ValidationResult vr = UserRules.validateUpdate(updatedUser);
        if (!vr.isOk()) {
            throw new IllegalArgumentException(String.join("\n", vr.getErrors()));
        }

        // 2) Update the in-memory list
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUsername().equals(updatedUser.getUsername())) {
                userList.set(i, updatedUser);
                // 3) Persist only that role file
                overwriteRoleFile(updatedUser.getRole());
                return true;
            }
        }
        return false; // user not found
    }

    // ✅ Rewrite the specified role file using the in-memory list
    public static void overwriteRoleFile(String role) {
        String filePath = getRoleFilePath(role);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (User user : userList) {
                if (user.getRole().equalsIgnoreCase(role)) {
                    String line = String.join(",",
                            user.getUsername(),
                            user.getPassword(),
                            user.getName(),
                            user.getRole(),
                            user.getGender(),
                            user.getEmail(),
                            user.getPhoneNumber(),
                            user.getDateOfBirth()
                    );
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error rewriting " + filePath + ": " + e.getMessage());
        }
    }

    // ✅ Delete by username (updates memory + persists only that role file)
    public static boolean deleteUser(String username) {
        User user = searchUser(username);
        if (user != null) {
            userList.remove(user);
            overwriteRoleFile(user.getRole());
            return true;
        }
        return false;
    }

    // ✅ Login against a specific role file (returns a polymorphic User)
    public static User login(String username, String password, String roleFileName) {
        String filePath = getRoleFilePath(roleFileName);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] userDetails = line.split(",");
                if (userDetails.length < 8) continue;

                String fileUsername = userDetails[0].trim();
                String filePassword = userDetails[1].trim();

                if (fileUsername.equals(username) && filePassword.equals(password)) {
                    // role taken from column so it works even if file name casing differs
                    String role        = userDetails[3].trim();
                    String name        = userDetails[2].trim();
                    String gender      = userDetails[4].trim();
                    String email       = userDetails[5].trim();
                    String phone       = userDetails[6].trim();
                    String dateOfBirth = userDetails[7].trim();

                    // ✨ Polymorphic construction
                    return UserFactory.from(role, fileUsername, filePassword, name, gender, email, phone, dateOfBirth);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading login file: " + e.getMessage());
        }

        return null;
    }

    // ✅ Auto-detect role: try each role file until one matches (returns polymorphic User)
    public static User loginAutoRole(String username, String password) {
        for (String rf : ROLE_FILES) {
            User u = login(username, password, rf); // reuses per-file login
            if (u != null) {
                return u; // u.getRole() comes from file; object is the correct subclass
            }
        }
        return null;
    }

    // ---- Validation helpers (unchanged) ----

    public static boolean isValidPassword(String password) {
        String regex = "^(?=.*[^a-zA-Z0-9\\s]).{8,15}$";
        return Pattern.compile(regex).matcher(password).matches();
    }

    public static boolean isValidUsername(String username) {
        String regex = "^[a-zA-Z0-9]{5,10}$";
        return Pattern.compile(regex).matcher(username).matches();
    }

    public static boolean isValidName(String name) {
        String regex = "^[A-Za-z][A-Za-z\\s'-.]*[A-Za-z]$";
        return Pattern.compile(regex).matcher(name).matches();
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    public static boolean isValidDateOfBirth(String dateOfBirth) {
        String regex = "^\\d{2}-\\d{2}-\\d{4}$";
        return Pattern.compile(regex).matcher(dateOfBirth).matches();
    }
}
