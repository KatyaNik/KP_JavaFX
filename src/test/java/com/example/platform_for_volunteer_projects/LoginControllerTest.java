package com.example.platform_for_volunteer_projects;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@ExtendWith(ApplicationExtension.class)
public class LoginControllerTest extends ApplicationTest {

    private LoginController loginController;

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        loginController = spy(new LoginController());

        doReturn(mockConnection).when(loginController).getConnection();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
        FxToolkit.hideStage();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/platform_for_volunteer_projects/login-view.fxml"));
        loader.setController(loginController);
        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testSuccessfulLogin_AsVolunteer() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("passwordOfUser")).thenReturn(LoginController.PasswordUtils.hash("password"));
        when(mockResultSet.getInt("id_user")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("volunteer");
        when(mockResultSet.getInt("id_of_role")).thenReturn(2); // Volunteer role

        clickOn("#textFieldMail").write("volunteer@test.com");
        clickOn("#textFieldPassword").write("password");
        clickOn("#buttonLogin");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPreparedStatement).setString(1, "volunteer@test.com");
    }

    @Test
    public void testSuccessfulLogin_AsOrganizer() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("passwordOfUser")).thenReturn(LoginController.PasswordUtils.hash("password"));
        when(mockResultSet.getInt("id_user")).thenReturn(2);
        when(mockResultSet.getString("username")).thenReturn("organizer");
        when(mockResultSet.getInt("id_of_role")).thenReturn(1); // Organizer role

        clickOn("#textFieldMail").write("viktor@gmail.com");
        clickOn("#textFieldPassword").write("1234");
        clickOn("#buttonLogin");
        WaitForAsyncUtils.waitForFxEvents();

        verify(mockPreparedStatement).setString(1, "organizer@test.com");
    }

    @Test
    public void testFailedLogin_WrongPassword() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("passwordOfUser")).thenReturn(LoginController.PasswordUtils.hash("correct_password"));

        clickOn("#textFieldMail").write("user@test.com");
        clickOn("#textFieldPassword").write("wrong_password");
        clickOn("#buttonLogin");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        clickOn("OK");
    }

    @Test
    public void testFailedLogin_UserNotFound() throws Exception {
        when(mockResultSet.next()).thenReturn(false);

        clickOn("#textFieldMail").write("nonexistent@test.com");
        clickOn("#textFieldPassword").write("password");
        clickOn("#buttonLogin");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        clickOn("OK");
    }

    @Test
    public void testLogin_WithEmptyFields() {
        clickOn("#buttonLogin");
        WaitForAsyncUtils.waitForFxEvents();

        verifyThat(".dialog-pane", isVisible());
        clickOn("OK");
    }
    
    @Test
    public void testRegistrationButton_OpensRegistrationWindow() {
        clickOn("#buttonRegistration");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testObserverButton_OpensObserverWindow() {
        clickOn("#buttonView");
        WaitForAsyncUtils.waitForFxEvents();
    }
} 