package com.octavianonline.packagepreparer;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class PackagePreparer extends Application {

    private ApplicationContext context;
    private static String[] arguments;
    private Parent root;

    public static void main(String[] args) {
        arguments = args;
        launch(PackagePreparer.class, args);
    }

    @Override
    public void init() throws Exception {
        this.context = SpringApplication.run(PackagePreparer.class, arguments);
        this.root = (Parent) context.getBean("rootView");
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Package creator");
        primaryStage.setWidth(900);
        primaryStage.setHeight(700);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
