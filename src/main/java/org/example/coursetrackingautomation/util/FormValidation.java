package org.example.coursetrackingautomation.util;

import java.util.Objects;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public final class FormValidation {

    private FormValidation() {
    }
    
    public static void applyNameFilter(TextField field) {
        applyFilter(field, "[\\p{L} '\\-]*", 50);
    }

    public static void applyUsernameFilter(TextField field) {
        applyFilter(field, "[A-Za-z0-9._-]*", 30);
    }

    public static void applyEmailFilter(TextField field) {
        applyFilter(field, "\\S*", 254);
    }

    public static void applyPhoneFilter(TextField field) {
        applyFilter(field, "[0-9+()\\-\\s]*", 20);
    }

    public static void applyDigitsOnly(TextField field, int maxLen) {
        applyFilter(field, "\\d*", maxLen);
    }

    public static void applyScoreFilter(TextField field) {
        applyFilter(field, "\\d{0,3}([\\.,]\\d{0,2})?", 6);
    }

    private static void applyFilter(TextField field, String regex, int maxLen) {
        if (field == null) {
            return;
        }

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText == null) {
                return null;
            }
            if (newText.length() > maxLen) {
                return null;
            }
            if (!newText.matches(regex)) {
                return null;
            }
            return change;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
    }

    public static String requireNotBlankTrimmed(String raw, String fieldName) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " boş bırakılamaz");
        }
        return value;
    }

    public static String validatePersonNameRequired(String raw, String fieldName) {
        String value = requireNotBlankTrimmed(raw, fieldName);
        if (value.length() < 2) {
            throw new IllegalArgumentException(fieldName + " en az 2 karakter olmalıdır");
        }

        if (!value.matches("[\\p{L}][\\p{L} '\\-]*")) {
            throw new IllegalArgumentException(fieldName + " sadece harf içermelidir");
        }

        return value;
    }

    public static String validateUsernameRequired(String raw) {
        String value = requireNotBlankTrimmed(raw, "Kullanıcı adı");
        if (value.length() < 3 || value.length() > 30) {
            throw new IllegalArgumentException("Kullanıcı adı 3 ile 30 karakter arasında olmalıdır");
        }
        if (!value.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Kullanıcı adı sadece harf, rakam ve (._-) içerebilir");
        }
        return value;
    }

    public static String validateEmailRequired(String raw) {
        String value = requireNotBlankTrimmed(raw, "E-posta");
        validateEmailFormat(value);
        return value;
    }

    public static String validateEmailOptional(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            return null;
        }
        validateEmailFormat(value);
        return value;
    }

    private static void validateEmailFormat(String email) {
        Objects.requireNonNull(email, "email");

        if (email.contains(" ")) {
            throw new IllegalArgumentException("E-posta boşluk içeremez");
        }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("E-posta geçerli formatta olmalıdır (örn: ornek@domain.com)");
        }
    }

    public static String validatePhoneOptional(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            return null;
        }

        if (value.matches(".*[A-Za-z\\p{L}].*")) {
            throw new IllegalArgumentException("Telefon alanında harf bulunamaz");
        }

        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 15) {
            throw new IllegalArgumentException("Telefon numarası 10-15 haneli olmalıdır");
        }

        return value;
    }
}
