package org.example.coursetrackingautomation.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.coursetrackingautomation.dto.GradeDTO;
import org.example.coursetrackingautomation.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class StudentDashboardController {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblGpa;

    @FXML
    private TableView<GradeDTO> tableStudentCourses;
    @FXML
    private TableColumn<GradeDTO, String> colCourseCode;
    @FXML
    private TableColumn<GradeDTO, String> colCourseName;
    @FXML
    private TableColumn<GradeDTO, Double> colCredit;
    @FXML
    private TableColumn<GradeDTO, Double> colMidterm;
    @FXML
    private TableColumn<GradeDTO, Double> colFinal;
    @FXML
    private TableColumn<GradeDTO, Double> colAverage;
    @FXML
    private TableColumn<GradeDTO, String> colLetter;
    @FXML
    private TableColumn<GradeDTO, Integer> colAttendance;
    @FXML
    private TableColumn<GradeDTO, String> colStatus;

    // Faz 2'de yazdığınız servisi buraya bağlıyoruz
    @Autowired
    private GradeService gradeService;

    @FXML
    public void initialize() {
        // Tablo Sütunlarını DTO'daki isimlerle eşleştiriyoruz
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        // Şimdilik ders adı yerine kodu gösteriyoruz (İleride CourseEntity'den gelecek)
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseCode")); 
        
        colMidterm.setCellValueFactory(new PropertyValueFactory<>("midtermScore"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colAverage.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        colLetter.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));
        colAttendance.setCellValueFactory(new PropertyValueFactory<>("attendanceCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Veritabanı bağlanana kadar ekran boş kalmasın diye test verisi ekliyoruz
        loadDummyData();
    }

    private void loadDummyData() {
        ObservableList<GradeDTO> list = FXCollections.observableArrayList();
        
        // Örnek: Vizesi 50, Finali 60 olan bir not hesaplatıp ekleyelim
        Double avg = gradeService.calculateAverage(50.0, 60.0);
        String letter = gradeService.determineLetterGrade(avg);
        String status = gradeService.isPassed(letter) ? "GEÇTİ" : "KALDI";

        // GradeDTO(id, isim, dersKodu, vize, final, ortalama, harf, durum, devamsızlık, kritikMi)
        list.add(new GradeDTO(1L, "Zeynep", "CSE101", 50.0, 60.0, avg, letter, status, 2, false));
        list.add(new GradeDTO(2L, "Zeynep", "MATH101", 80.0, 90.0, 86.0, "BA", "GEÇTİ", 0, false));
        
        tableStudentCourses.setItems(list);
        
        // GPA'yı ekrana yaz (Örnek değer)
        lblGpa.setText(String.format("%.2f", avg));
    }
}