module me.image.manager {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.fxml;
    requires javafx.base;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.zondicons;
    requires java.desktop;
    requires com.google.api.client.json.gson;
    requires com.google.api.client;
    requires com.google.api.services.drive;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires opencensus.api;

    opens me.image.manager to javafx.fxml;
    exports me.image.manager to javafx.graphics;
}