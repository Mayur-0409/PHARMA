package util;

import java.util.regex.Pattern;
import java.util.Vector;

public class ValidationUtil {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    
    // Phone number validation pattern (10 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\d{10}$"
    );
    
    // Name validation pattern (letters, spaces, and hyphens)
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[A-Za-z\\s]{2,50}$"
    );
    
    // ID validation pattern (alphanumeric)
    private static final Pattern ID_PATTERN = Pattern.compile(
        "^[A-Za-z0-9]+$"
    );

    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id).matches();
    }

    public static String validateCustomerData(Vector<String> columnNames, Vector<String> values) {
        String name = null;
        String phone = null;
        String email = null;

        // Find the values based on column names
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i).toLowerCase();
            String value = values.get(i);

            switch (columnName) {
                case "name":
                    name = value;
                    break;
                case "phone":
                    phone = value;
                    break;
                case "email":
                    email = value;
                    break;
            }
        }

        // Validate name
        if (name != null && !NAME_PATTERN.matcher(name).matches()) {
            return "Invalid Name. Must be 2-50 characters long and contain only letters and spaces.";
        }

        // Validate phone
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            return "Invalid Phone Number. Must be 10 digits.";
        }

        // Validate email
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid Email Format.";
        }

        return null;
    }

    public static String validateProductData(Vector<String> columnNames, Vector<String> values) {
        String name = null;
        String price = null;

        // Find the values based on column names
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i).toLowerCase();
            String value = values.get(i);

            switch (columnName) {
                case "name":
                    name = value;
                    break;
                case "price":
                    price = value;
                    break;
            }
        }

        // Validate name
        if (name != null && name.trim().isEmpty()) {
            return "Product name cannot be empty.";
        }

        // Validate price
        if (price != null && !PRICE_PATTERN.matcher(price).matches()) {
            return "Invalid Price Format. Must be a number with up to 2 decimal places.";
        }

        return null;
    }
} 