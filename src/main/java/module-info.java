module me.image.manager {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.zondicons;

    opens me.image.manager to javafx.fxml;
    exports me.image.manager;
}