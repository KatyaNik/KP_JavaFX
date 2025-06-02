package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CurrentUserTest {
    private CurrentUser user;
    private static final int TEST_ID = 1;
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final int TEST_ROLE_ID = 1;

    @BeforeEach
    void setUp() {
        user = new CurrentUser(TEST_ID, TEST_USERNAME, TEST_EMAIL, TEST_ROLE_ID);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals(TEST_ID, user.getId());
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(TEST_ROLE_ID, user.getRoleId());
    }

    @Test
    void testImmutability() {
        // Создаем нового пользователя с теми же данными
        CurrentUser sameUser = new CurrentUser(TEST_ID, TEST_USERNAME, TEST_EMAIL, TEST_ROLE_ID);
        
        // Проверяем, что значения совпадают
        assertEquals(user.getId(), sameUser.getId());
        assertEquals(user.getUsername(), sameUser.getUsername());
        assertEquals(user.getEmail(), sameUser.getEmail());
        assertEquals(user.getRoleId(), sameUser.getRoleId());
    }

    @Test
    void testWithDifferentValues() {
        CurrentUser differentUser = new CurrentUser(2, "otherUser", "other@example.com", 2);
        
        assertNotEquals(user.getId(), differentUser.getId());
        assertNotEquals(user.getUsername(), differentUser.getUsername());
        assertNotEquals(user.getEmail(), differentUser.getEmail());
        assertNotEquals(user.getRoleId(), differentUser.getRoleId());
    }

    @Test
    void testEmailFormat() {
        assertTrue(user.getEmail().contains("@"), "Email should contain @ symbol");
        assertTrue(user.getEmail().contains("."), "Email should contain domain");
    }
} 