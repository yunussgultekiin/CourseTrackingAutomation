package org.example.coursetrackingautomation.ui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.exception.AuthenticationException;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UiExceptionHandler {

    private static final String DEFAULT_ERROR_TITLE = "Hata";

    private final AlertUtil alertUtil;

    public void handle(Exception exception) {
        if (exception == null) {
            return;
        }

        log.warn("UI error", exception);

        if (exception instanceof AuthenticationException) {
            alertUtil.showErrorAlert("Giriş başarısız", translateMessage(exception.getMessage()));
            return;
        }

        if (exception instanceof IllegalArgumentException) {
            alertUtil.showErrorAlert("Doğrulama hatası", translateMessage(exception.getMessage()));
            return;
        }

        alertUtil.showErrorAlert(DEFAULT_ERROR_TITLE, "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
    }

    private static String translateMessage(String message) {
        if (message == null || message.isBlank()) {
            return "İşlem gerçekleştirilemedi.";
        }

        return switch (message) {
            case "No active session" -> "Oturum bulunamadı. Lütfen tekrar giriş yapın.";
            case "Course not found" -> "Ders bulunamadı.";
            case "Enrollment not found" -> "Öğrenci ders kaydı bulunamadı.";
            case "Username already exists" -> "Bu kullanıcı adı zaten kullanılıyor.";
            case "Current password is incorrect" -> "Mevcut şifre yanlış.";
            case "Current password must not be blank" -> "Mevcut şifre boş bırakılamaz.";
            case "New password must not be blank" -> "Yeni şifre boş bırakılamaz.";
            case "New password and confirmation do not match" -> "Yeni şifre ile doğrulama şifresi uyuşmuyor.";
            default -> {
                // Simple partial mappings to avoid English leaks
                String lowered = message.toLowerCase();
                if (lowered.contains("must not be blank")) {
                    yield message.replace("must not be blank", "boş bırakılamaz");
                }
                if (lowered.contains("must be greater than 0")) {
                    yield message.replace("must be greater than 0", "0'dan büyük olmalıdır");
                }
                if (lowered.contains("must be a number")) {
                    yield message.replace("must be a number", "sayısal olmalıdır");
                }
                if (lowered.contains("not found")) {
                    yield "Kayıt bulunamadı.";
                }
                yield message;
            }
        };
    }
}
