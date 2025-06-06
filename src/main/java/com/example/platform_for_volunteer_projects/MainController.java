package com.example.platform_for_volunteer_projects;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

/**
 * Главный контроллер для управления основным интерфейсом платформы волонтерских проектов.
 * Обеспечивает отображение событий, регистрацию на мероприятия, генерацию сертификатов и другие функции.
 */
public class MainController {
    private CurrentUser currentUser;
    private Event selectedEvent;
    private Event selectedMeeting;
    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private ObservableList<Event> newsList = FXCollections.observableArrayList();
    private String searchText = "";

    @FXML
    TableView<Event> tableViewRecords;
    @FXML
    TableColumn<Event, String> nameColumn;
    @FXML
    TableColumn<Event, String> descriptionColumn;
    @FXML
    TableColumn<Event, String> locationColumn;
    @FXML
    TableColumn<Event, Integer> maxPeopleColumn;
    @FXML private Button buttonRecordsMeets;
    @FXML private Button buttonLogout;
    @FXML
    TableView<Event> tableViewMeetings;
    @FXML private TableColumn<Event, String> meetingNameColumn;
    @FXML private TableColumn<Event, String> meetingDateColumn;
    @FXML private TableColumn<Event, String> meetingTimeColumn;
    @FXML private TableColumn<Event, String> meetingStatusColumn;

    @FXML
    TableView<Event> tableViewNews;
    @FXML private TableColumn<Event, String> newsTitleColumn;
    @FXML private TableColumn<Event, String> newsDateColumn;
    @FXML private TableColumn<Event, String> newsLocationColumn;
    @FXML private TableColumn<Event, String> newsStatusColumn;
    @FXML private TextField searchField;

    private ObservableList<Event> allEvents = FXCollections.observableArrayList();
    private FilteredList<Event> filteredEvents = new FilteredList<>(allEvents);

    // Геттеры и сеттеры для тестирования
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public Event getSelectedMeeting() {
        return selectedMeeting;
    }

    public void setSelectedMeeting(Event selectedMeeting) {
        this.selectedMeeting = selectedMeeting;
    }

    public ObservableList<Event> getEventList() {
        return eventList;
    }

    public void setEventList(ObservableList<Event> eventList) {
        this.eventList = eventList;
    }

    public ObservableList<Event> getNewsList() {
        return newsList;
    }

    public void setNewsList(ObservableList<Event> newsList) {
        this.newsList = newsList;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
        updateFilteredEvents();
    }

    public ObservableList<Event> getFilteredEvents() {
        return filteredEvents;
    }

    private void updateFilteredEvents() {
        filteredEvents.setPredicate(event -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return event.getName().toLowerCase().contains(lowerCaseFilter) ||
                   event.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                   event.getLocation().toLowerCase().contains(lowerCaseFilter);
        });
    }

    /**
     * Инициализирует контроллер после загрузки FXML-файла.
     * Настраивает таблицы, загружает данные из базы данных и устанавливает обработчики событий.
     */
    @FXML
    public void initialize() {
        // Инициализация таблицы
        newsTitleColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        newsDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        newsLocationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        newsStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Загрузка данных из БД
        loadEventsFromDatabase();

        // Установка фильтрованного списка в таблицу
        tableViewNews.setItems(filteredEvents);
        initializeMeetingsTable();
        initializeRecordsTable();
        buttonRecordsMeets.setOnAction(event -> registerForEvent());
    }

    /**
     * Инициализирует данные текущего пользователя.
     *
     * @param user объект CurrentUser, представляющий текущего пользователя.
     */
    public void initUserData(CurrentUser user) {
        this.currentUser = user;
        loadEventsFromDatabase();
        loadUserMeetingsFromDatabase(); // Загружаем мероприятия пользователя
    }

    /**
     * Обрабатывает поиск событий по введенному тексту.
     * Фильтрует список событий и обновляет таблицу.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();

        filteredEvents.setPredicate(event -> {
            if (searchText == null || searchText.isEmpty()) {
                return true; // Показывать все события если поиск пустой
            }

            // Проверяем содержит ли название события искомый текст
            return event.getName().toLowerCase().contains(searchText);
        });
    }

    /**
     * Загружает мероприятия текущего пользователя из базы данных.
     */
    private void loadUserMeetingsFromDatabase() {
        String url = "jdbc:postgresql://localhost:5434/kp_java";
        String user = "postgres";
        String password = "1234";

        if (currentUser == null) {
            return;
        }

        String sql = "SELECT e.* FROM events_of_system e " +
                "JOIN user_events ue ON e.id_event = ue.event_id " +
                "WHERE ue.user_id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, currentUser.getId());
            ResultSet resultSet = statement.executeQuery();

            ObservableList<Event> userEvents = FXCollections.observableArrayList();

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
                userEvents.add(event);
            }

            tableViewMeetings.setItems(userEvents);

        } catch (SQLException e) {
            showAlert("Ошибка базы данных", "Не удалось загрузить мероприятия пользователя: " + e.getMessage());
        }
    }

    /**
     * Инициализирует таблицу мероприятий пользователя.
     */
    private void initializeMeetingsTable() {
        meetingNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        meetingDateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        meetingTimeColumn.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        meetingStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        tableViewMeetings.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectedMeeting = newValue);
    }

    /**
     * Инициализирует таблицу записей на события.
     */
    private void initializeRecordsTable() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        maxPeopleColumn.setCellValueFactory(cellData -> cellData.getValue().maxPeopleProperty().asObject());

        tableViewRecords.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectedEvent = newValue);
    }

    /**
     * Загружает список всех событий из базы данных.
     */
    private void loadEventsFromDatabase() {
        String url = "jdbc:postgresql://localhost:5434/kp_java";
        String user = "postgres";
        String password = "1234";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM events_of_system")) {

            eventList.clear();
            allEvents.clear(); // Очищаем список всех событий

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
                allEvents.add(event); // Добавляем событие в оба списка
            }

            tableViewRecords.setItems(eventList);
            tableViewMeetings.setItems(eventList);
            tableViewNews.setItems(filteredEvents); // Убедитесь, что таблица news использует filteredEvents

        } catch (SQLException e) {
            showAlert("Ошибка базы данных", "Не удалось загрузить события: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает выход пользователя из системы.
     * Закрывает текущее окно и открывает окно входа.
     */
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

    /**
     * Генерирует сертификат для выбранного мероприятия.
     * Позволяет пользователю выбрать место для сохранения сертификата.
     */
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

    /**
     * Создает Word-документ сертификата.
     *
     * @param filePath путь для сохранения файла.
     * @param recipientName имя получателя сертификата.
     * @param eventName название мероприятия.
     * @param date дата мероприятия.
     * @throws IOException если произошла ошибка при создании файла.
     */
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

    /**
     * Генерирует уникальный номер сертификата.
     *
     * @return строка с номером сертификата.
     */
    private String generateCertificateNumber() {
        return "CERT-" + System.currentTimeMillis();
    }

    /**
     * Регистрирует текущего пользователя на выбранное событие.
     * Обновляет базу данных, отправляет подтверждение по email.
     */
    void registerForEvent() {
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
                String url = "jdbc:postgresql://localhost:5434/kp_java";
                String dbUser = "postgres";
                String dbPassword = "1234";

                String sql = "INSERT INTO user_events (user_id, event_id) VALUES (?, ?)";

                try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                     PreparedStatement statement = connection.prepareStatement(sql)) {

                    statement.setInt(1, currentUser.getId());
                    statement.setInt(2, selectedEvent.getIdEvent());
                    statement.executeUpdate();

                    // Обновляем список мероприятий пользователя
                    Platform.runLater(this::loadUserMeetingsFromDatabase);

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
                } catch (SQLException e) {
                    Platform.runLater(() ->
                            showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage())
                    );
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                        showAlert("Ошибка", "Не удалось выполнить запись: " + e.getMessage())
                );
            }
        }).start();
    }

    /**
     * Отправляет email с указанным содержимым.
     *
     * @param to адрес получателя.
     * @param subject тема письма.
     * @param body содержимое письма.
     * @throws MessagingException если произошла ошибка при отправке email.
     */
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

    /**
     * Отображает информационное окно с заданным заголовком и сообщением.
     *
     * @param title заголовок окна.
     * @param message текст сообщения.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}