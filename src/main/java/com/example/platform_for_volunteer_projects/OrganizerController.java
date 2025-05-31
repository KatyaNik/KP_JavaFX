package com.example.platform_for_volunteer_projects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CurrentUser;

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

    @FXML
    private Tab tabLenta; // Добавляем ссылку на вкладку "Лента"
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
    }

    @FXML
    public void initialize() {
        // Инициализация колонок таблицы новостей
        newsColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));
        newsColumn2.setCellValueFactory(new PropertyValueFactory<>("dataEvent"));

        // Добавляем слушатель для вкладки "Лента" (проверяем, что tabLenta не null)
        if (tabLenta != null) {
            tabLenta.setOnSelectionChanged(event -> {
                if (tabLenta.isSelected()) {
                    loadNewsFromDatabase();
                }
            });
        }

        buttonDeleteMeets.setOnAction(event -> buttonDeleteMeetsOnAction());

        // Инициализация столбцов таблицы
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));
        column2.setCellValueFactory(new PropertyValueFactory<>("dataEvent"));

        // Добавьте слушатель выбора строки
        eventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFormWithSelectedEvent(newSelection);
            }
        });

        // Загрузка типов событий из БД
        try (Connection conn = getConnection()) {
            // Загрузка типов событий
            String typeSql = "SELECT typeName FROM type_of_event";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(typeSql)) {
                while (rs.next()) {
                    comboBoxCategoryMeets.getItems().add(rs.getString("typeName"));
                }
            }

            // Загрузка статусов
            String statusSql = "SELECT status FROM type_of_status";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(statusSql)) {
                while (rs.next()) {
                    comboBoxStatysMeets.getItems().add(rs.getString("status"));
                }
            }

            // Загрузка данных событий
            loadEventsFromDatabase();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке данных: " + e.getMessage());
        }
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
        // Получаем выбранное мероприятие из таблицы
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();

        if (selectedEvent == null) {
            showAlert("Ошибка", "Не выбрано мероприятие", "Пожалуйста, выберите мероприятие для удаления.");
            return;
        }

        try (Connection conn = getConnection()) {
            // SQL запрос для удаления записи
            String sql = "DELETE FROM events_of_system WHERE id_event = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedEvent.getId());

                // Выполняем запрос
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    showAlert("Успех", "Мероприятие удалено", "Мероприятие было успешно удалено из базы данных.");
                    // Обновляем таблицу после удаления
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
            String sql = "SELECT * FROM events_of_system WHERE name_of_event = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, selectedEvent.getName());
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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при загрузке данных события: " + e.getMessage());
        }
    }

    @FXML
    public void handleRed(){
        // Проверяем, выбрана ли строка для редактирования
        Event selectedEvent = eventsTable.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите событие для редактирования");
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
        ObservableList<Event> eventsData = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5434/kp_java",
                "postgres",
                "1234")) {

            String sql = "SELECT id_event, dataEvent, name_of_event FROM events_of_system ORDER BY dataEvent";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

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
