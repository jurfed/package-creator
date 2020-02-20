package com.octavianonline.packagepreparer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class ViewConfig {

    @Autowired
    private ApplicationContext context;

    @Bean("rootView")
    public Parent getRootView() throws IOException {
        return getParent("/fxml/main_screen.fxml");
    }

    protected Parent getParent(String url) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(url));
        loader.setControllerFactory(this.context::getBean);
        return loader.load();
    }

}