import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String password = "admin123"; // Change this to your desired password
        String encodedPassword = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println("\nPostgreSQL Update Command:");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE email = 'admin@greenlink.com';");
    }
} 