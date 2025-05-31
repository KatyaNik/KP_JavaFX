package com.example.platform_for_volunteer_projects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Авторизация");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // Инициализация базы данных
        initDatabase();

        // Запуск JavaFX приложения
        launch(args);
    }

    private static void initDatabase() {
        String url = "jdbc:postgresql://localhost:5434/kp_java";
        String user = "postgres";
        String password = "1234";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Создание таблицы (пример)

            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Database initialization failed:");
            e.printStackTrace();
        }
    }
}