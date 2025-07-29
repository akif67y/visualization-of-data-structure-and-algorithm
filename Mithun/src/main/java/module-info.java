module org.example.dsa_simulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires org.json;
    requires annotations;

    opens org.example.dsa_simulator to javafx.fxml;
    opens org.example.dsa_simulator.graph to javafx.fxml;
    opens org.example.dsa_simulator.heap to javafx.fxml;
    opens org.example.dsa_simulator.ai to javafx.fxml;
    opens org.example.dsa_simulator.sort;
    opens org.example.dsa_simulator.bst;

    exports org.example.dsa_simulator;
    exports org.example.dsa_simulator.graph;
    exports org.example.dsa_simulator.heap;
    exports org.example.dsa_simulator.ai;
    exports org.example.dsa_simulator.sort;
    exports org.example.dsa_simulator.bst;
}