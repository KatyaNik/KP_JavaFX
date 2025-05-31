package com.example.platform_for_volunteer_projects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.CurrentUser;
import model.Event; // Исправленный импорт
import javafx.application.Platform;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Arrays;
import java.util.Properties;

import java.io.IOException;
import java.sql.*;
import javax.mail.Authenticator; // Важно: это из javax.mail, а не java.net
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class MainController {
    private CurrentUser currentUser;

    @FXML
    private TableView<Event> tableViewRecords;
    @FXML
    private TableColumn<Event, String> nameColumn;
    @FXML
    private TableColumn<Event, String> descriptionColumn;
    @FXML
    private TableColumn<Event, String> locationColumn;
    @FXML
    private TableColumn<Event, Integer> maxPeopleColumn;
    @FXML
    private Button buttonRecordsMeets;

    @FXML
    private Button buttonLogout;

    @FXML
    private void handleLogout() {
        try {
            // Закрываем текущее окно
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            stage.close();

            // Загружаем окно входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            // Создаем новое окно
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Вход в систему");
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка базы данных", "Не удалось открыть окно входа: " + e.getMessage());
        }
    }
    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private Event selectedEvent;

    public void initUserData(CurrentUser user) {
        this.currentUser = user;
        loadEventsFromDatabase(); // Загружаем события после инициализации пользователя
    }

    @FXML
    public void initialize() {
        if (tableViewRecords == null || nameColumn == null || descriptionColumn == null
                || locationColumn == null || maxPeopleColumn == null || buttonRecordsMeets == null) {
            System.err.println("FXML-элементы не были инъектированы!");
            return;
        }


        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        maxPeopleColumn.setCellValueFactory(cellData -> cellData.getValue().maxPeopleProperty().asObject());

        // Выбор события в таблице
        tableViewRecords.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedEvent = newValue;
                    }
                });

        // Обработчик кнопки "Записаться"
        buttonRecordsMeets.setOnAction(event -> registerForEvent());
    }

    private void loadEventsFromDatabase() {
        String url = "jdbc:postgresql://localhost:5434/kp_java";
        String user = "postgres";
        String password = "1234";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            if (connection == null || connection.isClosed()) {
                showAlert("Ошибка", "Не удалось подключиться к базе данных");
                return;
            }

            String query = "SELECT * FROM events_of_system";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

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
                            resultSet.getString("dataEvent")
                    );
                    eventList.add(event);
                }

                tableViewRecords.setItems(eventList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка базы данных", "Не удалось загрузить события: " + e.getMessage());
        }
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

        try {
            // 1. Запись в БД (ваша существующая логика)
            try {
                // Здесь должна быть логика записи в БД
                String emailContent = String.format(
                        "Уважаемый %s, вы успешно записались на событие: %s\nМесто: %s\nМакс. участников: %d",
                        currentUser.getUsername(),
                        selectedEvent.getName(),
                        selectedEvent.getLocation(),
                        selectedEvent.getMaxPeople()
                );


                showAlert("Успешно", "Вы записались на событие. Подтверждение будет отправлено на вашу почту");
            } catch (Exception e) {
                showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage());
            }
            // 2. Отправка email
            String to = "katyanikitos@gmail.com"; // Предполагаем, что у CurrentUser есть email
            String subject = "Подтверждение регистрации на мероприятие";
            String body = String.format(
                    "Уважаемый %s,\n\n" +
                            "Вы успешно зарегистрировались на мероприятие:\n" +
                            "Название: %s\n" +
                            "Описание: %s\n" +
                            "Место проведения: %s\n" +
                            "Дата: %s\n" +
                            "Максимальное количество участников: %d\n\n" +
                            "С уважением,\nКоманда волонтерской платформы",
                    currentUser.getUsername(),
                    selectedEvent.getName(),
                    selectedEvent.getDescription(),
                    selectedEvent.getLocation(),
                    selectedEvent.getDataEvent(),
                    selectedEvent.getMaxPeople()
            );

            // Отправка email в отдельном потоке, чтобы не блокировать UI
            new Thread(() -> {
                try {
                    sendEmail(to, subject, body);
                    Platform.runLater(() ->
                            showAlert("Успешно", "Вы записались на событие. Подтверждение отправлено на " + to)
                    );
                } catch (Exception e) {
                    Platform.runLater(() ->
                            showAlert("Ошибка", "Не удалось отправить письмо: " + e.getMessage())
                    );
                }
            }).start();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        final String username = "katyanikitos@gmail.com"; // Ваш Gmail адрес
        final String password = "rrcv srhp bloo pugf"; // Пароль приложения

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
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
            System.out.println("Email sent successfully via Gmail!");
        } catch (MessagingException e) {
            System.err.println("Failed to send email via Gmail:");
            e.printStackTrace();
            throw e;
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