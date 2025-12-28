package org.example.coursetrackingautomation.controller.admin;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.stage.Stage;
import org.example.coursetrackingautomation.controller.EditCourseFormController;
import org.example.coursetrackingautomation.controller.EditEnrollmentFormController;
import org.example.coursetrackingautomation.controller.EditUserFormController;
import org.example.coursetrackingautomation.dto.AdminEnrollmentRowDTO;
import org.example.coursetrackingautomation.dto.AdminUserRowDTO;
import org.example.coursetrackingautomation.dto.CourseDTO;
import org.example.coursetrackingautomation.service.AdminDashboardService;
import org.example.coursetrackingautomation.service.CourseService;
import org.example.coursetrackingautomation.ui.FxAsync;
import org.example.coursetrackingautomation.ui.SceneNavigator;
import org.example.coursetrackingautomation.ui.UiConstants;
import org.example.coursetrackingautomation.ui.UiExceptionHandler;
import org.example.coursetrackingautomation.util.AlertUtil;

/**
 * Encapsulates common Admin Dashboard actions.
 *
 * <p>Responsible for opening modals, wiring edit dialogs, executing delete flows, and invoking a
 * refresh callback after a successful operation. This keeps the FXML controller/coordinator free
 * of modal boilerplate.</p>
 */
public final class AdminDashboardActions {

    private final AdminDashboardService adminDashboardService;
    private final CourseService courseService;
    private final AlertUtil alertUtil;
    private final SceneNavigator sceneNavigator;
    private final UiExceptionHandler uiExceptionHandler;
    private final Supplier<Stage> stageSupplier;
    private final Runnable afterAction;

    public AdminDashboardActions(
        AdminDashboardService adminDashboardService,
        CourseService courseService,
        AlertUtil alertUtil,
        SceneNavigator sceneNavigator,
        UiExceptionHandler uiExceptionHandler,
        Supplier<Stage> stageSupplier,
        Runnable afterAction
    ) {
        this.adminDashboardService = adminDashboardService;
        this.courseService = courseService;
        this.alertUtil = alertUtil;
        this.sceneNavigator = sceneNavigator;
        this.uiExceptionHandler = uiExceptionHandler;
        this.stageSupplier = stageSupplier;
        this.afterAction = afterAction;
    }

    /**
     * Opens a modal window.
     *
     * @param fxmlPath FXML resource path
     * @param title window title
     */
    public void openModal(String fxmlPath, String title) {
        Stage stage = stageSupplier.get();
        if (stage == null) {
            return;
        }
        sceneNavigator.openModal(fxmlPath, title, stage);
    }

    /**
     * Opens a modal window and exposes the controller instance to the provided configurator.
     *
     * @param fxmlPath FXML resource path
     * @param title window title
     * @param controllerConfigurator callback used to set up the controller
     * @param <C> controller type
     */
    public <C> void openEditDialog(String fxmlPath, String title, Consumer<C> controllerConfigurator) {
        Stage stage = stageSupplier.get();
        if (stage == null) {
            return;
        }
        sceneNavigator.openModalWithController(fxmlPath, title, stage, controllerConfigurator);
    }

    /**
     * Dispatches an edit action for a dashboard row item.
     *
     * @param item row item
     * @param type entity type identifier (e.g. user/course/enrollment)
     */
    public void handleEdit(Object item, String type) {
        attemptOperation(() -> {
            if (item == null) {
                return;
            }

            switch (type) {
                case "user" -> {
                    Long id = ((AdminUserRowDTO) item).id();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(
                        UiConstants.FXML_EDIT_USER_FORM,
                        UiConstants.WINDOW_TITLE_EDIT_USER,
                        (EditUserFormController c) -> c.setUserId(id)
                    );
                }

                case "course" -> {
                    Long id = ((CourseDTO) item).getId();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(
                        UiConstants.FXML_EDIT_COURSE_FORM,
                        UiConstants.WINDOW_TITLE_EDIT_COURSE,
                        (EditCourseFormController c) -> c.setCourseId(id)
                    );
                }

                case "enrollment" -> {
                    Long id = ((AdminEnrollmentRowDTO) item).id();
                    if (id == null) {
                        return;
                    }
                    openEditDialog(
                        UiConstants.FXML_EDIT_ENROLLMENT_FORM,
                        UiConstants.WINDOW_TITLE_EDIT_ENROLLMENT,
                        (EditEnrollmentFormController c) -> c.setEnrollmentId(id)
                    );
                }
            }

            afterAction.run();
        });
    }

    public void handleDelete(Object item, String type) {
        attemptOperation(() -> {
            String confirmMsg = getDeleteConfirmationMessage(item, type);
            if (alertUtil.showConfirmationAlert("Emin misiniz?", confirmMsg)) {
                performDelete(item, type);
            }
        });
    }

    private void performDelete(Object item, String type) {
        FxAsync.runAsync(
            () -> {
                switch (type) {
                    case "user" -> adminDashboardService.deleteUser(((AdminUserRowDTO) item).id());
                    case "course" -> courseService.deactivateCourse(((CourseDTO) item).getId());
                    case "enrollment" -> {
                        var enrollment = (AdminEnrollmentRowDTO) item;
                        adminDashboardService.dropEnrollment(enrollment.studentId(), enrollment.courseId());
                    }
                    default -> {
                        // no-op
                    }
                }
            },
            () -> {
                switch (type) {
                    case "user" -> alertUtil.showInformationAlert("Başarılı", "Kullanıcı silindi.");
                    case "course" -> alertUtil.showInformationAlert("Başarılı", "Ders pasif yapıldı.");
                    case "enrollment" -> alertUtil.showInformationAlert("Başarılı", "Kayıt silindi.");
                    default -> alertUtil.showInformationAlert("Başarılı", "İşlem tamamlandı.");
                }
                afterAction.run();
            },
            uiExceptionHandler::handle
        );
    }

    private String getDeleteConfirmationMessage(Object item, String type) {
        return switch (type) {
            case "user" -> "'" + ((AdminUserRowDTO) item).username() + "' kullanıcısı silinecek.";
            case "course" -> "Bu dersi Pasif yapmak üzeresiniz. Pasif hale getirilen ders yeni öğrenci kabul etmez ve öğrenciler tarafından seçilemez. Devam etmek istiyor musunuz?";
            case "enrollment" -> "Kayıt silinecek.";
            default -> "Bu öğe silinecek.";
        };
    }

    private void attemptOperation(Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException e) {
            uiExceptionHandler.handle(e);
        }
    }
}
