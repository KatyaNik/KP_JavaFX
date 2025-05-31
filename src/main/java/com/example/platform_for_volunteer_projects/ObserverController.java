package com.example.platform_for_volunteer_projects;

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

import java.io.IOException;
import java.sql.*;

public class ObserverController {
    @FXML
    private TableView<Event> tableViewNews;
    @FXML
    private TableColumn<Event, String> colName;
    @FXML
    private TableColumn<Event, String> colDate;

    @FXML
    public void initialize() {
        // Настройка столбцов таблицы
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Загрузка данных из БД
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        ObservableList<Event> events = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            String sql = "SELECT name_of_event, dataEvent FROM events_of_system ORDER BY dataEvent DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    events.add(new Event(
                            rs.getString("name_of_event"),
                            rs.getString("dataEvent")
                    ));
                }
            }

            tableViewNews.setItems(events);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при загрузке данных", e.getMessage());
        }
    }
    @FXML
    private Button buttonLogout;

    @FXML
    private void handleLogout() {
        try {
            // Закрываем текущее окно
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            stage.close();

            // Загружаем окно авторизации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Авторизация");
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно авторизации", e.getMessage());
        }
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Модель данных для таблицы
    public static class Event {
        private final String name;
        private final String date;

        public Event(String name, String date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }
    }
}