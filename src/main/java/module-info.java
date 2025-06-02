module com.example.platform_for_volunteer_projects {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.mail;
    requires itextpdf;
    requires org.apache.poi.ooxml;

    opens com.example.platform_for_volunteer_projects to javafx.fxml;
    exports com.example.platform_for_volunteer_projects;
}