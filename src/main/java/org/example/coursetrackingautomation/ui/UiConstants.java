package org.example.coursetrackingautomation.ui;

public final class UiConstants {

    /**
     * Central place for UI labels, message keys, FXML resource paths, and common window configuration.
     */

    public static final String ERROR_KEY_NO_ACTIVE_SESSION = "No active session";
    public static final String ERROR_KEY_COURSE_NOT_FOUND = "Course not found";
    public static final String ERROR_KEY_ENROLLMENT_NOT_FOUND = "Enrollment not found";
    public static final String ERROR_KEY_USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String ERROR_KEY_COURSE_SELECTION_REQUIRED = "Course selection is required";
    public static final String ERROR_KEY_INSTRUCTOR_SELECTION_REQUIRED = "Instructor selection is required";
    public static final String ERROR_KEY_STUDENT_SELECTION_REQUIRED = "Student selection is required";
    public static final String ERROR_KEY_ROLE_SELECTION_REQUIRED = "Role selection is required";

    public static final String ALERT_TITLE_ERROR = "Hata";
    public static final String ALERT_TITLE_VALIDATION_ERROR = "Doğrulama hatası";
    public static final String ALERT_TITLE_AUTHENTICATION_FAILED = "Giriş başarısız";
    public static final String ALERT_TITLE_WARNING = "Uyarı";
    public static final String ALERT_TITLE_SUCCESS = "Başarılı";

    public static final String ALERT_MESSAGE_UNEXPECTED_ERROR = "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.";
    public static final String ALERT_MESSAGE_OPERATION_FAILED = "İşlem gerçekleştirilemedi.";
    public static final String UI_WELCOME_PREFIX = "Hoş geldin, ";

    public static final String UI_STATUS_NOT_GRADED = "Notlar girilmedi";
    public static final String UI_STATUS_PASSED = "Geçti";
    public static final String UI_STATUS_FAILED = "Kaldı";

    public static final String UI_MESSAGE_SELECT_COURSE_FIRST = "Lütfen önce bir ders seçiniz.";
    public static final String UI_MESSAGE_SELECT_WEEK_FIRST = "Lütfen önce bir hafta seçiniz.";
    public static final String UI_MESSAGE_CHANGES_SAVED = "Değişiklikler kaydedildi.";
    public static final String UI_MESSAGE_PROFILE_UPDATED = "Profil bilgileriniz güncellendi.";

    public static final String FXML_LOGIN = "/fxml/login.fxml";
    public static final String FXML_ADMIN_DASHBOARD = "/fxml/admin_dashboard.fxml";
    public static final String FXML_STUDENT_DASHBOARD = "/fxml/student_dashboard.fxml";
    public static final String FXML_INSTRUCTOR_DASHBOARD = "/fxml/instructor_dashboard.fxml";
    public static final String FXML_TRANSCRIPT_POPUP = "/fxml/transcript_popup.fxml";
    public static final String FXML_ADD_USER_FORM = "/fxml/add_user_form.fxml";
    public static final String FXML_ADD_COURSE_FORM = "/fxml/add_course_form.fxml";
    public static final String FXML_ENROLL_COURSE_POPUP = "/fxml/enroll_course_popup.fxml";
    public static final String FXML_PROFILE_POPUP = "/fxml/profile_popup.fxml";
    public static final String FXML_EDIT_USER_FORM = "/fxml/edit_user_form.fxml";
    public static final String FXML_EDIT_COURSE_FORM = "/fxml/edit_course_form.fxml";
    public static final String FXML_EDIT_ENROLLMENT_FORM = "/fxml/edit_enrollment_form.fxml";
    public static final String FXML_ADMIN_ENROLL_STUDENT_FORM = "/fxml/admin_enroll_student_form.fxml";
    public static final String FXML_EDIT_GRADE_POPUP = "/fxml/edit_grade_popup.fxml";

    public static final double DEFAULT_WINDOW_WIDTH = 800.0;
    public static final double DEFAULT_WINDOW_HEIGHT = 600.0;

    public static final int DEFAULT_TOTAL_COURSE_HOURS = 42;

    public static final String WINDOW_TITLE_ADD_USER = "Kullanıcı Ekle";
    public static final String WINDOW_TITLE_ADD_COURSE = "Ders Ekle";
    public static final String WINDOW_TITLE_ENROLL_COURSE = "Derse Kaydol";
    public static final String WINDOW_TITLE_TRANSCRIPT = "Transkript";

    public static final String WINDOW_TITLE_PROFILE = "Profilim";
    public static final String WINDOW_TITLE_EDIT_USER = "Kullanıcı Düzenle";
    public static final String WINDOW_TITLE_EDIT_COURSE = "Ders Düzenle";
    public static final String WINDOW_TITLE_EDIT_ENROLLMENT = "Kayıt Düzenle";
    public static final String WINDOW_TITLE_ADMIN_ENROLL_STUDENT = "Kayıt Ekle";
    public static final String WINDOW_TITLE_EDIT_GRADE = "Not Düzenle";

    private UiConstants() {}
}
