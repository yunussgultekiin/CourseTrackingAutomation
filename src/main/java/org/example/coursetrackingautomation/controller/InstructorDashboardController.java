package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.AttendanceService;
import org.example.coursetrackingautomation.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class InstructorDashboardController {

    @FXML
    private ComboBox<String> comboCourses;
    @FXML
    private TableView<GradeDTO> tableStudents;
    @FXML
    private TableColumn<GradeDTO, Long> colStudentNumber;
    @FXML
    private TableColumn<GradeDTO, String> colFullName;
    @FXML
    private TableColumn<GradeDTO, Double> colMidterm;
    @FXML
    private TableColumn<GradeDTO, Double> colFinal;
    @FXML
    private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML
    private TableColumn<GradeDTO, Double> colAverage;
    @FXML
    private TableColumn<GradeDTO, String> colStatus;
    @FXML
    private Button btnSave;

    @Autowired
    private GradeService gradeService;
    @Autowired
    private AttendanceService attendanceService;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupRowColorFactory(); // RENKLENDİRME MANTIĞI BURADA
        
        // Tabloyu düzenlenebilir yap (Hoca not girebilsin)
        tableStudents.setEditable(true);
        
        // Kaydet butonu
        btnSave.setOnAction(e -> System.out.println("Değişiklikler Veritabanına Kaydedildi!"));

        loadDummyData();
    }

    private void setupTableColumns() {
        colStudentNumber.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendanceCount"));

        // Vize ve Final elle değiştirilebilir olsun (TextFieldTableCell)
        colMidterm.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        colMidterm.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colFinal.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
    }

    // [ÖNEMLİ] Devamsızlık Sınırını Aşanları Kırmızı Yapma Kodu
    private void setupRowColorFactory() {
        tableStudents.setRowFactory(tv -> new TableRow<GradeDTO>() {
            @Override
            protected void updateItem(GradeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    getStyleClass().removeAll("critical-attendance");
                } else {
                    // Eğer kritik sınır aşılmışsa CSS'teki .critical-attendance sınıfını ekle
                    // (Faz 1'de styles.css içine bu sınıfı eklemiştiniz)
                    if (item.isAbsentCritically()) {
                        if (!getStyleClass().contains("critical-attendance")) {
                            getStyleClass().add("critical-attendance");
                        }
                    } else {
                        getStyleClass().removeAll("critical-attendance");
                    }
                }
            }
        });
    }

    private void loadDummyData() {
        ObservableList<GradeDTO> list = FXCollections.observableArrayList();
        
        // Test 1: Devamsızlığı Yüksek Olan Öğrenci (Tabloda KIRMIZI görünmeli)
        GradeDTO student1 = new GradeDTO(101L, "Ahmet Yılmaz", "CSE101", 40.0, 50.0, 46.0, "DD", "GEÇTİ", 10, true);
        
        // Test 2: Normal Öğrenci
        GradeDTO student2 = new GradeDTO(102L, "Ayşe Demir", "CSE101", 80.0, 90.0, 86.0, "BA", "GEÇTİ", 2, false);

        list.add(student1);
        list.add(student2);
        
        tableStudents.setItems(list);
    }
}