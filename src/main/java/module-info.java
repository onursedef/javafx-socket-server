module com.onursedef.postappjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;

    opens com.onursedef.postappjavafx to javafx.fxml;
    exports com.onursedef.postappjavafx;
}