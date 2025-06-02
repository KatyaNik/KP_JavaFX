package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    private Event event;
    private static final int TEST_ID = 1;
    private static final String TEST_NAME = "Test Event";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_LOCATION = "Test Location";
    private static final int TEST_MAX_PEOPLE = 100;
    private static final int TEST_TYPE = 1;
    private static final int TEST_STATUS = 1;
    private static final int TEST_ORGANIZER = 1;
    private static final String TEST_DATE = "2024-03-20";

    @BeforeEach
    void setUp() {
        event = new Event(TEST_ID, TEST_NAME, TEST_DESCRIPTION, TEST_LOCATION,
                TEST_MAX_PEOPLE, TEST_TYPE, TEST_STATUS, TEST_ORGANIZER, TEST_DATE);
    }

    @Test
    void testEventConstructorAndGetters() {
        assertEquals(TEST_ID, event.getIdEvent());
        assertEquals(TEST_NAME, event.getName());
        assertEquals(TEST_DESCRIPTION, event.getDescription());
        assertEquals(TEST_LOCATION, event.getLocation());
        assertEquals(TEST_MAX_PEOPLE, event.getMaxPeople());
        assertEquals(TEST_TYPE, event.getIdType());
        assertEquals(TEST_STATUS, event.getIdStatus());
        assertEquals(TEST_ORGANIZER, event.getIdOrganizer());
        assertEquals(TEST_DATE, event.getDataEvent());
    }

    @Test
    void testSetters() {
        Event newEvent = new Event();
        newEvent.setIdEvent(2);
        newEvent.setName("New Event");
        newEvent.setDescription("New Description");
        newEvent.setLocation("New Location");
        newEvent.setMaxPeople(200);
        newEvent.setIdType(2);
        newEvent.setIdStatus(2);
        newEvent.setIdOrganizer(2);
        newEvent.setDataEvent("2024-03-21");

        assertEquals(2, newEvent.getIdEvent());
        assertEquals("New Event", newEvent.getName());
        assertEquals("New Description", newEvent.getDescription());
        assertEquals("New Location", newEvent.getLocation());
        assertEquals(200, newEvent.getMaxPeople());
        assertEquals(2, newEvent.getIdType());
        assertEquals(2, newEvent.getIdStatus());
        assertEquals(2, newEvent.getIdOrganizer());
        assertEquals("2024-03-21", newEvent.getDataEvent());
    }

    @Test
    void testProperties() {
        assertEquals(TEST_ID, event.idEventProperty().get());
        assertEquals(TEST_NAME, event.nameProperty().get());
        assertEquals(TEST_DESCRIPTION, event.descriptionProperty().get());
        assertEquals(TEST_LOCATION, event.locationProperty().get());
        assertEquals(TEST_MAX_PEOPLE, event.maxPeopleProperty().get());
        assertEquals(TEST_TYPE, event.idTypeProperty().get());
        assertEquals(TEST_STATUS, event.idStatusProperty().get());
        assertEquals(TEST_ORGANIZER, event.idOrganizerProperty().get());
        assertEquals(TEST_DATE, event.dataEventProperty().get());
    }

    @Test
    void testGetStatus() {
        Event confirmedEvent = new Event();
        confirmedEvent.setIdStatus(1);
        assertEquals("Подтверждено", confirmedEvent.getStatus());

        Event pendingEvent = new Event();
        pendingEvent.setIdStatus(2);
        assertEquals("Ожидание", pendingEvent.getStatus());

        Event rejectedEvent = new Event();
        rejectedEvent.setIdStatus(3);
        assertEquals("Отклонено", rejectedEvent.getStatus());

        Event unknownEvent = new Event();
        unknownEvent.setIdStatus(4);
        assertEquals("Неизвестно", unknownEvent.getStatus());
    }

    @Test
    void testGetTime() {
        assertEquals("14:00", event.getTime());
    }

    @Test
    void testToString() {
        assertEquals(TEST_NAME, event.toString());
    }
} 