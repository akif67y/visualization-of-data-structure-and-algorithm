module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.demo to javafx.fxml;
    exports com.example.demo;
    exports com.example.demo.linkedlist;
    opens com.example.demo.linkedlist to javafx.fxml;
    exports com.example.demo.stack;
    opens com.example.demo.stack to javafx.fxml;
    exports com.example.demo.queue;
    opens com.example.demo.queue to javafx.fxml;

}