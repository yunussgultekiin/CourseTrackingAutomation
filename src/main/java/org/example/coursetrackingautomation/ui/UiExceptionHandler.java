package org.example.coursetrackingautomation.ui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coursetrackingautomation.exception.AuthenticationException;
import org.example.coursetrackingautomation.util.AlertUtil;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Maps exceptions to user-facing UI feedback.
 *
 * <p>This component centralizes alert handling for the JavaFX UI and translates common message
 * keys into localized Turkish text. Unknown exceptions are reported using a generic message.</p>
 */
public class UiExceptionHandler {

    private static final String DEFAULT_ERROR_TITLE = UiConstants.ALERT_TITLE_ERROR;

    private final AlertUtil alertUtil;

    /**
     * Handles an exception and displays an appropriate alert to the user.
     *
     * @param exception exception to handle; may be {@code null}
     */
    public void handle(Exception exception) {
        if (exception == null) {
            return;
        }

        log.warn("UI error", exception);

        if (exception instanceof AuthenticationException) {
            alertUtil.showErrorAlert(UiConstants.ALERT_TITLE_AUTHENTICATION_FAILED, translateMessage(exception.getMessage()));
            return;
        }

        if (exception instanceof IllegalArgumentException) {
            alertUtil.showErrorAlert(UiConstants.ALERT_TITLE_VALIDATION_ERROR, translateMessage(exception.getMessage()));
            return;
        }

        alertUtil.showErrorAlert(DEFAULT_ERROR_TITLE, UiConstants.ALERT_MESSAGE_UNEXPECTED_ERROR);
    }

    /**
     * Handles a {@link Throwable} and displays an appropriate alert to the user.
     *
     * <p>This overload is useful for background {@link javafx.concurrent.Task} failures where
     * the reported error type may not be an {@link Exception}. Unknown throwables are shown using
     * a generic Turkish message, while technical details are logged.</p>
     *
     * @param throwable throwable to handle; may be {@code null}
     */
    public void handle(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        if (throwable instanceof Exception ex) {
            handle(ex);
            return;
        }

        log.warn("UI error", throwable);
        alertUtil.showErrorAlert(DEFAULT_ERROR_TITLE, UiConstants.ALERT_MESSAGE_UNEXPECTED_ERROR);
    }

    private static String translateMessage(String message) {
        if (message == null || message.isBlank()) {
            return UiConstants.ALERT_MESSAGE_OPERATION_FAILED;
        }

        return switch (message) {
            case UiConstants.ERROR_KEY_NO_ACTIVE_SESSION -> "Oturum bulunamadı. Lütfen tekrar giriş yapın.";
            case UiConstants.ERROR_KEY_COURSE_NOT_FOUND -> "Ders bulunamadı.";
            case UiConstants.ERROR_KEY_ENROLLMENT_NOT_FOUND -> "Öğrenci ders kaydı bulunamadı.";
            case UiConstants.ERROR_KEY_USERNAME_ALREADY_EXISTS -> "Bu kullanıcı adı zaten kullanılıyor.";
            case UiConstants.ERROR_KEY_COURSE_SELECTION_REQUIRED -> "Lütfen bir ders seçiniz.";
            case UiConstants.ERROR_KEY_INSTRUCTOR_SELECTION_REQUIRED -> "Akademisyen seçimi zorunludur.";
            case UiConstants.ERROR_KEY_STUDENT_SELECTION_REQUIRED -> "Öğrenci seçimi zorunludur.";
            case UiConstants.ERROR_KEY_ROLE_SELECTION_REQUIRED -> "Rol seçimi zorunludur.";
            case "Current password is incorrect" -> "Mevcut şifre yanlış.";
            case "Current password must not be blank" -> "Mevcut şifre boş bırakılamaz.";
            case "New password must not be blank" -> "Yeni şifre boş bırakılamaz.";
            case "New password and confirmation do not match" -> "Yeni şifre ile doğrulama şifresi uyuşmuyor.";
            default -> {
                String normalized = message;
                normalized = normalized.replace("Username", "Kullanıcı adı");
                normalized = normalized.replace("Password", "Şifre");
                normalized = normalized.replace("First name", "Ad");
                normalized = normalized.replace("Last name", "Soyad");
                normalized = normalized.replace("Email", "E-posta");
                normalized = normalized.replace("Instructor", "Akademisyen");
                normalized = normalized.replace("Role", "Rol");
                normalized = normalized.replace("Code", "Ders Kodu");
                normalized = normalized.replace("Name", "Ders Adı");
                normalized = normalized.replace("Term", "Dönem");
                normalized = normalized.replace("Course id", "Ders id");
                normalized = normalized.replace("Enrollment id", "Kayıt id");
                normalized = normalized.replace("User id", "Kullanıcı id");
                normalized = normalized.replace("Credit", "Kredi");
                normalized = normalized.replace("Capacity", "Kapasite");
                normalized = normalized.replace("Quota", "Kontenjan");

                String lowered = normalized.toLowerCase();
                if (lowered.contains("must not be blank")) {
                    yield normalized.replace("must not be blank", "boş bırakılamaz");
                }
                if (lowered.contains("must be greater than 0")) {
                    yield normalized.replace("must be greater than 0", "0'dan büyük olmalıdır");
                }
                if (lowered.contains("must be a number")) {
                    yield normalized.replace("must be a number", "sayısal olmalıdır");
                }
                if (lowered.contains("not found")) {
                    yield "Kayıt bulunamadı.";
                }
                yield normalized;
            }
        };
    }
}
