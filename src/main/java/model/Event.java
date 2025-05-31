package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

    // Конструкторы
    public Event() {
        // Пустой конструктор для JavaFX
    }

    public Event(int idEvent, String name, String description, String location,
                 int maxPeople, int idType, int idStatus, int idOrganizer, String dataEvent) {
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

    // Геттеры и сеттеры для idEvent
    public int getIdEvent() { return idEvent.get(); }
    public IntegerProperty idEventProperty() { return idEvent; }
    public void setIdEvent(int idEvent) { this.idEvent.set(idEvent); }
    public void setDataEvent (String dataEvent){this.dataEvent.set(dataEvent);}

    // Геттеры и сеттеры для name
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    // Геттеры и сеттеры для description
    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }
    public void setDescription(String description) { this.description.set(description); }

    // Геттеры и сеттеры для location
    public String getLocation() { return location.get(); }
    public StringProperty locationProperty() { return location; }
    public void setLocation(String location) { this.location.set(location); }

    // Геттеры и сеттеры для maxPeople
    public int getMaxPeople() { return maxPeople.get(); }
    public IntegerProperty maxPeopleProperty() { return maxPeople; }
    public void setMaxPeople(int maxPeople) { this.maxPeople.set(maxPeople); }

    // Геттеры и сеттеры для idType
    public int getIdType() { return idType.get(); }
    public IntegerProperty idTypeProperty() { return idType; }
    public void setIdType(int idType) { this.idType.set(idType); }

    // Геттеры и сеттеры для idStatus
    public int getIdStatus() { return idStatus.get(); }
    public IntegerProperty idStatusProperty() { return idStatus; }
    public void setIdStatus(int idStatus) { this.idStatus.set(idStatus); }

    // Геттеры и сеттеры для idOrganizer
    public int getIdOrganizer() { return idOrganizer.get(); }
    public String getDataEvent(){return dataEvent.get();}
    public IntegerProperty idOrganizerProperty() { return idOrganizer; }
    public void setIdOrganizer(int idOrganizer) { this.idOrganizer.set(idOrganizer); }

    @Override
    public String toString() {
        return "Event{" +
                "idEvent=" + getIdEvent() +
                ", name=" + getName() +
                ", location=" + getLocation() +
                ", maxPeople=" + getMaxPeople() +
                ", dataEvent=" + getDataEvent() +
                '}';
    }
}
