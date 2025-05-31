package com.example.platform_for_volunteer_projects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;

public class RegistrationController {
    @FXML public TextField regNameField;
    @FXML public TextField regEmailField;
    @FXML public PasswordField regPasswordField;
    @FXML public ComboBox<String> roleComboBox;
    @FXML public Button buttonLogin;

    @FXML
    private void handleRegistration() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText().trim();

        // Проверка заполнения полей
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() ||
                roleComboBox.getSelectionModel().getSelectedIndex() == -1) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните все поля!");
            return;
        }

        // Хешируем пароль
        String hashedPassword = PasswordUtils.hash(password);
        int roleIndex = roleComboBox.getSelectionModel().getSelectedIndex();
        // Обычно индексы в БД начинаются с 1, а в ComboBox с 0
        int roleId = roleIndex + 1;

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            // Проверка, что email не занят
            if (isEmailExists(conn, email)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Этот email уже зарегистрирован!");
                return;
            }

            // SQL запрос для вставки данных
            String sql = "INSERT INTO users_of_system (username, email, passwordOfUser, id_of_role) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                pstmt.setInt(4, roleId);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех",
                            "Регистрация прошла успешно!");
                    clearFields();

                    // Переход на основное окно после успешной регистрации
                    openMainWindow();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при регистрации: " + e.getMessage());
        }
    }

    private void openMainWindow() {
        try {
            Stage currentStage = (Stage) buttonLogin.getScene().getWindow();
            currentStage.close();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage mainStage = new Stage();
            mainStage.setTitle("Платформа для волонтерских проектов");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось открыть главное окно: " + e.getMessage());
        }
    }

    public static class PasswordUtils {
        // Хеширование пароля
        public static String hash(String password) {
            return BCrypt.hashpw(password, BCrypt.gensalt());
        }

        // Проверка пароля
        public static boolean check(String password, String hashed) {
            return BCrypt.checkpw(password, hashed);
        }
    }

    private boolean isEmailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT 1 FROM users_of_system WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void clearFields() {
        regNameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}