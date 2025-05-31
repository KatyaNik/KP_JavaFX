package com.example.platform_for_volunteer_projects;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.CurrentUser;
import model.Event;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class MainController {
    private CurrentUser currentUser;
    private Event selectedEvent;
    private Event selectedMeeting;
    private ObservableList<Event> eventList = FXCollections.observableArrayList();

    @FXML private TableView<Event> tableViewRecords;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> descriptionColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, Integer> maxPeopleColumn;
    @FXML private Button buttonRecordsMeets;
    @FXML private Button buttonLogout;
    @FXML private TableView<Event> tableViewMeetings;
    @FXML private TableColumn<Event, String> meetingNameColumn;
    @FXML private TableColumn<Event, String> meetingDateColumn;
    @FXML private TableColumn<Event, String> meetingTimeColumn;
    @FXML private TableColumn<Event, String> meetingStatusColumn;

    @FXML
    public void initialize() {
        initializeMeetingsTable();
        initializeRecordsTable();
        buttonRecordsMeets.setOnAction(event -> registerForEvent());
    }

    public void initUserData(CurrentUser user) {
        this.currentUser = user;
        loadEventsFromDatabase();
    }

    private void initializeMeetingsTable() {
        meetingNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        meetingDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        meetingTimeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        meetingStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tableViewMeetings.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectedMeeting = newValue);
    }

    private void initializeRecordsTable() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        maxPeopleColumn.setCellValueFactory(cellData -> cellData.getValue().maxPeopleProperty().asObject());

        tableViewRecords.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectedEvent = newValue);
    }

    private void loadEventsFromDatabase() {
        String url = "jdbc:postgresql://localhost:5434/kp_java";
        String user = "postgres";
        String password = "1234";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM events_of_system")) {

            eventList.clear();
            while (resultSet.next()) {
                Event event = new Event(
                        resultSet.getInt("id_event"),
                        resultSet.getString("name_of_event"),
                        resultSet.getString("description"),
                        resultSet.getString("location_of_event"),
                        resultSet.getInt("max_people"),
                        resultSet.getInt("id_type_of_event"),
                        resultSet.getInt("id_status_of_event"),
                        resultSet.getInt("id_organizer"),
                        resultSet.getString("dataevent")
                );
                eventList.add(event);
            }

            tableViewRecords.setItems(eventList);
            tableViewMeetings.setItems(eventList);

        } catch (SQLException e) {
            showAlert("Ошибка базы данных", "Не удалось загрузить события: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            stage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Вход в систему");
            loginStage.show();
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось открыть окно входа: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateCertificate() {
        if (selectedMeeting == null) {
            showAlert("Ошибка", "Пожалуйста, выберите мероприятие из списка");
            return;
        }

        if (currentUser == null) {
            showAlert("Ошибка", "Пользователь не авторизован");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить сертификат");
        fileChooser.setInitialFileName("Сертификат_" + currentUser.getUsername() + ".docx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word документы", "*.docx"));

        File file = fileChooser.showSaveDialog(tableViewMeetings.getScene().getWindow());
        if (file != null) {
            try {
                createWordCertificate(file.getAbsolutePath(), currentUser.getUsername(),
                        selectedMeeting.getName(), selectedMeeting.getDate());
                showAlert("Успешно", "Сертификат сохранен: " + file.getName());
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось создать сертификат: " + e.getMessage());
            }
        }
    }

    private void createWordCertificate(String filePath, String recipientName,
                                       String eventName, String date) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Стили оформления
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setText("СЕРТИФИКАТ");
            titleRun.addBreak();

            // Основное содержание
            XWPFParagraph contentParagraph = document.createParagraph();
            contentParagraph.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun contentRun = contentParagraph.createRun();
            contentRun.setFontSize(14);
            contentRun.setText("Настоящим удостоверяется, что");
            contentRun.addBreak();

            XWPFRun nameRun = contentParagraph.createRun();
            nameRun.setBold(true);
            nameRun.setFontSize(16);
            nameRun.setText(recipientName);
            nameRun.addBreak();

            XWPFRun eventRun = contentParagraph.createRun();
            eventRun.setFontSize(14);
            eventRun.setText("успешно завершил(а) участие в мероприятии: " + eventName);
            eventRun.addBreak();

            XWPFRun dateRun = contentParagraph.createRun();
            dateRun.setFontSize(14);
            dateRun.setText("Дата: " + date);
            dateRun.addBreak();

            XWPFRun numberRun = contentParagraph.createRun();
            numberRun.setFontSize(14);
            numberRun.setText("Номер сертификата: " + generateCertificateNumber());

            // Сохраняем документ
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                document.write(out);
            }
        }
    }

    private String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis();
    }

    private void registerForEvent() {
        if (selectedEvent == null) {
            showAlert("Ошибка", "Пожалуйста, выберите событие из списка");
            return;
        }

        if (currentUser == null) {
            showAlert("Ошибка", "Пользователь не авторизован");
            return;
        }

        new Thread(() -> {
            try {
                // Логика записи в БД
                String emailContent = String.format(
                        "Уважаемый %s, вы успешно записались на событие: %s\nМесто: %s\nМакс. участников: %d",
                        currentUser.getUsername(),
                        selectedEvent.getName(),
                        selectedEvent.getLocation(),
                        selectedEvent.getMaxPeople()
                );

                // Отправка email
                sendEmail(currentUser.getEmail(), "Подтверждение регистрации", emailContent);

                Platform.runLater(() ->
                        showAlert("Успешно", "Вы записались на событие. Подтверждение отправлено")
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage())
                );
            }
        }).start();
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        final String username = "katyanikitos@gmail.com";
        final String password = "rrcv srhp bloo pugf";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Ошибка отправки email: " + e.getMessage(), e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}