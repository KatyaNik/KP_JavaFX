package com.example.platform_for_volunteer_projects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.CurrentUser;

import java.io.IOException;
import java.sql.*;

public class OrganizerController {
    @FXML
    private TableView<Event> tableViewMeetings;
    @FXML private TableColumn<Event, String> column1;
    @FXML private TableColumn<Event, String> column2;

    @FXML private TextField textFieldNameMeet;
    @FXML private TextArea textAreaDescriptionMeets;
    @FXML private TextField textFieldLocationMeets;
    @FXML private TextField textFieldParticipantsMeets;
    @FXML private ComboBox<String> comboBoxCategoryMeets;
    @FXML private ComboBox<String> comboBoxStatysMeets;
    @FXML private Button buttonAddMeets;
    @FXML private TextField textFieldPTimeToMeet;
    @FXML private TableView<Event> eventsTable; // Изменено с tableViewMyEvents
    @FXML private Button buttonDeleteMeets;
    // Компоненты для вкладки "Мои события"
    @FXML public TableView<Event> tableViewMeetings1;
    @FXML private TableColumn<Event, String> meetingNameColumn1;
    @FXML private TableColumn<Event, String> meetingDateColumn1;
    @FXML
    private Tab tabLenta; // Добавляем ссылку на вкладку "Лента"
    public Tab tabMyEvents; // Добавляем ссылку на вкладку "Лента"
    @FXML
    private TableView<Event> tableViewNews; // Таблица в разделе "Лента"
    @FXML
    private TableColumn<Event, String> newsColumn1; // Первая колонка
    @FXML
    private TableColumn<Event, String> newsColumn2; // Вторая колонка
    private int editingEventId = -1; // -1 означает, что ничего не редактируется
    @FXML public CurrentUser currentUser;
    private ObservableList<Event> eventsData = FXCollections.observableArrayList();

    public void initUserData(CurrentUser user) {
        this.currentUser = user;
        loadEventsFromDatabase(); // Теперь загружаем события только после установки пользователя

        // Также обновим ComboBox'ы здесь
        try (Connection conn = getConnection()) {
            // Загрузка типов событий
            String typeSql = "SELECT typeName FROM type_of_event";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(typeSql)) {
                comboBoxCategoryMeets.getItems().clear();
                while (rs.next()) {
                    comboBoxCategoryMeets.getItems().add(rs.getString("typeName"));
                }
            }

            // Загрузка статусов
            String statusSql = "SELECT status FROM type_of_status";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(statusSql)) {
                comboBoxStatysMeets.getItems().clear();
                while (rs.next()) {
                    comboBoxStatysMeets.getItems().add(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке данных: " + e.getMessage());
        }
    }
    private void loadUserEvents() {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Пользователь не авторизован");
            return;
        }

        ObservableList<Event> userEvents = FXCollections.observableArrayList();

        try (Connection conn = getConnection()) {
            String sql = "SELECT id_event, name_of_event, dataEvent FROM events_of_system " +
                    "WHERE id_organizer = ? ORDER BY dataEvent DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    userEvents.add(new Event(
                            rs.getInt("id_event"),
                            rs.getString("dataEvent"),
                            rs.getString("name_of_event")
                    ));
                }

                tableViewMeetings.setItems(userEvents);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке событий: " + e.getMessage());
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
            showAlert("Ошибка", "Не удалось открыть окно входа", e.getMessage());
        }
    }
    public void initialize() {
        // Инициализация колонок таблиц (с проверкой на null)
        if (meetingNameColumn1 != null) meetingNameColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (meetingDateColumn1 != null) meetingDateColumn1.setCellValueFactory(new PropertyValueFactory<>("dataEvent"));
        if (column1 != null) column1.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (column2 != null) column2.setCellValueFactory(new PropertyValueFactory<>("dataEvent"));
        if (newsColumn1 != null) newsColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (newsColumn2 != null) newsColumn2.setCellValueFactory(new PropertyValueFactory<>("dataEvent"));



        // Слушатель для вкладки "Лента"
        if (tabLenta != null) {
            tabLenta.setOnSelectionChanged(event -> {
                if (tabLenta.isSelected()) loadNewsFromDatabase();
            });
        }

        // Слушатель кнопки удаления
        if (buttonDeleteMeets != null) {
            buttonDeleteMeets.setOnAction(event -> buttonDeleteMeetsOnAction());
        }

        // Слушатель выбора в таблице событий
        if (eventsTable != null) {
            eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
                if (selected != null) fillFormWithSelectedEvent(selected);
            });
        }

        // Очистка ComboBox
        if (comboBoxCategoryMeets != null) comboBoxCategoryMeets.getItems().clear();
        if (comboBoxStatysMeets != null) comboBoxStatysMeets.getItems().clear();
    }
    private void loadNewsFromDatabase() {
        ObservableList<Event> newsData = FXCollections.observableArrayList();

        try (Connection conn = getConnection()) {
            String sql = "SELECT id_event, name_of_event, dataEvent FROM events_of_system ORDER BY dataEvent DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    newsData.add(new Event(
                            rs.getInt("id_event"),
                            rs.getString("dataEvent"),
                            rs.getString("name_of_event")
                    ));
                }

                tableViewNews.setItems(newsData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке новостей: " + e.getMessage());
        }

    }
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234");
    }
    @FXML
    public void buttonDeleteMeetsOnAction() {
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert("Ошибка", "Не выбрано мероприятие", "Пожалуйста, выберите мероприятие для удаления.");
            return;
        }

        try (Connection conn = getConnection()) {
            // Проверяем, что событие принадлежит текущему пользователю
            String checkSql = "SELECT 1 FROM events_of_system WHERE id_event = ? AND id_organizer = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, selectedEvent.getId());
                checkStmt.setInt(2, currentUser.getId());

                if (!checkStmt.executeQuery().next()) {
                    showAlert("Ошибка", "Доступ запрещен", "Вы не можете удалить это мероприятие.");
                    return;
                }
            }

            // SQL запрос для удаления записи
            String sql = "DELETE FROM events_of_system WHERE id_event = ? AND id_organizer = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedEvent.getId());
                pstmt.setInt(2, currentUser.getId());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert("Успех", "Мероприятие удалено", "Мероприятие было успешно удалено из базы данных.");
                    loadEventsFromDatabase();
                } else {
                    showAlert("Ошибка", "Не удалось удалить мероприятие", "Мероприятие не было удалено из базы данных.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка базы данных", "Произошла ошибка при удалении мероприятия: " + e.getMessage());
        }
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void fillFormWithSelectedEvent(Event selectedEvent) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM events_of_system WHERE id_event = ? AND id_organizer = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedEvent.getId());
                pstmt.setInt(2, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    textFieldNameMeet.setText(rs.getString("name_of_event"));
                    textAreaDescriptionMeets.setText(rs.getString("description"));
                    textFieldLocationMeets.setText(rs.getString("location_of_event"));
                    textFieldParticipantsMeets.setText(String.valueOf(rs.getInt("max_people")));
                    textFieldPTimeToMeet.setText(rs.getString("dataEvent"));

                    // Установка значений в ComboBox
                    comboBoxCategoryMeets.getSelectionModel().select(rs.getString("id_type_of_event"));
                    comboBoxStatysMeets.getSelectionModel().select(rs.getString("id_status_of_event"));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Событие не найдено или у вас нет прав на его редактирование");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке данных события: " + e.getMessage());
        }
    }

    @FXML
    public void handleRed(){
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите событие для редактирования");
            return;
        }

        // Проверяем, что событие принадлежит текущему пользователю
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT 1 FROM events_of_system WHERE id_event = ? AND id_organizer = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, selectedEvent.getId());
                checkStmt.setInt(2, currentUser.getId());

                if (!checkStmt.executeQuery().next()) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Вы не можете редактировать это мероприятие.");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при проверке прав: " + e.getMessage());
            return;
        }
        // Получаем данные из полей ввода
        String name = textFieldNameMeet.getText().trim();
        String description = textAreaDescriptionMeets.getText().trim();
        String location = textFieldLocationMeets.getText().trim();
        String participants = textFieldParticipantsMeets.getText().trim();
        String category = comboBoxCategoryMeets.getValue();
        String status = comboBoxStatysMeets.getValue();
        String date = textFieldPTimeToMeet.getText().trim();

        // Проверка заполнения обязательных полей
        if (name.isEmpty() || location.isEmpty() || category == null || status == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните обязательные поля!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            // Проверка валидности числовых полей
            int maxParticipants;
            try {
                maxParticipants = participants.isEmpty() ? 0 : Integer.parseInt(participants);
                if (maxParticipants < 0) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Количество участников не может быть отрицательным");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректное количество участников");
                return;
            }

            // Получаем ID типа события и статуса
            int typeId = getTypeIdByName(category, conn);
            int statusId = getStatusIdByName(status, conn);

            // SQL для обновления
            String sql = "UPDATE events_of_system SET name_of_event = ?, description = ?, " +
                    "location_of_event = ?, max_people = ?, id_type_of_event = ?, " +
                    "id_status_of_event = ?, dataEvent = ? WHERE id_event = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, description.isEmpty() ? null : description);
                pstmt.setString(3, location);
                pstmt.setInt(4, maxParticipants);
                pstmt.setInt(5, typeId);
                pstmt.setInt(6, statusId);
                pstmt.setString(7, date);
                pstmt.setInt(8, selectedEvent.getId()); // Предполагается, что у Event есть поле id

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Событие успешно обновлено!");
                    loadEventsFromDatabase();
                    clearFields();
                    editingEventId = -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при обновлении события: " + e.getMessage());
        }
    }
    private int getTypeIdByName(String typeName, Connection conn) throws SQLException {
        String sql = "SELECT id_type FROM type_of_event WHERE typeName = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, typeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_type");
            }
        }
        throw new SQLException("Тип события не найден");
    }

    private int getStatusIdByName(String statusName, Connection conn) throws SQLException {
        String sql = "SELECT id_status FROM type_of_status WHERE status = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, statusName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_status");
            }
        }
        throw new SQLException("Статус не найден");
    }
    @FXML
    private void handleAddEvent() {
        // Получаем данные из полей ввода
        String name = textFieldNameMeet.getText().trim();
        String description = textAreaDescriptionMeets.getText().trim();
        String location = textFieldLocationMeets.getText().trim();
        String participants = textFieldParticipantsMeets.getText().trim();
        String category = comboBoxCategoryMeets.getValue();
        String data = textFieldPTimeToMeet.getText().trim();

        // Проверка заполнения обязательных полей
        if (name.isEmpty() ||  location.isEmpty() || category == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните обязательные поля!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            // Проверка валидности числовых полей перед вставкой
            int maxParticipants = 0;
            try {
                maxParticipants = participants.isEmpty() ? 0 : Integer.parseInt(participants);
                if (maxParticipants < 0) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Количество участников не может быть отрицательным");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректное количество участников");
                return;
            }

            // Проверка обязательных полей
            if (name == null || name.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Название события обязательно для заполнения");
                return;
            }

            String sql = "INSERT INTO events_of_system (name_of_event, description, location_of_event, max_people, " +
                    "id_type_of_event, id_status_of_event, id_organizer, dataEvent) " +  // Добавлено start_date
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name.trim());
                pstmt.setString(2, description != null ? description.trim() : null);
                pstmt.setString(3, location != null ? location.trim() : null);
                pstmt.setInt(4, maxParticipants);
                pstmt.setInt(5, 1); // id_type_of_event
                pstmt.setInt(6, 1); // id_status_of_event (по умолчанию "Активно")
                pstmt.setInt(7, currentUser.getId());
                pstmt.setString(8, data.trim());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int eventId = generatedKeys.getInt(1);
                            System.out.println("Создано событие с ID: " + eventId);
                        }
                    }

                    loadEventsFromDatabase();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Событие успешно добавлено!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка",
                    "Ошибка при добавлении события: " + e.getMessage() +
                            "\nПроверьте, что все обязательные поля заполнены корректно");
        }
    }

    private void loadEventsFromDatabase() {
        if (currentUser == null) {
            System.out.println("Ошибка: currentUser не установлен");
            return;
        }

        ObservableList<Event> eventsData = FXCollections.observableArrayList();

        try (Connection conn = getConnection()) {
            String sql = "SELECT id_event, dataEvent, name_of_event FROM events_of_system " +
                    "WHERE id_organizer = ? ORDER BY dataEvent";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    eventsData.add(new Event(
                            rs.getInt("id_event"),
                            rs.getString("dataEvent"),
                            rs.getString("name_of_event")
                    ));
                }
                eventsTable.setItems(eventsData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке событий: " + e.getMessage());
        }
    }

    private void clearFields() {
        if (textFieldNameMeet != null) textFieldNameMeet.clear();
        if (textAreaDescriptionMeets != null) textAreaDescriptionMeets.clear();
        if (textFieldLocationMeets != null) textFieldLocationMeets.clear();
        if (textFieldParticipantsMeets != null) textFieldParticipantsMeets.clear();
        if (comboBoxCategoryMeets != null) comboBoxCategoryMeets.getSelectionModel().clearSelection();
        if (comboBoxStatysMeets != null) comboBoxStatysMeets.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Event {
        private final String dataEvent;
        private final int id;
        private final String name;

        public Event(int id, String dataEvent, String name) {
            this.id = id;
            this.dataEvent = dataEvent;
            this.name = name;
        }
        public int getId() {
            return id;
        }
        public String getDataEvent() {
            return dataEvent;
        }

        public String getName() {
            return name;
        }
    }
}
