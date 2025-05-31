package com.example.platform_for_volunteer_projects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CurrentUser;
import model.Event; // Исправленный импорт

import java.sql.*;

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
            // Здесь должна быть логика записи в БД
            String emailContent = String.format(
                    "Уважаемый %s, вы успешно записались на событие: %s\nМесто: %s\nМакс. участников: %d",
                    currentUser.getUsername(),
                    selectedEvent.getName(),
                    selectedEvent.getLocation(),
                    selectedEvent.getMaxPeople()
            );

            // В реальном приложении здесь будет отправка email
            System.out.println("Отправка email:\n" + emailContent);

            showAlert("Успешно", "Вы записались на событие. Подтверждение будет отправлено на вашу почту");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage());
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