package com.example.platform_for_volunteer_projects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.CurrentUser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.*;

public class LoginController {
    @FXML private Button buttonLogin;
    @FXML private Button buttonRegistration;
    @FXML private TextField textFieldMail;
    @FXML private PasswordField textFieldPassword;

    @FXML
    private void handleLoginButton() {
        String email = textFieldMail.getText().trim();
        String password = textFieldPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните все поля!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            String sql = "SELECT id_user, username, email, id_of_role, passwordOfUser FROM users_of_system WHERE email = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String hashedPasswordFromDB = rs.getString("passwordOfUser");

                        if (PasswordUtils.check(password, hashedPasswordFromDB)) {
                            CurrentUser user = new CurrentUser(
                                    rs.getInt("id_user"),
                                    rs.getString("username"),
                                    rs.getString("email"),
                                    rs.getInt("id_of_role")
                            );

                            openMainWindow(user);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный email или пароль");
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Ошибка", "Пользователь с таким email не найден");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при входе в систему: " + e.getMessage());
        }
    }

    private void openMainWindow(CurrentUser user) {
        Stage currentStage = null;
        Stage mainStage = null;

        try {
            // Безопасное получение текущей сцены
            if (buttonLogin != null && buttonLogin.getScene() != null) {
                currentStage = (Stage) buttonLogin.getScene().getWindow();
            }

            String fxmlFile = user.getRoleId() == 1 ? "organizer-view.fxml" : "main-view.fxml";
            URL fxmlUrl = getClass().getResource(fxmlFile);

            if (fxmlUrl == null) {
                throw new IOException("Файл " + fxmlFile + " не найден в ресурсах");
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 600, 400);

            // Получаем контроллер и передаем данные
            Object controller = fxmlLoader.getController();
            if (controller instanceof OrganizerController && user.getRoleId() == 1) {
                ((OrganizerController) controller).initUserData(user);
            } else if (controller instanceof MainController) {
                ((MainController) controller).initUserData(user);
            }

            mainStage = new Stage();
            mainStage.setTitle("Платформа для волонтерских проектов - " + user.getUsername());
            mainStage.setScene(scene);

            // Закрываем текущее окно только после успешного создания нового
            if (currentStage != null) {
                currentStage.close();
            }

            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Закрываем новое окно, если оно было создано, но произошла ошибка
            if (mainStage != null) {
                mainStage.close();
            }
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Не удалось открыть окно: " + e.getMessage() +
                            "\nПроверьте наличие файлов view в ресурсах");
        }
    }

    @FXML
    private void handleRegistrationButton() {
        try {
            closeWindow();
            URL fxmlUrl = getClass().getResource("/com/example/platform_for_volunteer_projects/registration-view.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Cannot find registration-view.fxml");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            Stage registrationStage = new Stage();
            registrationStage.setTitle("Регистрация");
            registrationStage.setScene(scene);
            registrationStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка открытия окна регистрации", e.getMessage());
        }
    }

    private void closeWindow() {
        Stage currentStage = (Stage) buttonLogin.getScene().getWindow();
        currentStage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class PasswordUtils {
        public static String hash(String password) {
            return BCrypt.hashpw(password, BCrypt.gensalt());
        }

        public static boolean check(String plainPassword, String hashedPassword) {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        }
    }
}