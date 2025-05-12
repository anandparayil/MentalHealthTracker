package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // Hash password using BCrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Verify password with stored hash
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
