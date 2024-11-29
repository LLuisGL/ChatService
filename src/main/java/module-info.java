module com.mycompany.discordserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.rabbitmq.client;
    requires java.sql;
    requires com.fasterxml.jackson.databind;

    opens com.mycompany.discordserver to javafx.fxml;
    exports com.mycompany.discordserver;
    exports model;
}
