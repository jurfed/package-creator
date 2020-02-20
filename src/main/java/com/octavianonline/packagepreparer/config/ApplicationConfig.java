package com.octavianonline.packagepreparer.config;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octavianonline.packagepreparer.model.ApplicationModel;
import com.octavianonline.packagepreparer.model.IApplicationModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ApplicationConfig {

    @Bean
    @Qualifier("defaultWorkDirectory")
    public File getDefaultWorkDirectory() {
        return new File(System.getProperty("user.home"));
    }

    @Bean
    public IApplicationModel createIApplicationModel(@Qualifier("defaultWorkDirectory") File file) {
        final ApplicationModel applicationModel = new ApplicationModel();
        applicationModel.setWorkDirectory(file);
        return applicationModel;
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }

}
