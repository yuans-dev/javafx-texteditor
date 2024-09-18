module com.eggplanters.le3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.eggplanters.le3 to javafx.fxml;

    exports com.eggplanters.le3;
}
