module org.example.dsa_simulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;

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
    requires animatefx;

    opens org.example.dsa_simulator to javafx.fxml;
    opens org.example.dsa_simulator.graph to javafx.fxml;
    opens org.example.dsa_simulator.heap to javafx.fxml;
    opens org.example.dsa_simulator.ai to javafx.fxml;
    opens org.example.dsa_simulator.sort to javafx.fxml;
    opens org.example.dsa_simulator.bst to javafx.fxml;
    opens org.example.dsa_simulator.dijkstra to javafx.fxml;
    opens org.example.dsa_simulator.dynamicProgramming to javafx.fxml;
    opens org.example.dsa_simulator.linkedlist to javafx.fxml;
    opens org.example.dsa_simulator.prim to javafx.fxml;
    opens org.example.dsa_simulator.stack to javafx.fxml;
    opens org.example.dsa_simulator.queue to javafx.fxml;


    exports org.example.dsa_simulator;
    exports org.example.dsa_simulator.graph;
    exports org.example.dsa_simulator.heap;
    exports org.example.dsa_simulator.ai;
    exports org.example.dsa_simulator.sort;
    exports org.example.dsa_simulator.bst;
    exports org.example.dsa_simulator.dijkstra;
    exports org.example.dsa_simulator.stack;
    exports org.example.dsa_simulator.queue;
    exports org.example.dsa_simulator.prim;
    exports org.example.dsa_simulator.dynamicProgramming;



}