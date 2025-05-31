package model;

import javafx.beans.property.*;

public class Event {
    private final IntegerProperty idEvent = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final IntegerProperty maxPeople = new SimpleIntegerProperty();
    private final StringProperty dataEvent = new SimpleStringProperty();
    private final IntegerProperty idType = new SimpleIntegerProperty();
    private final IntegerProperty idStatus = new SimpleIntegerProperty();
    private final IntegerProperty idOrganizer = new SimpleIntegerProperty();

    public Event() {
    }

    public Event(int idEvent, String name, String description, String location,
                 int maxPeople, int idType, int idStatus, int idOrganizer,
                 String dataEvent) {
        setIdEvent(idEvent);
        setName(name);
        setDescription(description);
        setLocation(location);
        setMaxPeople(maxPeople);
        setIdType(idType);
        setIdStatus(idStatus);
        setIdOrganizer(idOrganizer);
        setDataEvent(dataEvent);
    }

    // Геттеры и сеттеры
    public int getIdEvent() { return idEvent.get(); }
    public void setIdEvent(int idEvent) { this.idEvent.set(idEvent); }
    public IntegerProperty idEventProperty() { return idEvent; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }
    public StringProperty locationProperty() { return location; }

    public int getMaxPeople() { return maxPeople.get(); }
    public void setMaxPeople(int maxPeople) { this.maxPeople.set(maxPeople); }
    public IntegerProperty maxPeopleProperty() { return maxPeople; }

    public String getDataEvent() { return dataEvent.get(); }
    public void setDataEvent(String dataEvent) { this.dataEvent.set(dataEvent); }
    public StringProperty dataEventProperty() { return dataEvent; }

    public int getIdType() { return idType.get(); }
    public void setIdType(int idType) { this.idType.set(idType); }
    public IntegerProperty idTypeProperty() { return idType; }

    public int getIdStatus() { return idStatus.get(); }
    public void setIdStatus(int idStatus) { this.idStatus.set(idStatus); }
    public IntegerProperty idStatusProperty() { return idStatus; }

    public int getIdOrganizer() { return idOrganizer.get(); }
    public void setIdOrganizer(int idOrganizer) { this.idOrganizer.set(idOrganizer); }
    public IntegerProperty idOrganizerProperty() { return idOrganizer; }

    // Методы для работы с датой (используем dataEvent как дату)
    public String getDate() {
        // Здесь можно добавить форматирование даты при необходимости
        return getDataEvent();
    }
    public StringProperty dateProperty() { return dataEventProperty(); }

    // Методы для времени (поскольку в БД нет отдельного поля, можно установить фиксированное значение)
    public String getTime() { return "14:00"; } // Пример фиксированного времени
    public StringProperty timeProperty() { return new SimpleStringProperty(getTime()); }

    // Методы для статуса (используем id_status_of_event)
    public String getStatus() {
        switch(getIdStatus()) {
            case 1: return "Подтверждено";
            case 2: return "Ожидание";
            case 3: return "Отклонено";
            default: return "Неизвестно";
        }
    }
    public StringProperty statusProperty() { return new SimpleStringProperty(getStatus()); }

    @Override
    public String toString() {
        return getName();
    }
}