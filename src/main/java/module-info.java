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

    opens me.image.manager to javafx.fxml;
    exports me.image.manager to javafx.graphics;
}